package com.mentortrack.service;

import com.mentortrack.domain.*;
import com.mentortrack.dto.dashboard.DocumentStatusView;
import com.mentortrack.dto.dashboard.StudentDashboardResponse;
import com.mentortrack.dto.dashboard.SubjectMarkView;
import com.mentortrack.exception.NotFoundException;
import com.mentortrack.repository.DocumentRepository;
import com.mentortrack.repository.MarkRepository;
import com.mentortrack.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;
    private final DocumentRepository documentRepository;
    private final GpaCalculator gpaCalculator;

    public StudentDashboardService(StudentRepository studentRepository,
                                    MarkRepository markRepository,
                                    DocumentRepository documentRepository,
                                    GpaCalculator gpaCalculator) {
        this.studentRepository = studentRepository;
        this.markRepository = markRepository;
        this.documentRepository = documentRepository;
        this.gpaCalculator = gpaCalculator;
    }

    public Student requireByRegNo(String regNo) {
        return studentRepository.findByRegNo(regNo)
                .orElseThrow(() -> new NotFoundException("Student not found: " + regNo));
    }

    public Map<Integer, List<SubjectMarkView>> marksBySemester(Student student) {
        List<Mark> marks = markRepository.findByStudent(student);
        Map<Integer, List<SubjectMarkView>> grouped = new TreeMap<>();
        for (Mark mark : marks) {
            Subject subject = mark.getSubject();
            SubjectMarkView view = new SubjectMarkView(
                    subject.getId(), subject.getName(), subject.getSubjectCode(), subject.getSemesterNumber(),
                    mark.getCat1(), mark.getCat2(), mark.getCat3(), mark.getPreUniv(), mark.getIntMarks(),
                    mark.getUniMarks(), mark.getClearedMonthYear(), mark.getSource().name()
            );
            grouped.computeIfAbsent(subject.getSemesterNumber(), k -> new ArrayList<>()).add(view);
        }
        // stable order within a semester: by subject display order (subjectId as fallback proxy is
        // not reliable, so re-sort by subjectCode which encodes the printed order in seed data)
        grouped.values().forEach(list -> list.sort(Comparator.comparing(SubjectMarkView::subjectCode)));
        return grouped;
    }

    public StudentDashboardResponse buildDashboard(String regNo) {
        Student student = requireByRegNo(regNo);
        Map<Integer, List<SubjectMarkView>> marksBySemester = marksBySemester(student);

        Map<Integer, Double> gpaBySemester = new TreeMap<>();
        for (var entry : marksBySemester.entrySet()) {
            gpaBySemester.put(entry.getKey(), gpaCalculator.gpaForSemester(entry.getValue()));
        }
        Map<Integer, Double> cgpaBySemester = new TreeMap<>();
        for (int semester : marksBySemester.keySet()) {
            cgpaBySemester.put(semester, gpaCalculator.cgpaThroughSemester(semester, marksBySemester));
        }

        List<Document> documents = documentRepository.findByStudent(student);
        Map<DocumentType, Document> byType = documents.stream()
                .collect(Collectors.toMap(Document::getType, d -> d));

        List<DocumentStatusView> statuses = Arrays.stream(DocumentType.values())
                .map(type -> {
                    Document doc = byType.get(type);
                    return new DocumentStatusView(type, doc != null, doc != null && doc.isConfirmed());
                })
                .toList();

        boolean downloadReady = !marksBySemester.isEmpty();

        return new StudentDashboardResponse(
                student.getRegNo(), student.getName(), marksBySemester, gpaBySemester, cgpaBySemester,
                statuses, downloadReady
        );
    }
}
