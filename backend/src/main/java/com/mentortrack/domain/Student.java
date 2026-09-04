package com.mentortrack.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reg_no", nullable = false, unique = true, length = 32)
    private String regNo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "force_password_change", nullable = false)
    private boolean forcePasswordChange = true;

    @Column(name = "name")
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "blood_group", length = 8)
    private String bloodGroup;

    @Column(name = "hobbies")
    private String hobbies;

    @Column(name = "games")
    private String games;

    @Column(name = "literary")
    private String literary;

    @Column(name = "community")
    private String community;

    @Column(name = "percentage_10th")
    private Double percentage10th;

    @Column(name = "percentage_12th")
    private Double percentage12th;

    @Column(name = "percentage_diploma")
    private Double percentageDiploma;

    // Additional fields needed to fully populate the real Mentor Card template header
    // (present on the physical form but not explicitly listed in the base data model).
    @Column(name = "mentor_name")
    private String mentorName;

    @Column(name = "admitted_on")
    private String admittedOn;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mark> marks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getGames() {
        return games;
    }

    public void setGames(String games) {
        this.games = games;
    }

    public String getLiterary() {
        return literary;
    }

    public void setLiterary(String literary) {
        this.literary = literary;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public Double getPercentage10th() {
        return percentage10th;
    }

    public void setPercentage10th(Double percentage10th) {
        this.percentage10th = percentage10th;
    }

    public Double getPercentage12th() {
        return percentage12th;
    }

    public void setPercentage12th(Double percentage12th) {
        this.percentage12th = percentage12th;
    }

    public Double getPercentageDiploma() {
        return percentageDiploma;
    }

    public void setPercentageDiploma(Double percentageDiploma) {
        this.percentageDiploma = percentageDiploma;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getAdmittedOn() {
        return admittedOn;
    }

    public void setAdmittedOn(String admittedOn) {
        this.admittedOn = admittedOn;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public List<Mark> getMarks() {
        return marks;
    }
}
