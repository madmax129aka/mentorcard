import { useRef, useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { api, ApiError } from "../api/client";
import type { DocumentType, OcrExtractionResponse } from "../api/types";
import { ExtractingIndicator } from "../components/Skeleton";
import { OcrConfirmModal } from "../components/OcrConfirmModal";
import { SemesterMarksheetConfirmModal } from "../components/SemesterMarksheetConfirmModal";

interface UploadCardConfig {
  key: string;
  type: DocumentType;
  title: string;
  description: string;
  usesOcr: boolean;
  /** Set only for semester marksheet cards; drives which /semester/{n}/... endpoint is called. */
  semesterNumber?: number;
}

const SEMESTER_TYPES: DocumentType[] = [
  "SEMESTER_1_MARKSHEET",
  "SEMESTER_2_MARKSHEET",
  "SEMESTER_3_MARKSHEET",
  "SEMESTER_4_MARKSHEET",
  "SEMESTER_5_MARKSHEET",
  "SEMESTER_6_MARKSHEET",
  "SEMESTER_7_MARKSHEET",
  "SEMESTER_8_MARKSHEET",
];

const NON_SEMESTER_CARDS: UploadCardConfig[] = [
  {
    key: "MARKSHEET_10",
    type: "MARKSHEET_10",
    title: "10th Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    key: "MARKSHEET_12",
    type: "MARKSHEET_12",
    title: "12th Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    key: "DIPLOMA",
    type: "DIPLOMA",
    title: "Diploma Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    key: "AADHAAR",
    type: "AADHAAR",
    title: "Aadhaar",
    description: "Simple upload — no OCR, stored securely.",
    usesOcr: false,
  },
  {
    key: "PAN",
    type: "PAN",
    title: "PAN",
    description: "Simple upload — no OCR, stored securely.",
    usesOcr: false,
  },
];

// One independent upload card per semester (1-8), each with its own endpoint and its own
// upload/confirm state — uploading Semester 3 has no effect on any other semester's slot.
const SEMESTER_CARDS: UploadCardConfig[] = SEMESTER_TYPES.map((type, idx) => ({
  key: type,
  type,
  title: `Semester ${idx + 1} Marksheet`,
  description: "OCR extracts Uni. Marks and Cleared-in date for you to confirm.",
  usesOcr: true,
  semesterNumber: idx + 1,
}));

const ALL_CARDS: UploadCardConfig[] = [...NON_SEMESTER_CARDS, ...SEMESTER_CARDS];

export function UploadPage() {
  const navigate = useNavigate();
  const [extracting, setExtracting] = useState<string | null>(null);
  const [pendingExtraction, setPendingExtraction] = useState<{
    card: UploadCardConfig;
    result: OcrExtractionResponse;
  } | null>(null);
  const [uploadedKeys, setUploadedKeys] = useState<Set<string>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const fileInputs = useRef<Record<string, HTMLInputElement | null>>({});

  const uploadPathFor = (card: UploadCardConfig) =>
    card.semesterNumber != null
      ? `/student/documents/semester/${card.semesterNumber}/upload`
      : `/student/documents/${card.type}/upload`;

  const confirmPathFor = (card: UploadCardConfig) =>
    card.semesterNumber != null
      ? `/student/documents/semester/${card.semesterNumber}/confirm`
      : `/student/documents/${card.type}/confirm`;

  const handleFileSelected = async (card: UploadCardConfig, file: File) => {
    setError(null);
    if (!card.usesOcr) {
      try {
        await api.upload(uploadPathFor(card), file);
        setUploadedKeys((prev) => new Set(prev).add(card.key));
      } catch (err) {
        setError(describeError(err));
      }
      return;
    }

    setExtracting(card.key);
    try {
      const result = await api.upload<OcrExtractionResponse>(uploadPathFor(card), file);
      setPendingExtraction({ card, result });
    } catch (err) {
      setError(describeError(err));
    } finally {
      setExtracting(null);
    }
  };

  const handleConfirmMarksheet = async (percentage: number | null) => {
    if (!pendingExtraction) return;
    const { card, result } = pendingExtraction;
    try {
      await api.post(confirmPathFor(card), {
        documentId: result.documentId,
        confirmedPercentage: percentage,
        subjects: result.subjects.map((s) => ({
          subjectName: s.subjectName,
          marksObtained: s.marksObtained,
        })),
      });
      setUploadedKeys((prev) => new Set(prev).add(card.key));
      setPendingExtraction(null);
    } catch (err) {
      setError(describeError(err));
    }
  };

  const handleConfirmSemesterMarksheet = async (
    rows: { subjectCode: string; uniMarks: string; clearedMonthYear: string }[],
  ) => {
    if (!pendingExtraction) return;
    const { card, result } = pendingExtraction;
    try {
      await api.post(confirmPathFor(card), {
        documentId: result.documentId,
        subjects: rows.map((r) => ({
          subjectCode: r.subjectCode,
          uniMarks: r.uniMarks.trim() === "" ? null : Number(r.uniMarks),
          clearedMonthYear: r.clearedMonthYear.trim() === "" ? null : r.clearedMonthYear,
        })),
      });
      setUploadedKeys((prev) => new Set(prev).add(card.key));
      setPendingExtraction(null);
    } catch (err) {
      setError(describeError(err));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-lg font-bold text-navy-800">MentorTrack</h1>
            <p className="text-xs text-gray-500">Upload Documents</p>
          </div>
          <button
            onClick={() => navigate({ to: "/dashboard" })}
            className="rounded-md border border-navy-600 px-3 py-1.5 text-sm font-medium text-navy-700 hover:bg-navy-50"
          >
            Back to Dashboard
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-6 px-6 py-8">
        {error && (
          <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
            Qualifying Marksheets &amp; ID
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {NON_SEMESTER_CARDS.map((card) => (
              <UploadCard
                key={card.key}
                card={card}
                extracting={extracting === card.key}
                uploaded={uploadedKeys.has(card.key)}
                fileInputRef={(el) => {
                  fileInputs.current[card.key] = el;
                }}
                onFileSelected={(file) => handleFileSelected(card, file)}
                onClickChoose={() => fileInputs.current[card.key]?.click()}
              />
            ))}
          </div>
        </section>

        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
            Semester Marksheets (upload each semester independently)
          </h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {ALL_CARDS.filter((c) => c.semesterNumber != null).map((card) => (
              <UploadCard
                key={card.key}
                card={card}
                extracting={extracting === card.key}
                uploaded={uploadedKeys.has(card.key)}
                fileInputRef={(el) => {
                  fileInputs.current[card.key] = el;
                }}
                onFileSelected={(file) => handleFileSelected(card, file)}
                onClickChoose={() => fileInputs.current[card.key]?.click()}
              />
            ))}
          </div>
        </section>
      </main>

      {pendingExtraction && pendingExtraction.card.semesterNumber != null && (
        <SemesterMarksheetConfirmModal
          extraction={pendingExtraction.result}
          semesterNumber={pendingExtraction.card.semesterNumber}
          onConfirm={handleConfirmSemesterMarksheet}
          onCancel={() => setPendingExtraction(null)}
        />
      )}

      {pendingExtraction && pendingExtraction.card.semesterNumber == null && (
        <OcrConfirmModal
          extraction={pendingExtraction.result}
          onConfirm={handleConfirmMarksheet}
          onCancel={() => setPendingExtraction(null)}
        />
      )}
    </div>
  );
}

function UploadCard({
  card,
  extracting,
  uploaded,
  fileInputRef,
  onFileSelected,
  onClickChoose,
}: {
  card: UploadCardConfig;
  extracting: boolean;
  uploaded: boolean;
  fileInputRef: (el: HTMLInputElement | null) => void;
  onFileSelected: (file: File) => void;
  onClickChoose: () => void;
}) {
  return (
    <div className="card">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="font-semibold text-navy-800">{card.title}</h3>
        {uploaded && (
          <span className="status-chip status-chip--loaded">Uploaded ✓</span>
        )}
      </div>
      <p className="mb-3 text-sm text-gray-500">{card.description}</p>

      {extracting ? (
        <ExtractingIndicator />
      ) : (
        <>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*,application/pdf"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                onFileSelected(file);
              }
              e.target.value = "";
            }}
          />
          <button
            onClick={onClickChoose}
            className="rounded-md border border-navy-600 px-3 py-1.5 text-sm font-medium text-navy-700 hover:bg-navy-50"
          >
            Choose File
          </button>
        </>
      )}
    </div>
  );
}

function describeError(err: unknown): string {
  if (err instanceof ApiError) {
    return err.message || "Upload failed. Please try again.";
  }
  return "Upload failed. Please try again.";
}
