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
            return java.util.Map.of("status", "uploaded");
        }
        return uploadOcr(type, file, authentication);
    }

    private OcrExtractionResponse uploadOcr(DocumentType type, MultipartFile file, Authentication authentication) {
        String regNo = authentication.getName();
        return uploadService.uploadAndExtract(regNo, type, file);
    }

    @PostMapping("/{type}/confirm")
    public void confirmMarksheet(@PathVariable DocumentType type,
                                  @Valid @RequestBody ConfirmMarksheetRequest request,
                                  Authentication authentication) {
        String regNo = authentication.getName();
        uploadService.confirmMarksheet(regNo, type, request);
    }

    @PostMapping("/semester-marksheet/confirm")
    public void confirmSemesterMarksheet(@Valid @RequestBody ConfirmSemesterMarksheetRequest request,
                                          Authentication authentication) {
        String regNo = authentication.getName();
        uploadService.confirmSemesterMarksheet(regNo, request);
    }
}
