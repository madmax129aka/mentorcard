import { useRef, useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { api, ApiError } from "../api/client";
import type { DocumentType, OcrExtractionResponse } from "../api/types";
import { ExtractingIndicator } from "../components/Skeleton";
import { OcrConfirmModal } from "../components/OcrConfirmModal";
import { SemesterMarksheetConfirmModal } from "../components/SemesterMarksheetConfirmModal";

interface UploadCardConfig {
  type: DocumentType;
  title: string;
  description: string;
  usesOcr: boolean;
}

const CARDS: UploadCardConfig[] = [
  {
    type: "MARKSHEET_10",
    title: "10th Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    type: "MARKSHEET_12",
    title: "12th Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    type: "DIPLOMA",
    title: "Diploma Marksheet",
    description: "OCR extracts subject marks & percentage for you to confirm.",
    usesOcr: true,
  },
  {
    type: "SEMESTER_MARKSHEET",
    title: "Semester Marksheet",
    description: "OCR extracts Uni. Marks and Cleared-in date for you to confirm.",
    usesOcr: true,
  },
  {
    type: "AADHAAR",
    title: "Aadhaar",
    description: "Simple upload — no OCR, stored securely.",
    usesOcr: false,
  },
  {
    type: "PAN",
    title: "PAN",
    description: "Simple upload — no OCR, stored securely.",
    usesOcr: false,
  },
];

export function UploadPage() {
  const navigate = useNavigate();
  const [extracting, setExtracting] = useState<DocumentType | null>(null);
  const [pendingExtraction, setPendingExtraction] = useState<{
    type: DocumentType;
    result: OcrExtractionResponse;
  } | null>(null);
  const [uploadedTypes, setUploadedTypes] = useState<Set<DocumentType>>(
    new Set(),
  );
  const [error, setError] = useState<string | null>(null);
  const fileInputs = useRef<Record<string, HTMLInputElement | null>>({});

  const handleFileSelected = async (
    type: DocumentType,
    usesOcr: boolean,
    file: File,
  ) => {
    setError(null);
    if (!usesOcr) {
      try {
        await api.upload(`/student/documents/${type}/upload`, file);
        setUploadedTypes((prev) => new Set(prev).add(type));
      } catch (err) {
        setError(describeError(err));
      }
      return;
    }

    setExtracting(type);
    try {
      const result = await api.upload<OcrExtractionResponse>(
        `/student/documents/${type}/upload`,
        file,
      );
      setPendingExtraction({ type, result });
    } catch (err) {
      setError(describeError(err));
    } finally {
      setExtracting(null);
    }
  };

  const handleConfirmMarksheet = async (percentage: number | null) => {
    if (!pendingExtraction) return;
    try {
      await api.post(`/student/documents/${pendingExtraction.type}/confirm`, {
        documentId: pendingExtraction.result.documentId,
        confirmedPercentage: percentage,
        subjects: pendingExtraction.result.subjects.map((s) => ({
          subjectName: s.subjectName,
          marksObtained: s.marksObtained,
        })),
      });
      setUploadedTypes((prev) => new Set(prev).add(pendingExtraction.type));
      setPendingExtraction(null);
    } catch (err) {
      setError(describeError(err));
    }
  };

  const handleConfirmSemesterMarksheet = async (
    semesterNumber: number,
    rows: { subjectCode: string; uniMarks: string; clearedMonthYear: string }[],
  ) => {
    if (!pendingExtraction) return;
    try {
      await api.post("/student/documents/semester-marksheet/confirm", {
        documentId: pendingExtraction.result.documentId,
        semesterNumber,
        subjects: rows.map((r) => ({
          subjectCode: r.subjectCode,
          uniMarks: r.uniMarks.trim() === "" ? null : Number(r.uniMarks),
          clearedMonthYear: r.clearedMonthYear.trim() === "" ? null : r.clearedMonthYear,
        })),
      });
      setUploadedTypes((prev) => new Set(prev).add(pendingExtraction.type));
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

      <main className="mx-auto max-w-5xl space-y-4 px-6 py-8">
        {error && (
          <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="grid gap-4 sm:grid-cols-2">
          {CARDS.map((card) => (
            <div key={card.type} className="card">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="font-semibold text-navy-800">{card.title}</h3>
                {uploadedTypes.has(card.type) && (
                  <span className="status-chip status-chip--loaded">
                    Uploaded ✓
                  </span>
                )}
              </div>
              <p className="mb-3 text-sm text-gray-500">{card.description}</p>

              {extracting === card.type ? (
                <ExtractingIndicator />
              ) : (
                <>
                  <input
                    ref={(el) => {
                      fileInputs.current[card.type] = el;
                    }}
                    type="file"
                    accept="image/*,application/pdf"
                    className="hidden"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) {
                        handleFileSelected(card.type, card.usesOcr, file);
                      }
                      e.target.value = "";
                    }}
                  />
                  <button
                    onClick={() => fileInputs.current[card.type]?.click()}
                    className="rounded-md border border-navy-600 px-3 py-1.5 text-sm font-medium text-navy-700 hover:bg-navy-50"
                  >
                    Choose File
                  </button>
                </>
              )}
            </div>
          ))}
        </div>
      </main>

      {pendingExtraction && pendingExtraction.type === "SEMESTER_MARKSHEET" && (
        <SemesterMarksheetConfirmModal
          extraction={pendingExtraction.result}
          onConfirm={handleConfirmSemesterMarksheet}
          onCancel={() => setPendingExtraction(null)}
        />
      )}

      {pendingExtraction && pendingExtraction.type !== "SEMESTER_MARKSHEET" && (
        <OcrConfirmModal
          extraction={pendingExtraction.result}
          onConfirm={handleConfirmMarksheet}
          onCancel={() => setPendingExtraction(null)}
        />
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
