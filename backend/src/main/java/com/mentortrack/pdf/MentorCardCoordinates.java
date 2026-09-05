package com.mentortrack.pdf;

import java.util.Map;

/**
 * Fixed x/y coordinates (PDF points, origin bottom-left) used to overlay student data onto the
 * real "Mentor Card" template PDFs already shipped in this repo:
 *   - resources/templates/mentor-card-page1.pdf  (page 1 of 2: header/personal details + Sem I-IV)
 *   - resources/templates/mentor-card-page2.pdf  (page 2 of 2: Sem V-VIII + discipline/grading footer)
 *
 * <h2>How every coordinate below was derived and verified</h2>
 * Each template's own PDF content stream was decompressed and parsed for (1) every already-printed
 * text run's exact baseline (x, y) and (2) every grid rectangle (row/column divider line). Every
 * per-semester baseline map below (e.g. {@link #SEM_1_BASELINES}) gives, for each subject code, the
 * exact y-coordinate where that specific subject's own name is already printed on the form — not a
 * computed offset from a grid line. This matters because an earlier version of this file computed
 * each row's baseline as "grid-top minus a fixed 6.5pt", which silently broke for any row whose
 * subject name wraps onto 2-3 lines (the fixed
 * offset put values above or below the correct row depending on how many lines the name used) and
 * additionally mis-assigned an entire block's rows by one because a header-row boundary was counted
 * as if it were the first data row. Anchoring directly to each subject's own printed baseline avoids
 * both classes of error, since it does not depend on row height or row-counting at all.
 *
 * A further correctness fix: rows are looked up by {@code subjectCode}, not by list position/index.
 * The previous version wrote a semester's marks in whatever order they happened to come back from
 * the database, so a student missing a Mark row for any one subject would shift every subsequent
 * subject's values up by one row. {@link SemesterLayout#baselineFor(String)} looks up the baseline
 * for the subject code directly, so a missing subject simply leaves its own row blank.
 *
 * Two real anomalies in the template itself were found and accounted for during verification:
 * <ul>
 *   <li>Semester III's grid has one extra row (10 grid rows for 9 real subjects) containing only a
 *       stray single "D" glyph with no associated subject — this row is skipped entirely.</li>
 *   <li>Semester VIII's 4 real subjects are spread across non-contiguous grid rows within an
 *       11-row grid (several rows are genuinely blank on the printed form) — the 4 real baselines
 *       were located by matching each subject's actual printed text, not by counting rows.</li>
 * </ul>
 *
 * See /docs/PDF_FIELD_MAP.md for the full derivation method and raw extraction data.
 */
public final class MentorCardCoordinates {

    private MentorCardCoordinates() {
    }

    // ================= Page 1: header / personal details =================
    // Every value below is verified directly against printed content already on the template:
    // either a pre-printed fill-in hint (the DOB cell prints "ddmm yyyy" at y=694.75) or the exact
    // baseline of the field's own label (confirmed by a stray trailing space glyph printed right
    // after the label, indicating the value continues on that same text line). The one exception is
    // MENTOR_NAME_Y, which has no such hint to verify against — see its javadoc below.

    /**
     * "Name of the Mentor:" label baseline y=741.82; the fill-in cell is the row below it,
     * y=[736.54 (top), 722.14 (bottom)], height 14.4pt. UNLIKE every other coordinate in this file,
     * this cell has no pre-printed hint text to verify a baseline against (contrast with the DOB
     * cell's printed "ddmm yyyy" hint at y=694.75, used to verify {@link #HEADER_ROW1_Y} directly).
     * This value is a reasonable estimate (roughly centered in the cell, in line with how nearby
     * verified rows position text relative to their own cell bounds) but is NOT directly verified
     * against printed content the way every other value in this class is — spot-check this one
     * field specifically against a printed sample before relying on it.
     */
    public static final float MENTOR_NAME_SEM1_2_X = 30f; // left edge of the Sem I/II mentor-name cell (x=26.88-333.02)
    public static final float MENTOR_NAME_SEM3_8_X = 337f; // left edge of the Sem III-VIII mentor-name cell (x=333.74-555.58)
    public static final float MENTOR_NAME_Y = 728f; // ESTIMATED — see javadoc above; not printed-content-verified

    /** Reg No / Name of Student / DOB / Admitted on / Blood Group row: cell = [707.71 (top), 686.35 (bottom)]. */
    public static final float REG_NO_X = 30f;          // cell x=26.88-167.35
    public static final float STUDENT_NAME_X = 172f;    // cell x=168.79-333.02
    public static final float DOB_X = 337f;             // cell x=333.5-408.41; verified hint "ddmm yyyy" printed at x=350.54
    public static final float ADMITTED_ON_X = 412f;     // cell x=408.89-481.87
    public static final float BLOOD_GROUP_X = 486f;     // cell x=482.35-555.58
    public static final float HEADER_ROW1_Y = 694.75f;  // verified directly against the printed "ddmm"/"yyyy" hint baseline

