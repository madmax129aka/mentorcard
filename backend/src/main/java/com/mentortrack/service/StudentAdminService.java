package com.mentortrack.service;

import com.mentortrack.domain.Student;
import com.mentortrack.dto.admin.CreateStudentRequest;
import com.mentortrack.dto.admin.StudentSummaryResponse;
import com.mentortrack.exception.BadRequestException;
import com.mentortrack.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentAdminService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentAdminService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentSummaryResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByRegNo(request.getRegNo())) {
            throw new BadRequestException("A student with reg no " + request.getRegNo() + " already exists");
        }
        Student student = new Student();
        student.setRegNo(request.getRegNo());
        student.setName(request.getName());
        // Default password = register number, per spec; forced change on first login.
        student.setPasswordHash(passwordEncoder.encode(request.getRegNo()));
        student.setForcePasswordChange(true);
        studentRepository.save(student);
        return toSummary(student);
    }

    public List<StudentSummaryResponse> search(String regNoFragment) {
        List<Student> students = (regNoFragment == null || regNoFragment.isBlank())
                ? studentRepository.findAll()
                : studentRepository.findByRegNoContainingIgnoreCase(regNoFragment);
        return students.stream().map(this::toSummary).toList();
    }

    private StudentSummaryResponse toSummary(Student student) {
        return new StudentSummaryResponse(student.getId(), student.getRegNo(), student.getName(), student.isForcePasswordChange());
    }
}
