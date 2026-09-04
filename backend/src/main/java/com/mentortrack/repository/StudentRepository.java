package com.mentortrack.repository;

import com.mentortrack.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegNo(String regNo);

    boolean existsByRegNo(String regNo);

    java.util.List<Student> findByRegNoContainingIgnoreCase(String regNoFragment);
}
