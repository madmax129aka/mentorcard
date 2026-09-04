# Mentor Card PDF — Field Coordinate Map

This document records how the fixed x/y coordinates used by the PDFBox overlay
generator (`MentorCardPdfService`) were derived from the **real template
files already in this repo**:

- `Mentor Card Printout - CSE-1 2024.pdf` — page 1 of 2 (Header/personal
  details + Semester I & II mark tables + Semester III & IV mark tables)
- `Mentor Card Printout - CSE-2 2024.pdf` — page 2 of 2 (Semester V & VI mark
  tables + Semester VII & VIII mark tables + Discipline/Grading/Degree footer)

Both PDFs use `MediaBox [0 0 595.44 841.92]` (A4, PDF points, origin
bottom-left — this matches PDFBox's coordinate system directly, no flipping
needed).

## Derivation method (no visual PDF renderer was available in this sandbox)

The sandbox had no internet access to Maven Central/npm/pip mirrors and no
`poppler`/`ghostscript`/`ImageMagick` binaries, so the template could not be
rasterized to an image to eyeball pixel positions. Instead the coordinates
below were derived directly from the PDF's own content stream (the
authoritative source of truth for where the printer places ink):

1. Decompressed the page's `/Contents` stream (FlateDecode) for each file.
2. Parsed every `BT ... Tm ... Tj/TJ ... ET` text-showing block to recover the
   exact baseline `(x, y)` of every label already printed on the form (e.g.
   `"CAT"`, `"SUBJECT NAME"`, `"Reg. No."`, `"GPA:"`), together with font size.
3. Parsed every filled rectangle (`... re f`) used to draw the form's grid —
   the thin (~0.5–2pt) rectangles are the row/column separator lines. Grouping
   these by shared x (vertical lines) or shared y (horizontal lines) recovers
   the exact table grid the printer draws, independent of the text labels.
4. Cross-referenced (2) and (3): a label's baseline sits inside a specific
   grid cell, and the corresponding **blank space** to fill in is the
   remainder of that same cell.

This gives *exact* coordinates for the table grid (highest confidence — used
directly for all CAT/Uni marks/Cleared-in fields) and *derived* coordinates
for the free-text header fields (name, reg no, DOB, etc. — inferred from
label position + cell width, medium confidence).

## Confidence levels, by field group

- **High confidence (grid-derived):** all Semester I–IV CAT/Pre-Univ/Int/Uni
  mark column x-positions and row y-positions on page 1 — read directly off
  the template's own vertical/horizontal grid-line rectangles.
- **High confidence (grid-derived):** Semester V–VIII mark column x-positions
  on page 2.
- **Medium confidence (grid-derived, row count approximate):** Semester V–VIII
  row y-positions on page 2. The page-2 content stream's grid lines for these
  two blocks include a couple of extra boundary lines beyond the 10 visible
  subject rows per block (likely a header/footer border), so the row-top
  array used in code takes the first N grid lines top-down, which lines up
  with the subject labels for the first several rows but should be spot
  checked against a printed sample for the last 1–2 rows of Semester VII/VIII.
- **Medium confidence (label-position-derived):** all page-1 header/personal
  fields (name, reg no, DOB, blood group, hobbies, percentages) — inferred
  from where the *label* sits plus the blank cell it labels, not from a filled
  sample form.

## Recommended verification step (do before real department sign-off use)

Because no rasterizer was available to visually confirm the header-field
placements pixel-for-pixel, `MentorCardPdfService` centralizes **every**
coordinate in one place: `MentorCardCoordinates.java`. Generate one sample
PDF, print/view it, and nudge any `x`/`y` values in that single file — no
other code needs to change. The semester-table coordinates (grid-derived) are
expected to need no adjustment; the personal-details header fields are the
ones most likely to need a small nudge.

## Page 1 — Header / personal details (all coordinates in PDF points, origin bottom-left)

