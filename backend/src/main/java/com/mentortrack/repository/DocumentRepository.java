package com.mentortrack.repository;

import com.mentortrack.domain.Document;
import com.mentortrack.domain.DocumentType;
import com.mentortrack.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStudent(Student student);

    Optional<Document> findByStudentAndType(Student student, DocumentType type);
}
