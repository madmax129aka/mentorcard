package com.mentortrack.pdf;

import com.mentortrack.config.PdfProperties;
import com.mentortrack.domain.Mark;
import com.mentortrack.domain.Student;
import com.mentortrack.domain.Subject;
import com.mentortrack.dto.dashboard.SubjectMarkView;
import com.mentortrack.service.GpaCalculator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fills in the real Mentor Card PDF template (see resources/templates/*.pdf, sourced from the
 * repo's original university-provided files) with a student's collected data, using PDFBox to draw
 * text directly onto the existing template pages at the fixed coordinates defined in
 * {@link MentorCardCoordinates}.
 *
 * This is an overlay, not a from-scratch render: the template's own layout, borders, and static
 * labels are left completely untouched — only the blank cells are filled in.
 */
@Service
public class MentorCardPdfService {

    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PdfProperties pdfProperties;
    private final GpaCalculator gpaCalculator;

    public MentorCardPdfService(PdfProperties pdfProperties, GpaCalculator gpaCalculator) {
        this.pdfProperties = pdfProperties;
        this.gpaCalculator = gpaCalculator;
    }

    /**
     * @param marksBySemester semester number (1-8) -> ordered list of that semester's subject marks
     */
    public byte[] generate(Student student, Map<Integer, List<SubjectMarkView>> marksBySemester) {
        try (PDDocument page1Doc = loadTemplate(pdfProperties.getTemplatePage1());
             PDDocument page2Doc = loadTemplate(pdfProperties.getTemplatePage2())) {

            fillHeader(page1Doc, student);

            for (int semester = 1; semester <= 4; semester++) {
                fillSemesterTable(page1Doc, semester, marksBySemester);
            }
            for (int semester = 5; semester <= 8; semester++) {
                fillSemesterTable(page2Doc, semester, marksBySemester);
            }

            return mergeAndExport(page1Doc, page2Doc);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Mentor Card PDF for " + student.getRegNo(), e);
        }
    }

    private PDDocument loadTemplate(String classpathLocation) throws IOException {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return PDDocument.load(in);
        }
    }

    private void fillHeader(PDDocument doc, Student student) throws IOException {
        PDPage page = doc.getPage(0);
        try (var cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(
                doc, page, org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true, true)) {
            PDFont font = PDType1Font.HELVETICA;
            cs.setFont(font, MentorCardCoordinates.FONT_SIZE_HEADER);
            cs.setNonStrokingColor(0f, 0f, 0f);

            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.MENTOR_NAME_SEM1_2_X, MentorCardCoordinates.MENTOR_NAME_Y,
                    orEmpty(student.getMentorName()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.MENTOR_NAME_SEM3_8_X, MentorCardCoordinates.MENTOR_NAME_Y,
                    orEmpty(student.getMentorName()));

            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.REG_NO_X, MentorCardCoordinates.HEADER_ROW1_Y, orEmpty(student.getRegNo()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.STUDENT_NAME_X, MentorCardCoordinates.HEADER_ROW1_Y, orEmpty(student.getName()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.DOB_X, MentorCardCoordinates.HEADER_ROW1_Y,
                    student.getDob() != null ? student.getDob().format(DOB_FORMAT) : "");
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.ADMITTED_ON_X, MentorCardCoordinates.HEADER_ROW1_Y, orEmpty(student.getAdmittedOn()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.BLOOD_GROUP_X, MentorCardCoordinates.HEADER_ROW1_Y, orEmpty(student.getBloodGroup()));

            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.HOBBIES_X, MentorCardCoordinates.HOBBIES_ROW_Y, orEmpty(student.getHobbies()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.GAMES_X, MentorCardCoordinates.HOBBIES_ROW_Y, orEmpty(student.getGames()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.LITERARY_X, MentorCardCoordinates.HOBBIES_ROW_Y, orEmpty(student.getLiterary()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.COMMUNITY_X, MentorCardCoordinates.HOBBIES_ROW_Y, orEmpty(student.getCommunity()));

            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.PCT_10TH_X, MentorCardCoordinates.PERCENTAGE_ROW_Y, formatPercentage(student.getPercentage10th()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.PCT_12TH_X, MentorCardCoordinates.PERCENTAGE_ROW_Y, formatPercentage(student.getPercentage12th()));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    MentorCardCoordinates.PCT_DIPLOMA_X, MentorCardCoordinates.PERCENTAGE_ROW_Y, formatPercentage(student.getPercentageDiploma()));
        }
    }

    private void fillSemesterTable(PDDocument doc, int semesterNumber, Map<Integer, List<SubjectMarkView>> marksBySemester) throws IOException {
        MentorCardCoordinates.SemesterLayout layout = MentorCardCoordinates.layoutFor(semesterNumber);
        List<SubjectMarkView> subjectMarks = marksBySemester.getOrDefault(semesterNumber, List.of());
        PDPage page = doc.getPage(0); // each PDDocument here holds exactly one page (page 1 or page 2 of the form)
        var columns = layout.columns();

        try (var cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(
                doc, page, org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true, true)) {
            PDFont font = PDType1Font.HELVETICA;
            cs.setNonStrokingColor(0f, 0f, 0f);

            float[] rowTops = layout.rowTops();
            for (int i = 0; i < subjectMarks.size() && i < rowTops.length; i++) {
                SubjectMarkView m = subjectMarks.get(i);
                float baselineY = rowTops[i] - MentorCardCoordinates.ROW_TEXT_BASELINE_OFFSET;

                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.cat1(), baselineY, formatMark(m.cat1()));
                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.cat2(), baselineY, formatMark(m.cat2()));
                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.preUniv(), baselineY, formatMark(m.preUniv()));
                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.intMarks(), baselineY, formatMark(m.intMarks()));
                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.uniMarks(), baselineY, formatMark(m.uniMarks()));
                drawText(cs, font, MentorCardCoordinates.FONT_SIZE_MARKS, columns.clearedIn(), baselineY, orEmpty(m.clearedMonthYear()));
            }

            Double gpa = gpaCalculator.gpaForSemester(subjectMarks);
            Double cgpa = gpaCalculator.cgpaThroughSemester(semesterNumber, marksBySemester);
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    layout.gpaLabelX() + MentorCardCoordinates.GPA_VALUE_OFFSET_X, layout.gpaRowY(), formatMark(gpa));
            drawText(cs, font, MentorCardCoordinates.FONT_SIZE_HEADER,
                    layout.cgpaLabelX() + MentorCardCoordinates.GPA_VALUE_OFFSET_X, layout.gpaRowY(), formatMark(cgpa));
        }
    }

    private void drawText(org.apache.pdfbox.pdmodel.PDPageContentStream cs, PDFont font, float size,
                           float x, float y, String text) throws IOException {
        if (text == null || text.isBlank()) {
            return;
        }
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    /** PDFBox's standard-14 fonts only support WinAnsi-encodable characters. */
    private String sanitize(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c < 256 ? c : '?');
        }
        return sb.toString();
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatPercentage(Double value) {
        if (value == null) {
            return "";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatMark(Double value) {
        if (value == null) {
            return "";
        }
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private byte[] mergeAndExport(PDDocument page1Doc, PDDocument page2Doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument merged = new PDDocument()) {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.appendDocument(merged, page1Doc);
            merger.appendDocument(merged, page2Doc);
            merged.save(out);
        }
        return out.toByteArray();
    }
}