| Field | x | y (baseline) | Notes |
|---|---|---|---|
| Name of Mentor (Sem I & II) | 116 | 738.0 | blank spans x=26.88–332.8, row underline at y=736.54 |
| Name of Mentor (Sem III–VIII) | 375 | 738.0 | blank spans x=333.74–555.6, same row |
| Register Number | 72 | 709.5 | cell x=26.88–167.35, underline at y=707.71 |
| Name of the Student | 292 | 709.5 | cell x=168.79–333.0, underline at y=707.71 |
| Date of Birth (dd/mm/yyyy) | 345 | 709.5 | cell x=333.5–408.4, underline at y=707.71 |
| Admitted on (MM/YYYY) | 411 | 709.5 | cell x=408.89–481.9, underline at y=707.71 |
| Blood Group | 485 | 709.5 | cell x=482.35–555.6, underline at y=707.71 |
| Hobbies | 33 | 673.0 | label "Hobbies" at y=675.79, value below label |
| Games | 291 | 673.0 | label "Games" at y=675.79 |
| Literary | 380 | 673.0 | label "Literary" at y=675.79 |
| Community | 486 | 673.0 | label "Community" at y=675.79 |
| % in 10th | 85 | 637.0 | cell x=82.58–181.75, row underline at y=634.51 |
| % in 12th | 258 | 637.0 | cell x=255.46–332.8, row underline at y=634.51 |
| % in Diploma | 411 | 637.0 | cell x=408.89–455.5, row underline at y=634.51 |
| Photo | (attach physically — no overlay) | — | template already has "Affix stamp size photo" box, left blank |

## Page 1 — Semester table grid (CAT/marks columns), Semesters I–IV

Both semester pairs (I & II on the left/right of one block, III & IV on the
left/right of a second block below it) reuse an **identical column layout**,
just at different row-block y-ranges. Column x-positions were recovered from
the vertical grid lines (`re f` rects with width ≤1pt, height >5pt):

### Semester I (left column set) / Semester III (left column set, lower block)
| Column | x (left edge of cell) |
|---|---|
| CAT1 | 111.65 |
| CAT2 | 133.49 (Sem I) / 144.05 (Sem III — slightly different template revision) |
| Pre-Univ | 156.79 / 171.67 |
| Int. Marks | 181.75 / 200.23 |
| Uni. Marks | 211.30 / 227.38 |
| Cleared in (MM/YYYY) | 254.74 |

### Semester II (right column set) / Semester IV (right column set)
| Column | x (left edge of cell) |
|---|---|
| CAT1 | 372.38 / 399.29 |
| CAT2 | 394.97 / 424.73 |
| Pre-Univ | 419.93 / 448.75 |
| Int. Marks | 442.73 / 473.47 |
| Uni. Marks | 473.47 / 501.07 |
| Cleared in (MM/YYYY) | 501.07 |

> The template has a minor column-width difference between the Sem I/II block
> and the Sem III/IV block (visible in the raw grid-line extraction — see
> `docs/mentorcard_layout1.txt`, generated during analysis). Both sets are
> captured verbatim in `MentorCardCoordinates.java` as `SEM_1_2_COLUMNS` and
> `SEM_3_4_COLUMNS` rather than assumed identical, to stay faithful to the
> actual template.

### Row y-positions (top edge of each subject row), Semester I/II block
`491.66, 479.18, 462.84, 446.76, 430.68, 414.60, 397.80, 379.06, 360.34, 338.98`
(10 subject rows — matches the CSE Semester I/II subject count on the real
form: Technical English, Mathematics I, Engg. Physics, Engg. Chemistry, Basic
Electrical & Electronics Engg., Fundamentals of Computer Engineering,
Communicative English Lab, Basic Mechanical & Civil Engg. Lab, Python
Programming / C Programming & MS Office Tools, Environmental Science /
Orientation to Entrepreneurship & Project Lab)

### Row y-positions, Semester III/IV block
`274.63, 262.39, 246.07, 229.99, 214.15, 198.05, 180.05, 162.05, 145.97, 122.18, 110.18, 95.30`

GPA/CGPA labels for Sem I/II block: `"GPA:"` at (125.33, 329.14), `"CGPA:"` at
(203.35, 329.14) — write the value ~35pt to the right of each label on the
same baseline. Same pattern for Sem III/IV block at y=70.82/70.34.

## Page 2 — Semester table grid, Semesters V–VIII

Column x-positions (Sem V/VII left set): `124.37 (CAT1), 153.19 (CAT2), 180.55
(Pre-Univ), 209.62 (Int. Marks), 233.38 (Uni. Marks), 259.30 (Cleared in)`

Column x-positions (Sem VI/VIII right set): `392.81 (CAT1), 420.17 (CAT2),
445.39 (Pre-Univ), 471.55 (Int. Marks), 493.63 (Uni. Marks), 521.02 (Cleared in)`

GPA/CGPA labels: Sem V/VI block at y≈498.62/498.62, Sem VII/VIII block at
y≈254.47/253.27 (same right-offset convention as page 1).

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

Extracted from the same text-run parse described above (reading order,
fragments joined where the PDF wraps a subject name across 2 lines). Used
verbatim as the seed `Subject` rows so the demo dashboard/PDF show the
university's actual CSE curriculum, not placeholder names.

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
