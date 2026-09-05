package com.mentortrack.domain;

/**
 * Document types a student can upload. The eight SEMESTER_n_MARKSHEET values are deliberately
 * separate enum constants (not a single SEMESTER_MARKSHEET + a semester number field) so that each
 * semester gets its own independent upload slot, checklist entry, and Document row — a student can
 * upload/confirm Semester 3 without it having anything to do with Semester 1's upload.
 */
public enum DocumentType {
    MARKSHEET_10(null),
    MARKSHEET_12(null),
    DIPLOMA(null),
    SEMESTER_1_MARKSHEET(1),
    SEMESTER_2_MARKSHEET(2),
    SEMESTER_3_MARKSHEET(3),
    SEMESTER_4_MARKSHEET(4),
    SEMESTER_5_MARKSHEET(5),
    SEMESTER_6_MARKSHEET(6),
    SEMESTER_7_MARKSHEET(7),
    SEMESTER_8_MARKSHEET(8),
    AADHAAR(null),
    PAN(null);

    private final Integer semesterNumber;

    DocumentType(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    /** The semester this document type belongs to, or null if it's not a semester marksheet. */
    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public boolean isSemesterMarksheet() {
        return semesterNumber != null;
    }
}
