package com.mentortrack.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "import_batches")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uploaded_by_admin", nullable = false)
    private String uploadedByAdmin;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount;

    @Column(name = "unmatched_count", nullable = false)
    private int unmatchedCount;

    @Lob
    @Column(name = "unmatched_reg_nos_json")
    private String unmatchedRegNosJson;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUploadedByAdmin() {
        return uploadedByAdmin;
    }

    public void setUploadedByAdmin(String uploadedByAdmin) {
        this.uploadedByAdmin = uploadedByAdmin;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getUnmatchedCount() {
        return unmatchedCount;
    }

    public void setUnmatchedCount(int unmatchedCount) {
        this.unmatchedCount = unmatchedCount;
    }

    public String getUnmatchedRegNosJson() {
        return unmatchedRegNosJson;
    }

    public void setUnmatchedRegNosJson(String unmatchedRegNosJson) {
        this.unmatchedRegNosJson = unmatchedRegNosJson;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Instant importedAt) {
        this.importedAt = importedAt;
    }
}
