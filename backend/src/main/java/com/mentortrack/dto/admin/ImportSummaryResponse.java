package com.mentortrack.dto.admin;

import java.util.List;

public record ImportSummaryResponse(
        Long importBatchId,
        String filename,
        int totalRows,
        int matchedCount,
        int unmatchedCount,
        List<String> unmatchedRegNos
) {
}
