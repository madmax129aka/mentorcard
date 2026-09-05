package com.mentortrack.service;

import com.mentortrack.dto.dashboard.SubjectMarkView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GpaCalculatorTest {

    private final GpaCalculator calculator = new GpaCalculator();

    private SubjectMarkView markWithUniMarks(double uniMarks) {
        return new SubjectMarkView(1L, "Test Subject", "CS101", 1, null, null, null, null, null, uniMarks, null, "OCR");
    }

    @Test
    void gpaForSemester_returnsNullWhenNoMarks() {
        assertNull(calculator.gpaForSemester(List.of()));
    }

    @Test
    void gpaForSemester_ignoresSubjectsWithoutUniMarks() {
        SubjectMarkView noUniMarks = new SubjectMarkView(1L, "S", "CS101", 1, null, null, null, null, null, null, null, "OCR");
        assertNull(calculator.gpaForSemester(List.of(noUniMarks)));
    }

    @Test
    void gpaForSemester_computesUnweightedAverageGradePoint() {
        // 95 -> 10, 85 -> 9 => average 9.5
        List<SubjectMarkView> marks = List.of(markWithUniMarks(95), markWithUniMarks(85));
        assertEquals(9.5, calculator.gpaForSemester(marks), 0.0001);
    }

    @Test
    void gpaForSemester_belowPassingMarkYieldsZeroGradePoint() {
        List<SubjectMarkView> marks = List.of(markWithUniMarks(20));
        assertEquals(0.0, calculator.gpaForSemester(marks), 0.0001);
    }

    @Test
    void cgpaThroughSemester_averagesSemesterGpas() {
        Map<Integer, List<SubjectMarkView>> bySemester = Map.of(
                1, List.of(markWithUniMarks(95)),  // GPA 10
                2, List.of(markWithUniMarks(65))   // GPA 7
        );
        // CGPA through semester 2 = average(10, 7) = 8.5
        assertEquals(8.5, calculator.cgpaThroughSemester(2, bySemester), 0.0001);
        // CGPA through semester 1 = 10 (only semester 1 counted)
        assertEquals(10.0, calculator.cgpaThroughSemester(1, bySemester), 0.0001);
    }

    @Test
    void cgpaThroughSemester_returnsNullWhenNoSemestersQualify() {
        Map<Integer, List<SubjectMarkView>> bySemester = Map.of(3, List.of(markWithUniMarks(95)));
        assertNull(calculator.cgpaThroughSemester(1, bySemester));
    }
}
