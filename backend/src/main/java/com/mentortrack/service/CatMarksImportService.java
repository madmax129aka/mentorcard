package com.mentortrack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentortrack.domain.*;
import com.mentortrack.dto.admin.ImportSummaryResponse;
import com.mentortrack.exception.BadRequestException;
import com.mentortrack.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Parses the admin-uploaded master CAT marks Excel sheet (Apache POI) and upserts Mark rows.
 *
 * Expected sheet format (header row required, column order does not matter, extra columns are
 * ignored):
 *
 * <pre>
 * | RegNo       | SubjectCode | CAT1 | CAT2 | CAT3 | PreUniv | IntMarks |
 * | 21CSE001    | CS101       | 18   | 20   | 19   | 22      | 20       |
 * </pre>
 *
 * - RegNo and SubjectCode are required per row; missing/blank values cause that row to be skipped
 *   and counted as unmatched.
 * - A row matches an existing Student (by reg_no) AND an existing Subject (by subject_code). Both
 *   must exist for the row to be applied; otherwise the row's reg_no is recorded as unmatched.
 * - CAT3 and PreUniv/IntMarks are optional per the spec ("if available").
 */
@Service
public class CatMarksImportService {

    private static final List<String> REQUIRED_HEADERS = List.of("regno", "subjectcode");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final MarkRepository markRepository;
    private final ImportBatchRepository importBatchRepository;

    public CatMarksImportService(StudentRepository studentRepository,
                                  SubjectRepository subjectRepository,
                                  MarkRepository markRepository,
                                  ImportBatchRepository importBatchRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.markRepository = markRepository;
        this.importBatchRepository = importBatchRepository;
    }

    @Transactional
    public ImportSummaryResponse importExcel(MultipartFile file, String adminUsername) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file uploaded");
        }

        List<Map<String, String>> rows;
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            rows = readRows(workbook);
        } catch (IOException e) {
            throw new BadRequestException("Could not read Excel file: " + e.getMessage());
        }

        int total = rows.size();
        int matched = 0;
        List<String> unmatched = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String regNo = trimToNull(row.get("regno"));
            String subjectCode = trimToNull(row.get("subjectcode"));

            if (regNo == null || subjectCode == null) {
                unmatched.add(regNo != null ? regNo : "(missing reg no)");
                continue;
            }

            Optional<Student> studentOpt = studentRepository.findByRegNo(regNo);
            if (studentOpt.isEmpty()) {
                unmatched.add(regNo);
                continue;
            }

            Optional<Subject> subjectOpt = findSubjectByCode(subjectCode);
            if (subjectOpt.isEmpty()) {
                unmatched.add(regNo + " (unknown subject code: " + subjectCode + ")");
                continue;
            }

            Student student = studentOpt.get();
            Subject subject = subjectOpt.get();

            Mark mark = markRepository.findByStudentAndSubject(student, subject).orElseGet(Mark::new);
            mark.setStudent(student);
            mark.setSubject(subject);
            mark.setCat1(parseDouble(row.get("cat1")));
            mark.setCat2(parseDouble(row.get("cat2")));
            mark.setCat3(parseDouble(row.get("cat3")));
            mark.setPreUniv(parseDouble(row.get("preuniv")));
            mark.setIntMarks(parseDouble(row.get("intmarks")));
            // Uni. Marks and Cleared-in intentionally left untouched here: they come from the
            // semester marksheet OCR flow, not the CAT-marks import, per the spec.
            mark.setSource(MarkSource.EXCEL_IMPORT);
            markRepository.save(mark);
            matched++;
        }

        ImportBatch batch = new ImportBatch();
        batch.setUploadedByAdmin(adminUsername);
        batch.setFilename(file.getOriginalFilename());
        batch.setTotalRows(total);
        batch.setMatchedCount(matched);
        batch.setUnmatchedCount(unmatched.size());
        batch.setUnmatchedRegNosJson(writeJson(unmatched));
        importBatchRepository.save(batch);

        return new ImportSummaryResponse(batch.getId(), batch.getFilename(), total, matched, unmatched.size(), unmatched);
    }

    public List<ImportSummaryResponse> listBatches() {
        return importBatchRepository.findAllByOrderByImportedAtDesc().stream()
                .map(b -> new ImportSummaryResponse(
                        b.getId(), b.getFilename(), b.getTotalRows(), b.getMatchedCount(), b.getUnmatchedCount(),
                        readJson(b.getUnmatchedRegNosJson())
                ))
                .toList();
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException("Failed to serialize unmatched reg nos", e));
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private Optional<Subject> findSubjectByCode(String subjectCode) {
        return subjectRepository.findAllByOrderBySemesterNumberAscDisplayOrderAsc().stream()
                .filter(s -> s.getSubjectCode().equalsIgnoreCase(subjectCode))
                .findFirst();
    }

    private List<Map<String, String>> readRows(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) {
            throw new BadRequestException("Excel sheet is empty");
        }

        Row headerRow = rowIterator.next();
        Map<Integer, String> headerByColumn = new HashMap<>();
        for (Cell cell : headerRow) {
            String normalized = cellToString(cell).trim().toLowerCase().replace(" ", "").replace(".", "").replace("_", "");
            headerByColumn.put(cell.getColumnIndex(), normalized);
        }

        for (String required : REQUIRED_HEADERS) {
            if (!headerByColumn.containsValue(required)) {
                throw new BadRequestException("Missing required column: " + required
                        + " (expected header names: RegNo, SubjectCode, CAT1, CAT2, CAT3, PreUniv, IntMarks)");
            }
        }

        List<Map<String, String>> rows = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isBlankRow(row)) {
                continue;
            }
            Map<String, String> rowMap = new HashMap<>();
            for (Map.Entry<Integer, String> entry : headerByColumn.entrySet()) {
                Cell cell = row.getCell(entry.getKey());
                rowMap.put(entry.getValue(), cell == null ? null : cellToString(cell));
            }
            rows.add(rowMap);
        }
        return rows;
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !cellToString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String cellToString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return String.valueOf((long) value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Double parseDouble(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
