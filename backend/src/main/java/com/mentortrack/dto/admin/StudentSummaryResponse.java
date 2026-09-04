package com.mentortrack.dto.admin;

public record StudentSummaryResponse(Long id, String regNo, String name, boolean forcePasswordChange) {
}
