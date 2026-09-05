# Mentor Card PDF — Field Coordinate Map

This document records how the fixed x/y coordinates used by the PDFBox overlay
generator (`MentorCardPdfService` / `MentorCardCoordinates`) were derived from
the **real template files already in this repo**:

- `Mentor Card Printout - CSE-1 2024.pdf` — page 1 of 2 (Header/personal
  details + Semester I & II mark tables + Semester III & IV mark tables)
- `Mentor Card Printout - CSE-2 2024.pdf` — page 2 of 2 (Semester V & VI mark
  tables + Semester VII & VIII mark tables + Discipline/Grading/Degree footer)

Both PDFs use `MediaBox [0 0 595.44 841.92]` (A4, PDF points, origin
bottom-left — this matches PDFBox's coordinate system directly, no flipping
needed).

## Revision note

An earlier version of this document (and of `MentorCardCoordinates.java`)
computed each subject row's y-position as "the row's grid-top boundary minus
a fixed 6.5pt offset." That approach was **wrong** and has been replaced. Two
concrete bugs it caused, found and fixed during a re-verification pass:

1. **Off-by-one-row from the header.** In 3 of the 4 semester-table blocks on
   page 1, the row-boundary array included the `SUBJECT NAME` header row's
   own boundary as if it were the first data row, shifting every subject's
   marks up into the row above the one they actually belonged to.
2. **Fixed offset breaks on wrapped subject names.** Row heights vary a lot —
   some subject names print on one line, others wrap to two or three — so a
   flat "6.5pt below the top" rule put the mark values anywhere from 5.3pt to
   13.9pt below where they needed to be, depending on how many lines that
   row's subject name happened to wrap to.

Both are now fixed by anchoring every mark value directly to **the exact
baseline y-coordinate where that specific subject's name is already printed**
on the template (see "Corrected derivation method" below), and by looking
up that baseline **by subject code**, not by list position — so a student
missing a Mark row for one subject no longer shifts every later subject's
values into the wrong row (see `MentorCardCoordinates.SemesterLayout` and how
`MentorCardPdfService.fillSemesterTable` uses it).

## Corrected derivation method (no visual PDF renderer was available in this sandbox)

The sandbox had no internet access to Maven Central/npm/pip mirrors and no
`poppler`/`ghostscript`/`ImageMagick` binaries, so the template could not be
rasterized to an image to eyeball pixel positions. The coordinates below were
instead derived and **cross-checked against the template's own printed
content** (the ultimate ground truth for where things must line up):

1. Decompressed each page's `/Contents` stream (FlateDecode).
2. Parsed every `BT ... Tm ... Tj/TJ ... ET` text-showing block to recover the
   exact baseline `(x, y)` of every string already printed on the form —
   including every subject name, every label, and (critically) a few
   pre-printed fill-in **hints** the template itself prints inside otherwise
   blank cells (e.g. `"ddmm"` / `"yyyy"` inside the Date-of-Birth cell).
3. Parsed every filled rectangle (`... re f`) used to draw the form's grid,
   grouped by shared x (vertical dividers) or shared y (horizontal dividers),
   to recover the exact row/column boundaries independent of text.
4. For every semester's mark table: matched each of that semester's grid rows
   against the **actual printed subject name found in it** (not just counted
   rows top-to-bottom), confirming the row count and content against the
   seed subject list one row at a time. This surfaced two real anomalies in
   the template (see below) that a purely positional row count would have
   silently mismatched.
5. For header fields with a pre-printed fill-in hint (DOB), used that hint's
   exact baseline directly — no estimation involved. For header fields
   without one (Reg No, Name, Hobbies, %10th/12th/Diploma, etc.), used the
   verified baseline of the *label itself* when the value is meant to be
   written on the same line as the label (confirmed by finding a stray
   trailing space character printed immediately after each such label,
   indicating the template's own text flow expected more content on that
   same line), or the row's own grid-cell boundaries when the value belongs
   on a separate line below the label.

### Two real anomalies found in the template during verification

- **Semester III's grid has 10 rows for 9 real subjects.** The first row
  contains only a single stray "D" glyph (visible on the printed form itself,
  most likely a leftover from template editing) with no associated subject —
  this row is skipped entirely; Semester III's 9 subjects start at the next
  row down.
