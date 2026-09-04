export function Skeleton({ className = "" }: { className?: string }) {
  return <div className={`skeleton ${className}`} />;
}

export function ExtractingIndicator({ label = "Extracting data..." }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 text-navy-700">
      <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-navy-300 border-t-navy-700" />
      <span className="text-sm font-medium">{label}</span>
    </div>
  );
}