    /** Hobbies / Games / Literary / Community: label row baseline y=675.79, same row as the value (write after the label). */
    public static final float HOBBIES_X = 75f;    // label "Hobbies" ends ~x=70 (cell x=26.88-274.68)
    public static final float GAMES_X = 335f;     // label "Games" at x=287.88 (cell x=275.4-332.78)
    public static final float LITERARY_X = 415f;  // label "Literary" at x=376.46 (cell x=333.5-455.47)
    public static final float COMMUNITY_X = 535f; // label "Community" at x=483.31 (cell x=456.19-555.58)
    public static final float HOBBIES_ROW_Y = 675.79f; // verified: exact baseline of the printed labels themselves

    /** % in 10th / % in 12th / % in Diploma: label row baseline y=639.07 (Diploma label measured at 638.59, same row). */
    public static final float PCT_10TH_X = 78f;     // label "%in10th*" ends ~x=74 (cell x=26.88-181.75)
    public static final float PCT_12TH_X = 244f;    // label "%in12th*" ends ~x=240 (cell x=182.47-254.74)
    public static final float PCT_DIPLOMA_X = 404f; // label "% in Diploma*" ends ~x=400 (cell x=333.5-408.17)
    public static final float PERCENTAGE_ROW_Y = 639.07f; // verified: exact baseline of the printed labels themselves

    public static final float FONT_SIZE_HEADER = 9f;
    public static final float FONT_SIZE_MARKS = 6.5f;

    /** Horizontal distance from a "GPA:"/"CGPA:" label's start x to where the value is drawn. */
    public static final float GPA_VALUE_OFFSET_X = 26f;

    /** Column x-offsets for a 6-column semester mark table (CAT1, CAT2, PreUniv, Int, Uni, ClearedIn). */
    public record MarkColumns(float cat1, float cat2, float preUniv, float intMarks, float uniMarks, float clearedIn) {
    }

    /**
     * Everything needed to overlay one semester's table: its columns, a baseline-y lookup keyed by
     * subject code (so a missing subject leaves only its own row blank instead of shifting every
     * later row), and GPA/CGPA label positions.
     */
    public record SemesterLayout(MarkColumns columns, Map<String, Float> rowBaselineBySubjectCode,
                                  float gpaLabelX, float cgpaLabelX, float gpaRowY) {
        public Float baselineFor(String subjectCode) {
            return rowBaselineBySubjectCode.get(subjectCode);
        }
    }

