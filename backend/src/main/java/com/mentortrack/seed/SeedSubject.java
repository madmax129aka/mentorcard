package com.mentortrack.seed;

/** One row of seed subject data: (semester, subjectCode, name, displayOrder). */
public record SeedSubject(int semester, String subjectCode, String name, int displayOrder) {
}
