import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "@tanstack/react-router";
import { api, ApiError } from "../api/client";
import type { ImportSummaryResponse, StudentSummaryResponse } from "../api/types";
import { useAuth } from "../auth/AuthContext";

export function AdminDashboardPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [students, setStudents] = useState<StudentSummaryResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [importResult, setImportResult] = useState<ImportSummaryResponse | null>(
    null,
  );
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [newRegNo, setNewRegNo] = useState("");
  const [newName, setNewName] = useState("");
  const [creating, setCreating] = useState(false);

  const loadStudents = (regNo?: string) => {
    api
      .get<StudentSummaryResponse[]>(
        `/admin/students${regNo ? `?regNo=${encodeURIComponent(regNo)}` : ""}`,
      )
      .then(setStudents)
      .catch(() => setError("Could not load students."));
  };

  useEffect(() => {
    loadStudents();
  }, []);

  const handleImport = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    const fileInput = event.currentTarget.elements.namedItem(
      "file",
    ) as HTMLInputElement;
    const file = fileInput.files?.[0];
    if (!file) {
      setError("Please choose an Excel file to import.");
      return;
    }
    setImporting(true);
    try {
      const result = await api.upload<ImportSummaryResponse>(
        "/admin/import-cat-marks",
        file,
      );
      setImportResult(result);
      loadStudents(searchTerm);
    } catch (err) {
      setError(describeError(err));
    } finally {
      setImporting(false);
    }
  };

  const handleCreateStudent = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setCreating(true);
    try {
      await api.post("/admin/students", { regNo: newRegNo.trim(), name: newName.trim() });
      setNewRegNo("");
      setNewName("");
      loadStudents(searchTerm);
    } catch (err) {
      setError(describeError(err));
    } finally {
      setCreating(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate({ to: "/login" });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-lg font-bold text-navy-800">MentorTrack</h1>
            <p className="text-xs text-gray-500">Admin Dashboard</p>
          </div>
          <button
            onClick={handleLogout}
            className="rounded-md px-3 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-100"
          >
            Log out
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-6 px-6 py-8">
        {error && (
          <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="card">
          <h2 className="mb-1 text-lg font-semibold text-navy-800">
            Import CAT Marks
          </h2>
          <p className="mb-4 text-sm text-gray-500">
            Upload the master Excel sheet for this exam cycle. Expected
            columns: RegNo, SubjectCode, CAT1, CAT2, CAT3, PreUniv, IntMarks.
          </p>

          <form onSubmit={handleImport} className="flex items-center gap-3">
            <input
              type="file"
              name="file"
              accept=".xlsx,.xls"
              className="text-sm"
            />
            <button
              type="submit"
              disabled={importing}
              className="rounded-md bg-navy-700 px-4 py-2 text-sm font-semibold text-white hover:bg-navy-800 disabled:opacity-60"
            >
              {importing ? "Importing..." : "Import"}
            </button>
            <a
              href="/api/admin/sample-cat-marks-excel"
              className="text-sm text-navy-600 underline hover:text-navy-800"
            >
              Download sample file
            </a>
          </form>

          {importResult && (
            <div className="mt-4 rounded-md border border-gray-100 bg-gray-50 p-4 text-sm">
              <p>
                <strong>{importResult.filename}</strong> — {importResult.totalRows}{" "}
                rows, <span className="text-green-700">{importResult.matchedCount} matched</span>,{" "}
                <span className="text-amber-700">
                  {importResult.unmatchedCount} unmatched
                </span>
              </p>
              {importResult.unmatchedRegNos.length > 0 && (
                <ul className="mt-2 list-inside list-disc text-gray-600">
                  {importResult.unmatchedRegNos.map((r, idx) => (
                    <li key={idx}>{r}</li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>

        <div className="card">
          <h2 className="mb-4 text-lg font-semibold text-navy-800">
            Manage Students
          </h2>

          <form onSubmit={handleCreateStudent} className="mb-6 flex flex-wrap items-end gap-3">
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Reg No
              </label>
              <input
                value={newRegNo}
                onChange={(e) => setNewRegNo(e.target.value)}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm"
                required
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Name
              </label>
              <input
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm"
                required
              />
            </div>
            <button
              type="submit"
              disabled={creating}
              className="rounded-md bg-navy-700 px-4 py-1.5 text-sm font-semibold text-white hover:bg-navy-800 disabled:opacity-60"
            >
              {creating ? "Creating..." : "Create Account"}
            </button>
          </form>

          <div className="mb-3 flex items-center gap-2">
            <input
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by register number..."
              className="rounded-md border border-gray-300 px-3 py-1.5 text-sm"
            />
            <button
              onClick={() => loadStudents(searchTerm)}
              className="rounded-md border border-navy-600 px-3 py-1.5 text-sm font-medium text-navy-700 hover:bg-navy-50"
            >
              Search
            </button>
          </div>

          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-gray-200 text-xs uppercase text-gray-400">
                <th className="py-2">Reg No</th>
                <th className="py-2">Name</th>
                <th className="py-2">Password Status</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student) => (
                <tr key={student.id} className="border-b border-gray-100">
                  <td className="py-2">{student.regNo}</td>
                  <td className="py-2">{student.name}</td>
                  <td className="py-2">
                    {student.forcePasswordChange ? (
                      <span className="status-chip status-chip--pending">
                        Default password
                      </span>
                    ) : (
                      <span className="status-chip status-chip--loaded">
                        Changed
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}

function describeError(err: unknown): string {
  if (err instanceof ApiError) {
    return err.message || "Request failed. Please try again.";
  }
  return "Request failed. Please try again.";
}
