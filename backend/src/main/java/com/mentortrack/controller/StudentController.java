package com.mentortrack.controller;

import com.mentortrack.domain.Student;
import com.mentortrack.dto.dashboard.StudentDashboardResponse;
import com.mentortrack.pdf.MentorCardPdfService;
import com.mentortrack.service.StudentDashboardService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentDashboardService dashboardService;
    private final MentorCardPdfService pdfService;

    public StudentController(StudentDashboardService dashboardService, MentorCardPdfService pdfService) {
        this.dashboardService = dashboardService;
        this.pdfService = pdfService;
    }

    @GetMapping("/dashboard")
    public StudentDashboardResponse dashboard(Authentication authentication) {
        String regNo = authentication.getName();
        return dashboardService.buildDashboard(regNo);
    }

    @GetMapping("/mentor-card.pdf")
    public ResponseEntity<byte[]> downloadMentorCard(Authentication authentication) {
        String regNo = authentication.getName();
        Student student = dashboardService.requireByRegNo(regNo);
        var marksBySemester = dashboardService.marksBySemester(student);
        byte[] pdf = pdfService.generate(student, marksBySemester);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("MentorCard-" + student.getRegNo() + ".pdf")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
