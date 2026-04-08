import { describe, expect, it } from "vitest";

import { extractMarkdownSummary } from "./extractMarkdownSummary.ts";

describe("extractDescriptionPreview", () => {
  it("returns null when markdown is null", () => {
    const result = extractMarkdownSummary(null, 120);

    expect(result).toBeNull();
  });

  it("returns null when markdown is undefined", () => {
    const result = extractMarkdownSummary(undefined, 120);

    expect(result).toBeNull();
  });

  it("returns null when markdown is empty or whitespace", () => {
    const result = extractMarkdownSummary("  \n\t  ", 120);

    expect(result).toBeNull();
  });

  it("returns the first paragraph text", () => {
    const markdown = "\n\nFirst paragraph.\n\nSecond paragraph.";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBe("First paragraph.");
  });

  it("returns list item text when the list appears before any paragraph", () => {
    const markdown =
      "- First useful line\n- Another line\n\nA later paragraph.";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBe("First useful line");
  });

  it("returns heading and following paragraph when heading is first informative block", () => {
    const markdown =
      "# Context\n\nUseful details for the user.\n\nAnother section.";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBe("Context Useful details for the user.");
  });

  it("ignores code and html blocks before extracting text", () => {
    const markdown =
      "```ts\nconst a = 1;\n```\n\n<div>ignored block</div>\n\nActual paragraph.";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBe("Actual paragraph.");
  });

  it("keeps readable text for markdown formatting and links", () => {
    const markdown = "Use **bold** and [documentation](https://example.com).";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBe("Use bold and documentation.");
  });

  it("truncates on a word boundary and appends ellipsis", () => {
    const markdown = "One two three four five six seven";

    const result = extractMarkdownSummary(markdown, 15);

    expect(result).toBe("One two three...");
  });

  it("returns null when markdown has no informative text", () => {
    const markdown = "```txt\nonly code\n```";

    const result = extractMarkdownSummary(markdown, 120);

    expect(result).toBeNull();
  });
});