- **Semester VIII has only 4 real subjects spread across an 11-row grid with
  several genuinely blank rows.** The 4 real baselines were located by
  matching each subject's actual printed text (e.g. "PROJECT" / "PHASE II"
  wrapping across two grid rows for the single subject "Project Phase II"),
  not by counting grid rows sequentially.

## Confidence levels, by field group

- **Verified against printed content (highest confidence):** every Semester
  I–VIII CAT/Pre-Univ/Int/Uni-Marks/Cleared-in row baseline — each one is the
  exact baseline of that specific subject's own printed name, cross-checked
  against the seed subject list. All 8 semesters' subject counts were
  confirmed to match their baseline counts exactly (8/8/9/11/10/10/10/4).
- **Verified against printed content:** all mark-table column x-positions
  (read directly off vertical grid-line rectangles, unchanged from the
  original derivation — these were never the source of the misalignment).
- **Verified against a pre-printed hint:** the Reg No / Name / DOB / Admitted
  On / Blood Group row — the template itself prints `"ddmm"` / `"yyyy"`
  inside the DOB cell at y=694.75, used directly as the row's baseline.
- **Verified against the label's own baseline:** Hobbies/Games/Literary/
  Community (row baseline y=675.79) and %10th/%12th/%Diploma (row baseline
  y≈639) — confirmed by finding a stray trailing space glyph printed
  immediately after each label, indicating the value is meant to continue on
  that same text line.
- **Estimated, not directly verified (spot-check before relying on it):** the
  Mentor Name field only. Its fill-in cell has no pre-printed hint text of
  its own to verify a baseline against (unlike every other field above). See
  the javadoc on `MentorCardCoordinates.MENTOR_NAME_Y` for the exact caveat.

## Recommended verification step (do before real department sign-off use)

`MentorCardPdfService` centralizes **every** coordinate in one place:
`MentorCardCoordinates.java`. Generate one sample PDF, print/view it, and
nudge any `x`/`y` value in that single file if needed — no other code needs
to change. Given the re-verification above, only `MENTOR_NAME_Y` is expected
to need adjustment; everything else was checked against the template's own
printed content, not estimated.

## Page 1 — Header / personal details (all coordinates in PDF points, origin bottom-left)

| Field | x | y (baseline) | Verification |
|---|---|---|---|
| Name of Mentor (Sem I & II) | 30 | 728 | Estimated — see caveat above |
| Name of Mentor (Sem III–VIII) | 337 | 728 | Estimated — see caveat above |
| Register Number | 30 | 694.75 | Verified: DOB-cell "ddmm yyyy" hint on same row |
| Name of the Student | 172 | 694.75 | Verified: same row |
| Date of Birth (dd/mm/yyyy) | 337 | 694.75 | Verified: exact baseline of the printed "ddmm"/"yyyy" hint |
| Admitted on (MM/YYYY) | 412 | 694.75 | Verified: same row |
| Blood Group | 486 | 694.75 | Verified: same row |
| Hobbies | 75 | 675.79 | Verified: exact baseline of the printed "Hobbies" label |
| Games | 335 | 675.79 | Verified: exact baseline of the printed "Games" label |
| Literary | 415 | 675.79 | Verified: exact baseline of the printed "Literary" label |
| Community | 535 | 675.79 | Verified: exact baseline of the printed "Community" label |
| % in 10th | 78 | 639.07 | Verified: exact baseline of the printed "%in10th*" label |
| % in 12th | 244 | 639.07 | Verified: exact baseline of the printed "%in12th*" label |
| % in Diploma | 404 | 639.07 | Verified: exact baseline of the printed "% in Diploma*" label |
| Photo | (attach physically — no overlay) | — | template already has "Affix stamp size photo" box, left blank |

## Page 1 — Semester table grid (CAT/marks columns), Semesters I–IV

Column x-positions (read directly off vertical grid-line rectangles —
unchanged from the original derivation, never the source of the bug):

