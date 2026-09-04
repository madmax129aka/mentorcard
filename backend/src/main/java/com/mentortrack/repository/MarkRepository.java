package com.mentortrack.repository;

import com.mentortrack.domain.Mark;
import com.mentortrack.domain.Student;
import com.mentortrack.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {
    List<Mark> findByStudent(Student student);

    List<Mark> findByStudentId(Long studentId);

    Optional<Mark> findByStudentAndSubject(Student student, Subject subject);
}
