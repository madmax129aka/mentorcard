package com.mentortrack.service;

import com.mentortrack.dto.dashboard.SubjectMarkView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Computes semester GPA and running CGPA from University (Uni.) marks.
 *
 * <strong>IMPORTANT — formula not yet verified against the university's official grading
 * policy.</strong> The spec brief calls for GPA/CGPA to be "verified against real data before
 * being considered done", but no official grade-point scale, subject credit weights, or CGPA
 * aggregation rule were provided as part of this build. Subject credit weights in particular are
 * not part of the current data model (Subject has no `credits` field), so this implementation uses
 * an unweighted average of per-subject grade points as a reasonable placeholder:
 *
 * <ol>
 *   <li>Each subject's Uni. Marks (assumed to be out of 100) is converted to a 10-point grade
 *       point using a standard absolute grading table (91-100=10, 81-90=9, ... below 35=0).</li>
 *   <li>Semester GPA = unweighted mean of that semester's subject grade points.</li>
 *   <li>CGPA (as shown on the PDF for semester N) = mean of the GPAs of semesters 1..N.</li>
 * </ol>
 *
 * Before this is used for a real signed Mentor Card, replace {@link #gradePointFor(double)} and
 * the CGPA aggregation in {@link #cgpaThroughSemester} with the department's actual formula (most
 * likely credit-weighted), and add a `credits` column to {@code Subject} if weighting is required.
 */
@Service
public class GpaCalculator {

    public Double gpaForSemester(List<SubjectMarkView> subjectMarks) {
        List<Double> gradePoints = subjectMarks.stream()
                .map(SubjectMarkView::uniMarks)
                .filter(m -> m != null)
                .map(this::gradePointFor)
                .toList();
        if (gradePoints.isEmpty()) {
            return null;
        }
        return gradePoints.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Cumulative GPA up to and including {@code uptoSemester}, given ALL of a student's marks
     * grouped by semester. Kept as a separate method (rather than a running total held on
     * {@link com.mentortrack.domain.Student}) so it is always recomputed from source marks.
     */
    public Double cgpaThroughSemester(int uptoSemester, Map<Integer, List<SubjectMarkView>> marksBySemester) {
        List<Double> semesterGpas = marksBySemester.entrySet().stream()
                .filter(e -> e.getKey() <= uptoSemester)
                .map(e -> gpaForSemester(e.getValue()))
                .filter(g -> g != null)
                .toList();
        if (semesterGpas.isEmpty()) {
            return null;
        }
        return semesterGpas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double gradePointFor(double uniMarks) {
        if (uniMarks >= 91) return 10;
        if (uniMarks >= 81) return 9;
        if (uniMarks >= 71) return 8;
        if (uniMarks >= 61) return 7;
        if (uniMarks >= 51) return 6;
        if (uniMarks >= 41) return 5;
        if (uniMarks >= 35) return 4;
        return 0;
    }
}