### Semester I (left column set) / Semester III (left column set, lower block)
| Column | x (Sem I) | x (Sem III) |
|---|---|---|
| CAT1 | 111.65 | 111.65 |
| CAT2 | 133.49 | 144.05 |
| Pre-Univ | 156.79 | 171.67 |
| Int. Marks | 181.75 | 200.23 |
| Uni. Marks | 211.30 | 227.38 |
| Cleared in (MM/YYYY) | 254.74 | 254.74 |

### Semester II (right column set) / Semester IV (right column set)
| Column | x (Sem II) | x (Sem IV) |
|---|---|---|
| CAT1 | 372.38 | 372.38 |
| CAT2 | 394.97 | 399.29 |
| Pre-Univ | 419.93 | 424.73 |
| Int. Marks | 442.73 | 448.75 |
| Uni. Marks | 473.47 | 473.47 |
| Cleared in (MM/YYYY) | 506.86 | 501.07 |

> The template has a minor column-width difference between the Sem I/II block
> and the Sem III/IV block. Both sets are captured verbatim in
> `MentorCardCoordinates.java` rather than assumed identical, to stay
> faithful to the actual template.

### Row baselines — Semester I (8 subjects, in CS101–CS108 order)
`468.14, 452.04, 436.44, 420.36, 407.64, 387.48, 368.74, 354.10`
— each value is the exact baseline of that subject's own printed name
(Technical English I, Mathematics I, Engg. Physics I, Engg. Chemistry I,
Basic Electrical & Electronics Engg., Basic Mechanical & Civil Engg., C
Programming and MS Office Tools, Orientation to Entrepreneurship & Project
Lab).

### Row baselines — Semester II (8 subjects, in CS201–CS208 order)
`469.10, 452.76, 436.68, 420.60, 407.16, 389.40, 373.78, 355.06`

### Row baselines — Semester III (9 subjects, in CS301–CS309 order)
`235.99, 222.79, 206.93, 189.89, 174.77, 151.97, 134.93, 114.02, 103.46`
— note: the template's grid has a 10th row above these (top≈262.39–246.07)
containing only a stray "D" glyph with no subject; that row is intentionally
excluded.

### Row baselines — Semester IV (11 subjects, in CS401–CS411 order)
`254.71, 238.87, 219.91, 208.61, 192.77, 174.77, 156.77, 140.45, 116.66, 102.02, 88.10`

GPA/CGPA labels for Sem I/II block: `"GPA:"` at (125.33, 329.14), `"CGPA:"` at
(203.35, 329.14) — value drawn 26pt to the right of each label's own x, same
baseline. Sem III/IV block: `"GPA:"`/`"CGPA:"` at (125.81, 70.82) and
(204.79, 70.82) for Sem III; (395.93, 70.34) and (471.31, 70.34) for Sem IV.

## Page 2 — Semester table grid, Semesters V–VIII

Column x-positions (Sem V/VII left set): `124.37 (CAT1), 153.19 (CAT2), 180.55
(Pre-Univ), 209.62 (Int. Marks), 233.38 (Uni. Marks), 259.30 (Cleared in)`

Column x-positions (Sem VI/VIII right set): `392.81 (CAT1), 420.17 (CAT2),
445.39 (Pre-Univ), 471.55 (Int. Marks), 493.63 (Uni. Marks), 521.02 (Cleared in)`

### Row baselines — Semester V (10 subjects, in CS501–CS510 order)
`681.31, 658.75, 643.87, 623.21, 605.93, 593.45, 572.09, 554.81, 532.70, 515.66`

### Row baselines — Semester VI (10 subjects, in CS601–CS610 order)
`681.31, 662.11, 640.51, 623.21, 605.93, 593.45, 570.65, 553.37, 532.70, 515.66`

### Row baselines — Semester VII (10 subjects, in CS701–CS710 order)
`407.16, 390.12, 376.66, 362.50, 348.10, 334.18, 323.62, 302.26, 283.99, 269.83`

### Row baselines — Semester VIII (only 4 real subjects, in CS801–CS804 order)
`414.12, 386.76, 366.10, 337.54`
— note: this semester's grid has 11 rows but only 4 are real subjects; the
rest are genuinely blank on the printed form. These 4 baselines were located
by matching each subject's own printed text directly (including "Project
Phase II", which wraps across two grid rows as "PROJECT" / "PHASE II").

