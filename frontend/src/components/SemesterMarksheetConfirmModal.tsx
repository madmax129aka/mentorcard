import { useState } from "react";
import type { OcrExtractionResponse } from "../api/types";

interface SubjectRow {
  subjectCode: string;
  uniMarks: string;
  clearedMonthYear: string;
}

interface Props {
  extraction: OcrExtractionResponse;
  onConfirm: (semesterNumber: number, rows: SubjectRow[]) => Promise<void>;
  onCancel: () => void;
}

/**
 * Semester marksheets don't carry machine-readable subject codes, so OCR can only extract raw
 * subject names + marks (shown here as a reference). The student picks the semester and enters
 * the subject code + Uni. Marks + Cleared-in date for each row before saving — per spec, OCR
 * output is never silently auto-committed.
 */
export function SemesterMarksheetConfirmModal({
  extraction,
  onConfirm,
  onCancel,
}: Props) {
  const [semesterNumber, setSemesterNumber] = useState("1");
  const [rows, setRows] = useState<SubjectRow[]>(
    extraction.subjects.length > 0
      ? extraction.subjects.map((s) => ({
          subjectCode: "",
          uniMarks: String(s.marksObtained),
          clearedMonthYear: "",
        }))
      : [{ subjectCode: "", uniMarks: "", clearedMonthYear: "" }],
  );
  const [saving, setSaving] = useState(false);

  const updateRow = (index: number, patch: Partial<SubjectRow>) => {
    setRows((prev) =>
      prev.map((row, i) => (i === index ? { ...row, ...patch } : row)),
    );
  };

  const addRow = () =>
    setRows((prev) => [
      ...prev,
      { subjectCode: "", uniMarks: "", clearedMonthYear: "" },
    ]);

  const handleConfirm = async () => {
    setSaving(true);
    try {
      const validRows = rows.filter((r) => r.subjectCode.trim() !== "");
      await onConfirm(Number(semesterNumber), validRows);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="card w-full max-w-lg">
        <h3 className="mb-1 text-lg font-semibold text-navy-800">
          Confirm Semester Results
        </h3>
        <p className="mb-4 text-sm text-gray-500">
          OCR extracted the marks below as reference. Enter each subject's
          code (as listed on your dashboard), Uni. Marks, and Cleared-in date,
          then save.
        </p>

        <label className="mb-1 block text-xs font-medium text-gray-600">
          Semester Number
        </label>
        <select
          value={semesterNumber}
          onChange={(e) => setSemesterNumber(e.target.value)}
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          {Array.from({ length: 8 }, (_, i) => i + 1).map((n) => (
            <option key={n} value={n}>
              Semester {n}
            </option>
          ))}
        </select>

        <div className="mb-3 max-h-60 space-y-2 overflow-y-auto">
          {rows.map((row, idx) => (
            <div key={idx} className="grid grid-cols-3 gap-2">
              <input
                placeholder="Subject Code (e.g. CS101)"
                value={row.subjectCode}
                onChange={(e) =>
                  updateRow(idx, { subjectCode: e.target.value })
                }
                className="rounded-md border border-gray-300 px-2 py-1.5 text-sm"
              />
              <input
                placeholder="Uni. Marks"
                type="number"
                value={row.uniMarks}
                onChange={(e) => updateRow(idx, { uniMarks: e.target.value })}
                className="rounded-md border border-gray-300 px-2 py-1.5 text-sm"
              />
              <input
                placeholder="Cleared In (MM/YYYY)"
                value={row.clearedMonthYear}
                onChange={(e) =>
                  updateRow(idx, { clearedMonthYear: e.target.value })
                }
                className="rounded-md border border-gray-300 px-2 py-1.5 text-sm"
              />
            </div>
          ))}
        </div>

        <button
          onClick={addRow}
          className="mb-4 text-sm font-medium text-navy-600 hover:text-navy-800"
        >
          + Add another subject
        </button>

        <div className="flex justify-end gap-2">
          <button
            onClick={onCancel}
            className="rounded-md px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-100"
          >
            Cancel
          </button>
          <button
            onClick={handleConfirm}
            disabled={saving}
            className="rounded-md bg-navy-700 px-4 py-2 text-sm font-semibold text-white hover:bg-navy-800 disabled:opacity-60"
          >
            {saving ? "Saving..." : "Confirm & Save"}
          </button>
        </div>
      </div>
    </div>
  );
}
