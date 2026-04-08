import { extractMarkdownSummary } from "@/utils/extractMarkdownSummary.ts";

export function MarkdownSummary({
  value,
  maxChars = 150,
}: {
  value: string | null | undefined;
  maxChars: number;
}) {
  return extractMarkdownSummary(value, maxChars);
}
