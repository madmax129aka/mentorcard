package com.mentortrack.dto.upload;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** What the student confirms/edits after reviewing the OCR-extracted values (10th/12th/Diploma). */
public class ConfirmMarksheetRequest {

    @NotNull
    private Long documentId;

    private Double confirmedPercentage;

    private List<SubjectEntry> subjects;

    public static class SubjectEntry {
        private String subjectName;
        private Double marksObtained;

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public Double getMarksObtained() {
            return marksObtained;
        }

        public void setMarksObtained(Double marksObtained) {
            this.marksObtained = marksObtained;
        }
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Double getConfirmedPercentage() {
        return confirmedPercentage;
    }

    public void setConfirmedPercentage(Double confirmedPercentage) {
        this.confirmedPercentage = confirmedPercentage;
    }

    public List<SubjectEntry> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectEntry> subjects) {
        this.subjects = subjects;
    }
}
