export type DocumentType =
  | "MARKSHEET_10"
  | "MARKSHEET_12"
  | "DIPLOMA"
  | "SEMESTER_MARKSHEET"
  | "AADHAAR"
  | "PAN";

export interface LoginResponse {
  token: string;
  role: "STUDENT" | "ADMIN";
  forcePasswordChange: boolean;
}

export interface SubjectMarkView {
  subjectId: number;
  subjectName: string;
  subjectCode: string;
  semesterNumber: number;
  cat1: number | null;
  cat2: number | null;
  cat3: number | null;
  preUniv: number | null;
  intMarks: number | null;
  uniMarks: number | null;
  clearedMonthYear: string | null;
  source: "EXCEL_IMPORT" | "OCR";
}

export interface DocumentStatusView {
  type: DocumentType;
  uploaded: boolean;
  confirmed: boolean;
}

export interface StudentDashboardResponse {
  regNo: string;
  name: string;
  marksBySemester: Record<string, SubjectMarkView[]>;
  gpaBySemester: Record<string, number | null>;
  cgpaBySemester: Record<string, number | null>;
  documents: DocumentStatusView[];
  downloadReady: boolean;
}

export interface OcrExtractionResponse {
  documentId: number;
  subjects: { subjectName: string; marksObtained: number }[];
  overallPercentage: number | null;
  rawText: string;
}

export interface ImportSummaryResponse {
  importBatchId: number;
  filename: string;
  totalRows: number;
  matchedCount: number;
  unmatchedCount: number;
  unmatchedRegNos: string[];
}

export interface StudentSummaryResponse {
  id: number;
  regNo: string;
  name: string;
  forcePasswordChange: boolean;
}
