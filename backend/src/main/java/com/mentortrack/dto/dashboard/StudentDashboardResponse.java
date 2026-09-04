package com.mentortrack.dto.dashboard;

import java.util.List;
import java.util.Map;

public record StudentDashboardResponse(
        String regNo,
        String name,
        Map<Integer, List<SubjectMarkView>> marksBySemester,
        Map<Integer, Double> gpaBySemester,
        Map<Integer, Double> cgpaBySemester,
        List<DocumentStatusView> documents,
        boolean downloadReady
) {
}
