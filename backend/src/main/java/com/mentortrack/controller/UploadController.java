package com.mentortrack.controller;

import com.mentortrack.domain.DocumentType;
import com.mentortrack.dto.upload.ConfirmMarksheetRequest;
import com.mentortrack.dto.upload.ConfirmSemesterMarksheetRequest;
import com.mentortrack.dto.upload.OcrExtractionResponse;
import com.mentortrack.service.DocumentUploadService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Each of the eight semesters has its own independent upload/confirm endpoint pair
 * (/semester/{n}/upload, /semester/{n}/confirm) rather than one shared "semester marksheet"
 * endpoint, so a student can upload/confirm any semester's marksheet on its own.
 */
@RestController
@RequestMapping("/api/student/documents")
public class UploadController {

    private final DocumentUploadService uploadService;

    public UploadController(DocumentUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/{type}/upload", consumes = "multipart/form-data")
    public Object upload(@PathVariable DocumentType type,
                          @RequestParam("file") MultipartFile file,
                          Authentication authentication) {
        String regNo = authentication.getName();
        if (type == DocumentType.AADHAAR || type == DocumentType.PAN) {
            uploadService.uploadIdentityDocument(regNo, type, file);
            return Map.of("status", "uploaded");
        }
        return uploadService.uploadAndExtract(regNo, type, file);
    }

    @PostMapping("/{type}/confirm")
    public void confirmMarksheet(@PathVariable DocumentType type,
                                  @Valid @RequestBody ConfirmMarksheetRequest request,
                                  Authentication authentication) {
        String regNo = authentication.getName();
        uploadService.confirmMarksheet(regNo, type, request);
    }

    /**
     * Per-semester upload endpoint, e.g. POST /api/student/documents/semester/3/upload for
     * Semester III. {@code semesterNumber} (1-8) is translated to the matching
     * SEMESTER_n_MARKSHEET document type.
     */
    @PostMapping(value = "/semester/{semesterNumber}/upload", consumes = "multipart/form-data")
    public OcrExtractionResponse uploadSemesterMarksheet(@PathVariable int semesterNumber,
                                                          @RequestParam("file") MultipartFile file,
                                                          Authentication authentication) {
        String regNo = authentication.getName();
        DocumentType type = semesterMarksheetType(semesterNumber);
        return uploadService.uploadAndExtract(regNo, type, file);
    }

    @PostMapping("/semester/{semesterNumber}/confirm")
    public void confirmSemesterMarksheet(@PathVariable int semesterNumber,
                                          @Valid @RequestBody ConfirmSemesterMarksheetRequest request,
                                          Authentication authentication) {
        String regNo = authentication.getName();
        DocumentType type = semesterMarksheetType(semesterNumber);
        uploadService.confirmSemesterMarksheet(regNo, type, request);
    }

    private DocumentType semesterMarksheetType(int semesterNumber) {
        return switch (semesterNumber) {
            case 1 -> DocumentType.SEMESTER_1_MARKSHEET;
            case 2 -> DocumentType.SEMESTER_2_MARKSHEET;
            case 3 -> DocumentType.SEMESTER_3_MARKSHEET;
            case 4 -> DocumentType.SEMESTER_4_MARKSHEET;
            case 5 -> DocumentType.SEMESTER_5_MARKSHEET;
            case 6 -> DocumentType.SEMESTER_6_MARKSHEET;
            case 7 -> DocumentType.SEMESTER_7_MARKSHEET;
            case 8 -> DocumentType.SEMESTER_8_MARKSHEET;
            default -> throw new com.mentortrack.exception.BadRequestException(
                    "Semester number must be between 1 and 8, got: " + semesterNumber);
        };
    }
}
