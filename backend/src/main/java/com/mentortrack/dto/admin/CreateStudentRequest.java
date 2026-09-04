package com.mentortrack.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class CreateStudentRequest {

    @NotBlank
    private String regNo;

    @NotBlank
    private String name;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
