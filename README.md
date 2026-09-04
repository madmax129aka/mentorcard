# MentorTrack — Automated Digital Mentor Card System

A self-service web portal where students log in with their register number,
upload marksheets and ID documents, and download a fully populated Mentor
Card PDF — with CAT marks pulled automatically from a single college-provided
Excel sheet.

This repo also contains the **real Mentor Card PDF templates** used by the
university (`Mentor Card Printout - CSE-1 2024.pdf` and `... CSE-2 2024.pdf`
at the repo root, copied into `backend/src/main/resources/templates/` for the
app to use), and the actual PDF generation overlays student data onto those
exact files rather than a from-scratch rendering. See
`docs/PDF_FIELD_MAP.md` for how the field coordinates were derived and their
confidence levels.

## Repo layout

```
backend/    Spring Boot 3 REST API (Java 17, Maven)
frontend/   React + Vite + TypeScript SPA (TanStack Router, Tailwind CSS)
docs/       PDF field-coordinate derivation notes + raw extraction data
Mentor Card Printout - CSE-1 2024.pdf   <- real template, page 1 of 2 (also copied into backend/resources)
Mentor Card Printout - CSE-2 2024.pdf   <- real template, page 2 of 2 (also copied into backend/resources)
```

## ⚠️ Important: this build could not be compiled/run in the authoring sandbox

The sandbox this project was built in had **no outbound access to Maven
Central, npm, or pip** (`network_mode: INTEGRATIONS_ONLY`, and even the
GitHub-routed gateway proxy returned 403 for other domains). That means:

- `mvn` could not download Spring Boot / PDFBox / POI / Tess4j / JJWT and so
  the backend has **not been compiled** in this environment.
- `npm install` could not fetch React / Vite / TanStack Router / Tailwind and
  so the frontend has **not been built or type-checked** in this environment.
- No PDF rasterizer (`poppler`, `ghostscript`, `ImageMagick`) was available to
  visually render the filled PDF and confirm pixel-perfect placement.

Every file was therefore hand-written and manually cross-checked (import
paths, DTO field names between Java records and TypeScript interfaces, method
signatures against the pinned library versions) rather than verified by an
actual compiler/bundler. **Before relying on this for real use, run:**

```bash
cd backend && mvn clean verify
cd frontend && npm install && npm run build
```

and fix any compilation errors that surface — treat this as a thorough
first-pass implementation, not a build-verified one.

## Running locally (once dependencies are installable)

### Backend
```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080 with an in-memory H2 database (profile "demo")
# and seeds 15 demo students + CSE subjects (Sem I-VIII) + an admin account
# (admin / admin123) automatically on startup.
```

For a real MySQL-backed deployment:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql \
  -DMENTORTRACK_DB_URL=jdbc:mysql://localhost:3306/mentortrack \
  -DMENTORTRACK_DB_USER=... -DMENTORTRACK_DB_PASSWORD=... \
  -DMENTORTRACK_JWT_SECRET=... -DMENTORTRACK_STORAGE_KEY=...
```
Also set `mentortrack.seed.enabled=false` for a real deployment so demo data
isn't created.

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173, proxies /api to the backend on :8080
```

### OCR (Tesseract)
The marksheet upload endpoints use Tess4j, which requires the native
Tesseract library + trained data (`tessdata`) to be installed on the host
(e.g. `apt-get install tesseract-ocr` on Debian/Ubuntu). Configure the path
via `mentortrack.ocr.tessdata-path` / `MENTORTRACK_TESSDATA_PATH`. If
Tesseract isn't installed, OCR upload endpoints return a clear `503` instead
of crashing — the rest of the app keeps working, and Aadhaar/PAN uploads
(which never use OCR) are unaffected.

## Demo walkthrough

1. Start the backend (demo profile) — it seeds 15 students (`21CSE001` ..
   `21CSE015`, default password = reg no), an admin account (`admin` /
   `admin123`), the full CSE Semester I-VIII subject list transcribed from
   the real template, and a small amount of Semester I CAT-marks data so the
   dashboard has something to show immediately.
2. Log in as `admin` / `admin123` → **Admin Dashboard**.
   - Click "Download sample file" to get
     `backend/src/main/resources/seed-assets/CAT_Marks_Import_Sample.xlsx`
     (15 students × Semester I subjects, plus one deliberately-unmatched reg
     no and one deliberately-unknown subject code so you can see the
     match/unmatched summary work).
   - Upload it via "Import CAT Marks" and see the match/unmatched summary.
3. Log out, log in as a student (e.g. `21CSE001` / `21CSE001`) → forced
   password-change modal appears (spec requirement) → set a new password.
4. On the **Student Dashboard**, see CAT marks/GPA populated, and the
   document checklist.
5. Go to **Upload Documents** → try the 10th/12th/Diploma/Semester marksheet
   flow (a sample demo image is at
   `backend/src/main/resources/seed-assets/sample-marksheet-10th.png` — since
   this repo's sandbox couldn't install Tesseract to generate a *real* OCR'd
   sample, this is a clean synthetic mark-sheet-style image for demoing the
   upload → OCR → confirm-modal → save flow end to end) — review/edit the
   OCR-extracted values in the confirm modal, then save. Try Aadhaar/PAN for
   the plain-upload flow.
6. Back on the dashboard, click **Download Mentor Card PDF** — this calls
   PDFBox to overlay all collected data onto the real template PDFs bundled
   in `backend/src/main/resources/templates/` and returns the merged 2-page
   PDF.

## Known gaps / things to verify before real department sign-off use

- **GPA/CGPA formula is a placeholder.** No official grade-point scale or
  credit-weighting rule was provided. `GpaCalculator` documents the exact
  interim formula used (unweighted average of an absolute 10-point grading
  table applied to Uni. Marks) — replace this with the department's actual
  formula, and add subject credit weights to the `Subject` entity if the real
  formula is credit-weighted.
- **Header/personal-detail PDF coordinates are label-position-derived, not
  print-verified** (see `docs/PDF_FIELD_MAP.md` confidence levels) — generate
  one sample PDF and visually check the header fields land inside their
  intended cells; nudge coordinates in `MentorCardCoordinates.java` if not.
- **Semester V-VIII row-count on page 2** was derived from the template's own
  grid lines but the last 1-2 rows of Semester VII/VIII should be
  spot-checked against a printed sample (see the same doc's confidence
  notes).
- The whole backend and frontend are unbuild-verified in this authoring
  environment (see the warning above) — run a full `mvn verify` / `npm run
  build` pass and fix anything a real compiler/bundler surfaces.
- Backend unit tests exist for `GpaCalculator` and `MarksheetExtractor`
  (`backend/src/test/java/...`) but could not be executed here for the same
  network-access reason.
