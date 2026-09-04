package com.mentortrack.ocr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarksheetExtractorTest {

    private final MarksheetExtractor extractor = new MarksheetExtractor();

    @Test
    void extractsSubjectMarksAndPercentageFromCleanText() {
        String ocrText = """
                ENGLISH 87
                MATHEMATICS 91
                SCIENCE 84
                PERCENTAGE: 87.33
                """;

        MarksheetExtractor.ExtractedMarksheet result = extractor.extract(ocrText);

        assertEquals(3, result.subjects().size());
        assertEquals("ENGLISH", result.subjects().get(0).subjectName());
        assertEquals(87.0, result.subjects().get(0).marksObtained());
        assertEquals(87.33, result.overallPercentage());
    }

    @Test
    void computesAveragePercentageWhenNotExplicitlyPrinted() {
        String ocrText = "ENGLISH 80\nMATHS 90";
        MarksheetExtractor.ExtractedMarksheet result = extractor.extract(ocrText);
        assertEquals(85.0, result.overallPercentage());
    }

    @Test
    void ignoresBlankLinesAndNoise() {
        String ocrText = "\n\n   \nENGLISH 80\n###garbage###\n";
        MarksheetExtractor.ExtractedMarksheet result = extractor.extract(ocrText);
        assertEquals(1, result.subjects().size());
    }

    @Test
    void returnsEmptyResultForUnrecognizableText() {
        MarksheetExtractor.ExtractedMarksheet result = extractor.extract("completely unrelated garbage !!! 123456789012");
        assertTrue(result.subjects().isEmpty());
        assertNull(result.overallPercentage());
    }
}
