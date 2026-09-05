import { useEffect, useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { api } from "../api/client";
import type { DocumentType, StudentDashboardResponse } from "../api/types";
import { StatusChip } from "../components/StatusChip";
import { Skeleton } from "../components/Skeleton";
import { ChangePasswordModal } from "../components/ChangePasswordModal";
import { useAuth } from "../auth/AuthContext";

const DOCUMENT_LABELS: Record<DocumentType, string> = {
  MARKSHEET_10: "10th Marksheet",
  MARKSHEET_12: "12th Marksheet",
  DIPLOMA: "Diploma Marksheet",
  SEMESTER_1_MARKSHEET: "Semester 1 Marksheet",
  SEMESTER_2_MARKSHEET: "Semester 2 Marksheet",
  SEMESTER_3_MARKSHEET: "Semester 3 Marksheet",
  SEMESTER_4_MARKSHEET: "Semester 4 Marksheet",
  SEMESTER_5_MARKSHEET: "Semester 5 Marksheet",
  SEMESTER_6_MARKSHEET: "Semester 6 Marksheet",
  SEMESTER_7_MARKSHEET: "Semester 7 Marksheet",
  SEMESTER_8_MARKSHEET: "Semester 8 Marksheet",
  AADHAAR: "Aadhaar",
  PAN: "PAN",
};

export function StudentDashboardPage() {
  const { logout, forcePasswordChange } = useAuth();
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState<StudentDashboardResponse | null>(
    null,
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(forcePasswordChange);

  useEffect(() => {
    api
      .get<StudentDashboardResponse>("/student/dashboard")
      .then(setDashboard)
      .catch(() => setError("Could not load your dashboard. Please try again."))
      .finally(() => setLoading(false));
  }, []);

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const blob = await api.downloadBlob("/student/mentor-card.pdf");
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `MentorCard-${dashboard?.regNo ?? "student"}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch {
      setError("Could not generate the Mentor Card PDF. Please try again.");
    } finally {
      setDownloading(false);
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
            <p className="text-xs text-gray-500">Student Dashboard</p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate({ to: "/upload" })}
              className="rounded-md border border-navy-600 px-3 py-1.5 text-sm font-medium text-navy-700 hover:bg-navy-50"
            >
              Upload Documents
            </button>
            <button
              onClick={handleLogout}
              className="rounded-md px-3 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-100"
            >
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-6 px-6 py-8">
        {error && (
          <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {loading ? (
          <div className="space-y-4">
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-48 w-full" />
          </div>
        ) : dashboard ? (
          <>
            <div className="card flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold text-navy-800">
                  {dashboard.name}
                </h2>
                <p className="text-sm text-gray-500">
                  Reg No: {dashboard.regNo}
                </p>
              </div>
              <button
                onClick={handleDownload}
                disabled={!dashboard.downloadReady || downloading}
                className="rounded-md bg-navy-700 px-4 py-2 text-sm font-semibold text-white transition hover:bg-navy-800 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {downloading ? "Generating..." : "Download Mentor Card PDF"}
              </button>
            </div>

            <div className="card">
              <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-gray-500">
                Document Checklist
              </h3>
              <ul className="grid gap-3 sm:grid-cols-2">
                {dashboard.documents.map((doc) => (
                  <li
                    key={doc.type}
                    className="flex items-center justify-between rounded-md border border-gray-100 px-3 py-2"
                  >
                    <span className="text-sm text-gray-700">
                      {DOCUMENT_LABELS[doc.type]}
                    </span>
                    <StatusChip
                      variant={doc.uploaded ? "loaded" : "pending"}
                      label={doc.uploaded ? "Uploaded ✓" : "Pending"}
                    />
                  </li>
                ))}
              </ul>
            </div>

            <div className="card">
              <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-gray-500">
                CAT Marks &amp; GPA by Semester
              </h3>
              {Object.keys(dashboard.marksBySemester).length === 0 ? (
                <p className="text-sm text-gray-500">
                  Not yet available — waiting on the admin's CAT marks import.
                </p>
              ) : (
                <div className="space-y-6">
                  {Object.entries(dashboard.marksBySemester)
                    .sort((a, b) => Number(a[0]) - Number(b[0]))
                    .map(([semester, marks]) => (
                      <div key={semester}>
                        <div className="mb-2 flex items-center justify-between">
                          <h4 className="text-sm font-semibold text-navy-700">
                            Semester {semester}
                          </h4>
                          <div className="flex gap-4 text-xs text-gray-500">
                            <span>
                              GPA:{" "}
                              <strong className="text-navy-700">
                                {dashboard.gpaBySemester[semester]?.toFixed(2) ??
                                  "—"}
                              </strong>
                            </span>
                            <span>
                              CGPA:{" "}
                              <strong className="text-navy-700">
                                {dashboard.cgpaBySemester[semester]?.toFixed(
                                  2,
                                ) ?? "—"}
                              </strong>
                            </span>
                          </div>
                        </div>
                        <div className="overflow-x-auto">
                          <table className="w-full text-left text-sm">
                            <thead>
                              <tr className="border-b border-gray-200 text-xs uppercase text-gray-400">
                                <th className="py-2">Subject</th>
                                <th className="py-2">CAT1</th>
                                <th className="py-2">CAT2</th>
                                <th className="py-2">CAT3</th>
                                <th className="py-2">Uni. Marks</th>
                                <th className="py-2">Cleared In</th>
                              </tr>
                            </thead>
                            <tbody>
                              {marks.map((mark) => (
                                <tr
                                  key={mark.subjectId}
                                  className="border-b border-gray-100"
                                >
                                  <td className="py-2">{mark.subjectName}</td>
                                  <td className="py-2">{mark.cat1 ?? "—"}</td>
                                  <td className="py-2">{mark.cat2 ?? "—"}</td>
                                  <td className="py-2">{mark.cat3 ?? "—"}</td>
                                  <td className="py-2">{mark.uniMarks ?? "—"}</td>
                                  <td className="py-2">
                                    {mark.clearedMonthYear ?? "—"}
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </div>
          </>
        ) : null}
      </main>

      {showChangePassword && (
        <ChangePasswordModal onDone={() => setShowChangePassword(false)} />
      )}
    </div>
  );
}
