package com.mentortrack.repository;

import com.mentortrack.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySemesterNumberOrderByDisplayOrderAsc(int semesterNumber);

    List<Subject> findAllByOrderBySemesterNumberAscDisplayOrderAsc();

    Optional<Subject> findBySemesterNumberAndSubjectCode(int semesterNumber, String subjectCode);
}
