package com.mentortrack.dto.dashboard;

/** Flattened view of one subject's marks for a given semester, used both by the dashboard API and the PDF generator. */
public record SubjectMarkView(
        Long subjectId,
        String subjectName,
        String subjectCode,
        int semesterNumber,
        Double cat1,
        Double cat2,
        Double cat3,
        Double preUniv,
        Double intMarks,
        Double uniMarks,
        String clearedMonthYear,
        String source
) {
}
