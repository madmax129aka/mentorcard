package com.mentortrack.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "marks", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id"}))
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "cat1")
    private Double cat1;

    @Column(name = "cat2")
    private Double cat2;

    @Column(name = "cat3")
    private Double cat3;

    @Column(name = "pre_univ")
    private Double preUniv;

    @Column(name = "int_marks")
    private Double intMarks;

    @Column(name = "uni_marks")
    private Double uniMarks;

    @Column(name = "cleared_month_year", length = 16)
    private String clearedMonthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private MarkSource source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Double getCat1() {
        return cat1;
    }

    public void setCat1(Double cat1) {
        this.cat1 = cat1;
    }

    public Double getCat2() {
        return cat2;
    }

    public void setCat2(Double cat2) {
        this.cat2 = cat2;
    }

    public Double getCat3() {
        return cat3;
    }

    public void setCat3(Double cat3) {
        this.cat3 = cat3;
    }

    public Double getPreUniv() {
        return preUniv;
    }

    public void setPreUniv(Double preUniv) {
        this.preUniv = preUniv;
    }

    public Double getIntMarks() {
        return intMarks;
    }

    public void setIntMarks(Double intMarks) {
        this.intMarks = intMarks;
    }

    public Double getUniMarks() {
        return uniMarks;
    }

    public void setUniMarks(Double uniMarks) {
        this.uniMarks = uniMarks;
    }

    public String getClearedMonthYear() {
        return clearedMonthYear;
    }

    public void setClearedMonthYear(String clearedMonthYear) {
        this.clearedMonthYear = clearedMonthYear;
    }

    public MarkSource getSource() {
        return source;
    }

    public void setSource(MarkSource source) {
        this.source = source;
    }
}
