package com.mentortrack.dto.upload;

import com.mentortrack.ocr.MarksheetExtractor;

import java.util.List;

/** Returned to the frontend after OCR runs, for the student to review/edit before confirming save. */
public record OcrExtractionResponse(
        Long documentId,
        List<MarksheetExtractor.ExtractedSubjectMark> subjects,
        Double overallPercentage,
        String rawText
) {
}
