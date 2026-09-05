package com.mentortrack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentortrack.domain.*;
import com.mentortrack.dto.upload.ConfirmMarksheetRequest;
import com.mentortrack.dto.upload.ConfirmSemesterMarksheetRequest;
import com.mentortrack.dto.upload.OcrExtractionResponse;
import com.mentortrack.exception.BadRequestException;
import com.mentortrack.exception.NotFoundException;
import com.mentortrack.ocr.MarksheetExtractor;
import com.mentortrack.ocr.OcrService;
import com.mentortrack.repository.DocumentRepository;
import com.mentortrack.repository.MarkRepository;
import com.mentortrack.repository.StudentRepository;
import com.mentortrack.repository.SubjectRepository;
import com.mentortrack.storage.EncryptedFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentUploadService {

    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;
    private final MarkRepository markRepository;
    private final SubjectRepository subjectRepository;
    private final EncryptedFileStorageService storageService;
    private final OcrService ocrService;
    private final MarksheetExtractor marksheetExtractor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentUploadService(StudentRepository studentRepository,
                                  DocumentRepository documentRepository,
                                  MarkRepository markRepository,
                                  SubjectRepository subjectRepository,
                                  EncryptedFileStorageService storageService,
                                  OcrService ocrService,
                                  MarksheetExtractor marksheetExtractor) {
        this.studentRepository = studentRepository;
        this.documentRepository = documentRepository;
        this.markRepository = markRepository;
        this.subjectRepository = subjectRepository;
        this.storageService = storageService;
        this.ocrService = ocrService;
        this.marksheetExtractor = marksheetExtractor;
    }

    /** 10th / 12th / Diploma / Semester marksheet upload: stores the file, runs OCR, returns for review (not yet saved). */
    @Transactional
    public OcrExtractionResponse uploadAndExtract(String regNo, DocumentType type, MultipartFile file) {
        Student student = requireStudent(regNo);

        String storedPath;
        try {
            storedPath = storageService.store(regNo, type.name(), file.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }

        Document document = documentRepository.findByStudentAndType(student, type).orElseGet(Document::new);
        document.setStudent(student);
        document.setType(type);
        document.setFilePathEncrypted(storedPath);
        document.setConfirmed(false);

        String ocrText;
        try (var in = storageService.retrieve(storedPath)) {
            ocrText = ocrService.extractText(in);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read back stored file for OCR: " + e.getMessage());
        }

        MarksheetExtractor.ExtractedMarksheet extracted = marksheetExtractor.extract(ocrText);
        try {
            document.setExtractedDataJson(objectMapper.writeValueAsString(extracted));
        } catch (Exception e) {
            document.setExtractedDataJson(null);
        }
        documentRepository.save(document);

        return new OcrExtractionResponse(document.getId(), extracted.subjects(), extracted.overallPercentage(), extracted.rawText());
    }

    /** Student confirms/edits the 10th/12th/Diploma OCR result -> persist percentage on Student, mark Document confirmed. */
    @Transactional
    public void confirmMarksheet(String regNo, DocumentType type, ConfirmMarksheetRequest request) {
        if (type.isSemesterMarksheet()) {
            throw new BadRequestException("Use the semester-marksheet confirm endpoint for semester results");
        }
        Student student = requireStudent(regNo);
        Document document = documentRepository.findByStudentAndType(student, type)
                .orElseThrow(() -> new NotFoundException("No uploaded document to confirm for " + type));
        if (!document.getId().equals(request.getDocumentId())) {
            throw new BadRequestException("documentId does not match the most recent upload for this document type");
        }

        Double percentage = request.getConfirmedPercentage();
        switch (type) {
            case MARKSHEET_10 -> student.setPercentage10th(percentage);
            case MARKSHEET_12 -> student.setPercentage12th(percentage);
            case DIPLOMA -> student.setPercentageDiploma(percentage);
            default -> throw new BadRequestException("Unsupported marksheet type: " + type);
        }
        studentRepository.save(student);

        document.setConfirmed(true);
        documentRepository.save(document);
    }

    /**
     * Student confirms/edits one semester's marksheet OCR result -> upserts Mark rows (source=OCR).
     * {@code type} must be one of the SEMESTER_n_MARKSHEET constants; the semester number is taken
     * from that document type, not from the request body.
     */
    @Transactional
    public void confirmSemesterMarksheet(String regNo, DocumentType type, ConfirmSemesterMarksheetRequest request) {
        if (!type.isSemesterMarksheet()) {
            throw new BadRequestException("Use the marksheet confirm endpoint for non-semester documents");
        }
        int semesterNumber = type.getSemesterNumber();

        Student student = requireStudent(regNo);
        Document document = documentRepository.findByStudentAndType(student, type)
                .orElseThrow(() -> new NotFoundException("No uploaded marksheet to confirm for Semester " + semesterNumber));
        if (!document.getId().equals(request.getDocumentId())) {
            throw new BadRequestException("documentId does not match the most recent upload");
        }

        List<ConfirmSemesterMarksheetRequest.SubjectResultEntry> entries = request.getSubjects();
        if (entries == null || entries.isEmpty()) {
            throw new BadRequestException("No subject results provided to confirm");
        }

        for (var entry : entries) {
            Subject subject = subjectRepository.findBySemesterNumberAndSubjectCode(semesterNumber, entry.getSubjectCode())
                    .orElseThrow(() -> new BadRequestException("Unknown subject code " + entry.getSubjectCode()
                            + " for semester " + semesterNumber));

            Mark mark = markRepository.findByStudentAndSubject(student, subject).orElseGet(Mark::new);
            mark.setStudent(student);
            mark.setSubject(subject);
            mark.setUniMarks(entry.getUniMarks());
            mark.setClearedMonthYear(entry.getClearedMonthYear());
            if (mark.getSource() == null) {
                mark.setSource(MarkSource.OCR);
            }
            markRepository.save(mark);
        }

        document.setConfirmed(true);
        documentRepository.save(document);
    }

    /** Aadhaar / PAN upload: plain encrypted storage, no OCR, just marks the Document as uploaded. */
    @Transactional
    public void uploadIdentityDocument(String regNo, DocumentType type, MultipartFile file) {
        if (type != DocumentType.AADHAAR && type != DocumentType.PAN) {
            throw new BadRequestException("This endpoint only accepts AADHAAR or PAN documents");
        }
        Student student = requireStudent(regNo);

        String storedPath;
        try {
            storedPath = storageService.store(regNo, type.name(), file.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }

        Document document = documentRepository.findByStudentAndType(student, type).orElseGet(Document::new);
        document.setStudent(student);
        document.setType(type);
        document.setFilePathEncrypted(storedPath);
        document.setConfirmed(true); // no OCR confirm step needed; upload itself is the confirmation
        documentRepository.save(document);
    }

    private Student requireStudent(String regNo) {
        return studentRepository.findByRegNo(regNo)
                .orElseThrow(() -> new NotFoundException("Student not found: " + regNo));
    }
}
