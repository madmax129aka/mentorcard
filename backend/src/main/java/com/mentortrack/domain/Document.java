package com.mentortrack.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private DocumentType type;

    @Column(name = "file_path_encrypted", nullable = false)
    private String filePathEncrypted;

    @Lob
    @Column(name = "extracted_data_json")
    private String extractedDataJson;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    /** True once the OCR-extracted data (if applicable) has been confirmed by the student. */
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public String getFilePathEncrypted() {
        return filePathEncrypted;
    }

    public void setFilePathEncrypted(String filePathEncrypted) {
        this.filePathEncrypted = filePathEncrypted;
    }

    public String getExtractedDataJson() {
        return extractedDataJson;
    }

    public void setExtractedDataJson(String extractedDataJson) {
        this.extractedDataJson = extractedDataJson;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
