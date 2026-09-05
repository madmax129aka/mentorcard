import { useState } from "react";
import type { OcrExtractionResponse } from "../api/types";

interface Props {
  extraction: OcrExtractionResponse;
  onConfirm: (percentage: number | null) => Promise<void>;
  onCancel: () => void;
}

/**
 * Shows the OCR-extracted percentage/marks for a 10th/12th/Diploma marksheet and lets the student
 * edit the value before it is saved — per spec, never silently auto-commit OCR output.
 */
export function OcrConfirmModal({ extraction, onConfirm, onCancel }: Props) {
  const [percentage, setPercentage] = useState<string>(
    extraction.overallPercentage != null
      ? String(extraction.overallPercentage)
      : "",
  );
  const [saving, setSaving] = useState(false);

  const handleConfirm = async () => {
    setSaving(true);
    try {
      await onConfirm(percentage.trim() === "" ? null : Number(percentage));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="card w-full max-w-md">
        <h3 className="mb-1 text-lg font-semibold text-navy-800">
          Confirm Extracted Marks
        </h3>
        <p className="mb-4 text-sm text-gray-500">
          Review the values OCR extracted from your marksheet. Edit anything
          that looks wrong before saving.
        </p>

        {extraction.subjects.length > 0 && (
          <div className="mb-4 max-h-40 overflow-y-auto rounded-md border border-gray-100">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                  <th className="px-3 py-2">Subject</th>
                  <th className="px-3 py-2">Marks</th>
                </tr>
              </thead>
              <tbody>
                {extraction.subjects.map((s, idx) => (
                  <tr key={idx} className="border-b border-gray-50">
                    <td className="px-3 py-1.5">{s.subjectName}</td>
                    <td className="px-3 py-1.5">{s.marksObtained}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <label className="mb-1 block text-sm font-medium text-gray-700">
          Overall Percentage
        </label>
        <input
          type="number"
          step="0.01"
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-navy-500 focus:outline-none focus:ring-1 focus:ring-navy-500"
          value={percentage}
          onChange={(e) => setPercentage(e.target.value)}
        />

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
