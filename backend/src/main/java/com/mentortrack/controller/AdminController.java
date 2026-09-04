package com.mentortrack.controller;

import com.mentortrack.dto.admin.CreateStudentRequest;
import com.mentortrack.dto.admin.ImportSummaryResponse;
import com.mentortrack.dto.admin.StudentSummaryResponse;
import com.mentortrack.service.CatMarksImportService;
import com.mentortrack.service.StudentAdminService;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CatMarksImportService catMarksImportService;
    private final StudentAdminService studentAdminService;

    public AdminController(CatMarksImportService catMarksImportService, StudentAdminService studentAdminService) {
        this.catMarksImportService = catMarksImportService;
        this.studentAdminService = studentAdminService;
    }

    @PostMapping(value = "/import-cat-marks", consumes = "multipart/form-data")
    public ImportSummaryResponse importCatMarks(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return catMarksImportService.importExcel(file, authentication.getName());
    }

    @PostMapping("/students")
    public StudentSummaryResponse createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return studentAdminService.createStudent(request);
    }

    @GetMapping("/students")
    public List<StudentSummaryResponse> listStudents(@RequestParam(value = "regNo", required = false) String regNo) {
        return studentAdminService.search(regNo);
    }

    @GetMapping("/import-batches")
    public List<ImportSummaryResponse> listImportBatches() {
        return catMarksImportService.listBatches();
    }

    /** Serves the bundled sample master CAT-marks Excel (seed-assets/) so the import flow can be demoed end-to-end. */
    @GetMapping("/sample-cat-marks-excel")
    public ResponseEntity<byte[]> sampleCatMarksExcel() {
        try {
            ClassPathResource resource = new ClassPathResource("seed-assets/CAT_Marks_Import_Sample.xlsx");
            byte[] bytes = resource.getInputStream().readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename("CAT_Marks_Import_Sample.xlsx").build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