    private static Map<String, Float> baselines(String[] codes, float[] values) {
        if (codes.length != values.length) {
            throw new IllegalStateException("Subject code count (" + codes.length
                    + ") does not match baseline count (" + values.length + ")");
        }
        Map<String, Float> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < codes.length; i++) {
            map.put(codes[i], values[i]);
        }
        return Map.copyOf(map);
    }

    // ---- Semester I: 8 subjects, baseline = exact y of that subject's own printed name (top line if wrapped) ----
    private static final Map<String, Float> SEM_1_BASELINES = baselines(
            new String[]{"CS101", "CS102", "CS103", "CS104", "CS105", "CS106", "CS107", "CS108"},
            new float[]{468.14f, 452.04f, 436.44f, 420.36f, 407.64f, 387.48f, 368.74f, 354.10f});

    // ---- Semester II: 8 subjects ----
    private static final Map<String, Float> SEM_2_BASELINES = baselines(
            new String[]{"CS201", "CS202", "CS203", "CS204", "CS205", "CS206", "CS207", "CS208"},
            new float[]{469.10f, 452.76f, 436.68f, 420.60f, 407.16f, 389.40f, 373.78f, 355.06f});

    // ---- Semester III: 9 subjects (grid has a 10th row containing only a stray "D" glyph, skipped) ----
    private static final Map<String, Float> SEM_3_BASELINES = baselines(
            new String[]{"CS301", "CS302", "CS303", "CS304", "CS305", "CS306", "CS307", "CS308", "CS309"},
            new float[]{235.99f, 222.79f, 206.93f, 189.89f, 174.77f, 151.97f, 134.93f, 114.02f, 103.46f});

    // ---- Semester IV: 11 subjects ----
    private static final Map<String, Float> SEM_4_BASELINES = baselines(
            new String[]{"CS401", "CS402", "CS403", "CS404", "CS405", "CS406", "CS407", "CS408", "CS409", "CS410", "CS411"},
            new float[]{254.71f, 238.87f, 219.91f, 208.61f, 192.77f, 174.77f, 156.77f, 140.45f, 116.66f, 102.02f, 88.10f});

    // ---- Semester V: 10 subjects ----
    private static final Map<String, Float> SEM_5_BASELINES = baselines(
            new String[]{"CS501", "CS502", "CS503", "CS504", "CS505", "CS506", "CS507", "CS508", "CS509", "CS510"},
            new float[]{681.31f, 658.75f, 643.87f, 623.21f, 605.93f, 593.45f, 572.09f, 554.81f, 532.70f, 515.66f});

    // ---- Semester VI: 10 subjects ----
    private static final Map<String, Float> SEM_6_BASELINES = baselines(
            new String[]{"CS601", "CS602", "CS603", "CS604", "CS605", "CS606", "CS607", "CS608", "CS609", "CS610"},
            new float[]{681.31f, 662.11f, 640.51f, 623.21f, 605.93f, 593.45f, 570.65f, 553.37f, 532.70f, 515.66f});

    // ---- Semester VII: 10 subjects ----
    private static final Map<String, Float> SEM_7_BASELINES = baselines(
            new String[]{"CS701", "CS702", "CS703", "CS704", "CS705", "CS706", "CS707", "CS708", "CS709", "CS710"},
            new float[]{407.16f, 390.12f, 376.66f, 362.50f, 348.10f, 334.18f, 323.62f, 302.26f, 283.99f, 269.83f});

    // ---- Semester VIII: only 4 subjects, spread across non-contiguous grid rows (several rows in
    // this block are genuinely blank on the printed form) — baselines located by matching each
    // subject's actual printed text directly, not by counting rows. ----
    private static final Map<String, Float> SEM_8_BASELINES = baselines(
            new String[]{"CS801", "CS802", "CS803", "CS804"},
            new float[]{414.12f, 386.76f, 366.10f, 337.54f});

    private static final Map<Integer, SemesterLayout> LAYOUTS = Map.ofEntries(
            Map.entry(1, new SemesterLayout(
                    new MarkColumns(111.65f, 133.49f, 156.79f, 181.75f, 211.30f, 254.74f),
                    SEM_1_BASELINES, 125.33f, 203.35f, 329.14f)),
            Map.entry(2, new SemesterLayout(
                    new MarkColumns(372.38f, 394.97f, 419.93f, 442.73f, 473.47f, 506.86f),
                    SEM_2_BASELINES, 393.53f, 468.91f, 329.14f)),
            Map.entry(3, new SemesterLayout(
                    new MarkColumns(111.65f, 144.05f, 171.67f, 200.23f, 227.38f, 254.74f),
                    SEM_3_BASELINES, 125.81f, 204.79f, 70.82f)),
            Map.entry(4, new SemesterLayout(
                    new MarkColumns(372.38f, 399.29f, 424.73f, 448.75f, 473.47f, 501.07f),
                    SEM_4_BASELINES, 395.93f, 471.31f, 70.34f)),
            Map.entry(5, new SemesterLayout(
                    new MarkColumns(124.37f, 153.19f, 180.55f, 209.62f, 233.38f, 259.30f),
                    SEM_5_BASELINES, 120.05f, 195.67f, 498.62f)),
            Map.entry(6, new SemesterLayout(
                    new MarkColumns(392.81f, 420.17f, 445.39f, 471.55f, 493.63f, 521.02f),
                    SEM_6_BASELINES, 398.09f, 484.51f, 498.62f)),
            Map.entry(7, new SemesterLayout(
                    new MarkColumns(124.37f, 153.19f, 180.55f, 209.62f, 233.38f, 259.30f),
                    SEM_7_BASELINES, 113.81f, 225.94f, 254.47f)),
            Map.entry(8, new SemesterLayout(
                    new MarkColumns(392.81f, 420.17f, 445.39f, 471.55f, 493.63f, 521.02f),
                    SEM_8_BASELINES, 406.49f, 489.79f, 253.27f))
    );

    /** Which physical page (1 or 2) a semester's table is drawn on. */
    public static int pageFor(int semesterNumber) {
        return semesterNumber <= 4 ? 1 : 2;
    }

    public static SemesterLayout layoutFor(int semesterNumber) {
        SemesterLayout layout = LAYOUTS.get(semesterNumber);
        if (layout == null) {
            throw new IllegalArgumentException("No mark-table layout defined for semester " + semesterNumber);
        }
        return layout;
    }
}
