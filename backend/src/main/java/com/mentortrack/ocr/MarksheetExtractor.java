package com.mentortrack.ocr;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Best-effort parsing of OCR'd marksheet text into structured subject-wise marks + overall
 * percentage. Tesseract's raw text output for scanned marksheets is inherently noisy, so this is
 * intentionally a heuristic first pass — the extracted result is ALWAYS shown to the student to
 * confirm/edit before anything is saved (per spec: "never silently auto-commit").
 */
@Component
public class MarksheetExtractor {

    // Matches lines like "MATHEMATICS   87" or "English - 76/100" -> subject + obtained marks.
    private static final Pattern SUBJECT_MARK_LINE =
            Pattern.compile("^([A-Za-z .,&/-]{3,40}?)\\s*[:\\-]?\\s*(\\d{1,3})\\s*(?:/\\s*100)?\\s*$");

    private static final Pattern PERCENTAGE_LINE =
            Pattern.compile("(?i)(percentage|percent|%)\\D{0,10}(\\d{1,3}(?:\\.\\d{1,2})?)");

    public record ExtractedSubjectMark(String subjectName, Double marksObtained) {
    }

    public record ExtractedMarksheet(List<ExtractedSubjectMark> subjects, Double overallPercentage, String rawText) {
    }

    public ExtractedMarksheet extract(String ocrText) {
        List<ExtractedSubjectMark> subjects = new ArrayList<>();
        Double percentage = null;

        for (String line : ocrText.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            Matcher pctMatcher = PERCENTAGE_LINE.matcher(trimmed);
            if (pctMatcher.find()) {
                percentage = safeParse(pctMatcher.group(2));
                continue;
            }

            Matcher subjMatcher = SUBJECT_MARK_LINE.matcher(trimmed);
            if (subjMatcher.matches()) {
                String subject = subjMatcher.group(1).trim();
                Double marks = safeParse(subjMatcher.group(2));
                if (!subject.isBlank() && marks != null && marks <= 100) {
                    subjects.add(new ExtractedSubjectMark(subject, marks));
                }
            }
        }

        if (percentage == null && !subjects.isEmpty()) {
            double avg = subjects.stream().mapToDouble(ExtractedSubjectMark::marksObtained).average().orElse(0);
            percentage = Math.round(avg * 100) / 100.0;
        }

        return new ExtractedMarksheet(subjects, percentage, ocrText);
    }

    private Double safeParse(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
