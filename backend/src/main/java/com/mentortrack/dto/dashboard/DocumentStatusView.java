package com.mentortrack.dto.dashboard;

import com.mentortrack.domain.DocumentType;

public record DocumentStatusView(DocumentType type, boolean uploaded, boolean confirmed) {
}