GPA/CGPA labels: Sem V/VI block at (120.05, 498.62)/(195.67, 498.62) for Sem
V, (398.09, 498.62)/(484.51, 498.62) for Sem VI. Sem VII/VIII block at
(113.81, 254.47)/(225.94, 254.47) for Sem VII, (406.49, 253.27)/(489.79,
253.27) for Sem VIII.

## Page 2 — Footer (Discipline / Extra-Curricular / Attendance / Overall Grading)

A single 8-column grid (`1`–`8`, one per semester) starts at row y=201.41
("Discipline"), 188.93 ("Extra-Curricular Activities"), 178.37
("Co-Curricular Activities"), 167.57 ("Attendance"), 156.77 ("Average"),
145.97 ("Over All Grading"). Column x header positions: `142.85, 165.67,
193.51, 221.86, 245.62, 268.92, 294.12, 322.68` (Semesters 1–8) plus a
"Remarks" column at x=435.29. These are **out of scope for this build**
(manual/mentor-filled fields per the spec — not part of the automated
overlay), left blank on the generated PDF for physical fill-in.

## Subject names per semester (as printed on the real template)

Used verbatim as the seed `Subject` rows so the demo dashboard/PDF show the
university's actual CSE curriculum, not placeholder names. Confirmed during
this verification pass to match, subject-for-subject and in order, the row
baselines listed above for every semester.

**Semester I:** Technical English I; Mathematics I; Engg. Physics I; Engg.
Chemistry I; Basic Electrical & Electronics Engg.; Basic Mechanical & Civil
Engg.; C Programming and MS Office Tools; Orientation to Entrepreneurship &
Project Lab

**Semester II:** Mathematics II; Solid State Physics; Technical Chemistry;
Engineering Graphics; Fundamentals of Computer Engineering; Communicative
English Lab; Python Programming; Environmental Science (Audit Course)

**Semester III:** Data Structures; Database Management System; Digital
Principles and System Design; Basic Electrical Engineering; Universal Human
Values: Understanding Harmony; Data Structures Lab; Database Management
System Lab; Digital Systems Lab; Object Oriented Programming with C++

**Semester IV:** Statistics for Computer Engineers; Design and Analysis of
Algorithms; Operating System; Microprocessor and Microcontrollers; The Indian
Constitution / The Indian Traditional Knowledge (Audit Course); Microprocessor
and Microcontrollers Lab; Design and Analysis of Algorithms Lab; Operating
System Lab; Java Programming; Technical Skill I; Soft Skill I - Employability
Skills

**Semester V:** Computer Organization and Architecture; Computer Networks;
Principles of Compiler Design; Program Elective I; Open Elective I; Online
Course (NPTEL/SWAYAM/Any MOOC approved by AICTE/UGC); Network Programming
Lab; Compiler Design Lab; User Experience Design; Technical Skill II

**Semester VI:** Object Oriented Software Engineering; Web Design using PHP &
MySQL; Artificial Intelligence; Program Elective II; Open Elective II; Object
Oriented Software Engineering Lab; Web Design using PHP & MySQL Lab; Soft
Skill II - Qualitative and Quantitative Skills; Technical Skill III; Mini
Project/Internship

**Semester VII:** Big Data Analytics; Program Elective III; Connected
Business (elective); Cloud Computing; Machine Learning; Open Lab; Data
Analytics Lab using Machine Learning Algorithms; Cloud Computing Lab; Project
Phase I; Foreign Language

**Semester VIII:** Principles of Management and Behavioral Science; Program
Elective IV; Program Elective V; Project Phase II

> A handful of subject-name fragments in the source PDF's content stream are
> visually truncated by the template's column width (e.g. "Connected
> Business" has no visible elective suffix on the printed form itself) — these
> are transcribed as printed, not expanded/guessed.

## Full raw extraction data

The raw text-run and grid-line extraction dumps used to build this map are
kept for future re-verification: `docs/mentorcard_layout1.txt` (page 1),
`docs/mentorcard_layout2.txt` (page 2).
