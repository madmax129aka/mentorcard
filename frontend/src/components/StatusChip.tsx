type ChipVariant = "loaded" | "pending" | "na";

const LABEL: Record<ChipVariant, string> = {
  loaded: "Loaded",
  pending: "Pending",
  na: "Not applicable",
};

const DOT: Record<ChipVariant, string> = {
  loaded: "●",
  pending: "●",
  na: "●",
};

export function StatusChip({
  variant,
  label,
}: {
  variant: ChipVariant;
  label?: string;
}) {
  return (
    <span className={`status-chip status-chip--${variant}`}>
      <span aria-hidden>{DOT[variant]}</span>
      {label ?? LABEL[variant]}
    </span>
  );
}
