package com.mentortrack.pdf;

import java.util.Map;

/**
 * Fixed x/y coordinates (PDF points, origin bottom-left) used to overlay student data onto the
 * real "Mentor Card" template PDFs already shipped in this repo:
 *   - resources/templates/mentor-card-page1.pdf  (page 1 of 2: header/personal details + Sem I-IV)
 *   - resources/templates/mentor-card-page2.pdf  (page 2 of 2: Sem V-VIII + discipline/grading footer)
 *
 * These values were derived by decompressing the templates' own PDF content streams and reading
 * back the label text-run positions and grid rectangle positions already drawn on the university's
 * form (see /docs/PDF_FIELD_MAP.md for the full derivation method and raw extraction data). This
 * sandbox had no PDF rasterizer available to visually confirm pixel placement, so ALL tunable
 * coordinates are centralized here: if a printed sample needs a small nudge, change values in this
 * file only, nowhere else.
 */
public final class MentorCardCoordinates {

    private MentorCardCoordinates() {
    }

    // ================= Page 1: header / personal details =================

    public static final float MENTOR_NAME_SEM1_2_X = 116f;
    public static final float MENTOR_NAME_SEM3_8_X = 375f;
    public static final float MENTOR_NAME_Y = 738.0f;

    public static final float REG_NO_X = 72f;
    public static final float STUDENT_NAME_X = 292f;
    public static final float DOB_X = 345f;
    public static final float ADMITTED_ON_X = 411f;
    public static final float BLOOD_GROUP_X = 485f;
    public static final float HEADER_ROW1_Y = 709.5f;

    public static final float HOBBIES_X = 33f;
    public static final float GAMES_X = 291f;
    public static final float LITERARY_X = 380f;
    public static final float COMMUNITY_X = 486f;
    public static final float HOBBIES_ROW_Y = 673.0f;

    public static final float PCT_10TH_X = 85f;
    public static final float PCT_12TH_X = 258f;
    public static final float PCT_DIPLOMA_X = 411f;
    public static final float PERCENTAGE_ROW_Y = 637.0f;

    public static final float FONT_SIZE_HEADER = 9f;
    public static final float FONT_SIZE_MARKS = 6.5f;

    /** Vertical offset applied below a row's "top" gridline to place the baseline of drawn text. */
    public static final float ROW_TEXT_BASELINE_OFFSET = 6.5f;

    /** Horizontal distance from a "GPA:"/"CGPA:" label's start x to where the value is drawn. */
    public static final float GPA_VALUE_OFFSET_X = 26f;

    /** Column x-offsets for a 6-column semester mark table (CAT1, CAT2, PreUniv, Int, Uni, ClearedIn). */
    public record MarkColumns(float cat1, float cat2, float preUniv, float intMarks, float uniMarks, float clearedIn) {
    }

    /** Everything needed to overlay one semester's table: which page, its columns, row tops, and GPA/CGPA label positions. */
    public record SemesterLayout(int page, MarkColumns columns, float[] rowTops,
                                  float gpaLabelX, float cgpaLabelX, float gpaRowY) {
    }

    // ---- Page 1: Semester I (left block) ----
    private static final float[] SEM_1_2_ROW_TOPS = {
            491.66f, 479.18f, 462.84f, 446.76f, 430.68f, 414.60f, 397.80f, 379.06f, 360.34f, 338.98f
    };

    // ---- Page 1: Semester III/IV row tops ----
    private static final float[] SEM_3_4_ROW_TOPS = {
            274.63f, 262.39f, 246.07f, 229.99f, 214.15f, 198.05f, 180.05f, 162.05f, 145.97f, 122.18f, 110.18f, 95.30f
    };

    // ---- Page 2: Semester V/VI row tops (from the template's own grid lines, 10 subject rows) ----
    private static final float[] SEM_5_6_ROW_TOPS = {
            690.43f, 670.51f, 651.55f, 634.27f, 616.97f, 599.69f, 578.33f, 561.05f, 543.74f, 526.46f
    };

    // ---- Page 2: Semester VII/VIII row tops (from the template's own grid lines, 10 subject rows) ----
    private static final float[] SEM_7_8_ROW_TOPS = {
            438.36f, 421.08f, 399.00f, 386.04f, 372.34f, 357.70f, 343.54f, 329.86f, 308.26f, 293.11f
    };

    private static final Map<Integer, SemesterLayout> LAYOUTS = Map.ofEntries(
            Map.entry(1, new SemesterLayout(1,
                    new MarkColumns(114f, 136f, 159f, 184f, 214f, 257f),
                    SEM_1_2_ROW_TOPS, 125.33f, 203.35f, 329.14f)),
            Map.entry(2, new SemesterLayout(1,
                    new MarkColumns(375f, 398f, 423f, 446f, 476f, 504f),
                    SEM_1_2_ROW_TOPS, 393.53f, 468.91f, 329.14f)),
            Map.entry(3, new SemesterLayout(1,
                    new MarkColumns(125f, 147f, 174f, 203f, 230f, 257f),
                    SEM_3_4_ROW_TOPS, 125.81f, 204.79f, 70.82f)),
            Map.entry(4, new SemesterLayout(1,
                    new MarkColumns(402f, 427f, 451f, 476f, 504f, 524f),
                    SEM_3_4_ROW_TOPS, 395.93f, 471.31f, 70.34f)),
            Map.entry(5, new SemesterLayout(2,
                    new MarkColumns(126f, 155f, 183f, 212f, 236f, 262f),
                    SEM_5_6_ROW_TOPS, 120.05f, 195.67f, 498.62f)),
            Map.entry(6, new SemesterLayout(2,
                    new MarkColumns(395f, 423f, 448f, 474f, 496f, 524f),
                    SEM_5_6_ROW_TOPS, 398.09f, 484.51f, 498.62f)),
            Map.entry(7, new SemesterLayout(2,
                    new MarkColumns(126f, 155f, 183f, 212f, 236f, 262f),
                    SEM_7_8_ROW_TOPS, 113.81f, 225.94f, 254.47f)),
            Map.entry(8, new SemesterLayout(2,
                    new MarkColumns(395f, 423f, 448f, 474f, 496f, 524f),
                    SEM_7_8_ROW_TOPS, 406.49f, 489.79f, 253.27f))
    );

    public static SemesterLayout layoutFor(int semesterNumber) {
        SemesterLayout layout = LAYOUTS.get(semesterNumber);
        if (layout == null) {
            throw new IllegalArgumentException("No mark-table layout defined for semester " + semesterNumber);
        }
        return layout;
    }
}
