package com.mentortrack.dto.upload;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** What the student confirms/edits after reviewing OCR-extracted semester marksheet data. */
public class ConfirmSemesterMarksheetRequest {

    @NotNull
    private Long documentId;

    @NotNull
    private Integer semesterNumber;

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

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public List<SubjectResultEntry> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectResultEntry> subjects) {
        this.subjects = subjects;
    }
}
