package com.mentortrack.dto.upload;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * What the student confirms/edits after reviewing OCR-extracted semester marksheet data. The
 * semester number itself is no longer part of this payload — it is implied by which
 * SEMESTER_n_MARKSHEET document type/endpoint the student uploaded to.
 */
public class ConfirmSemesterMarksheetRequest {

    @NotNull
    private Long documentId;

    private List<SubjectResultEntry> subjects;

    public static class SubjectResultEntry {
        private String subjectCode;
        private Double uniMarks;
        private String clearedMonthYear;

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public Double getUniMarks() {
            return uniMarks;
        }

        public void setUniMarks(Double uniMarks) {
            this.uniMarks = uniMarks;
        }

        public String getClearedMonthYear() {
            return clearedMonthYear;
        }

        public void setClearedMonthYear(String clearedMonthYear) {
            this.clearedMonthYear = clearedMonthYear;
        }
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public List<SubjectResultEntry> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectResultEntry> subjects) {
        this.subjects = subjects;
    }
}
