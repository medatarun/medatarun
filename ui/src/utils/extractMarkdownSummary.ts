import { toString } from "mdast-util-to-string";
import remarkParse from "remark-parse";
import { unified } from "unified";

type MarkdownNode = {
  type: string;
  children?: MarkdownNode[];
};

/**
 * Builds a short plain-text preview from markdown content.
 * The extraction scans blocks in document order and returns the first useful one:
 * paragraph, list item, or heading (+ following paragraph when present).
 */
export function extractMarkdownSummary(
  markdown: string | null | undefined,
  maxChars: number,
): string | null {
  if (maxChars <= 0) {
    return null;
  }
  if (markdown === null || markdown === undefined) {
    return null;
  }
  if (markdown.trim() === "") {
    return null;
  }

  const ast = unified().use(remarkParse).parse(markdown) as MarkdownNode;
  if (!hasChildren(ast)) {
    return null;
  }

  for (let index = 0; index < ast.children.length; index += 1) {
    const node = ast.children[index];
    const text = extractTextFromNode(node, ast.children[index + 1]);
    if (text !== "") {
      return truncateText(text, maxChars);
    }
  }

  return null;
}

function extractTextFromNode(
  node: MarkdownNode,
  nextNode: MarkdownNode | undefined,
): string {
  if (node.type === "code" || node.type === "html" || node.type === "table") {
    return "";
  }

  if (node.type === "paragraph") {
    return normalizeText(toString(node));
  }

  if (node.type === "list") {
    return extractTextFromList(node);
  }

  if (node.type === "heading") {
    const headingText = normalizeText(toString(node));
    if (headingText === "") {
      return "";
    }
    if (nextNode !== undefined && nextNode.type === "paragraph") {
      const paragraphText = normalizeText(toString(nextNode));
      if (paragraphText !== "") {
        return normalizeText(headingText + " " + paragraphText);
      }
    }
    return headingText;
  }

  return "";
}

function extractTextFromList(node: MarkdownNode): string {
  if (!hasChildren(node)) {
    return "";
  }

  for (const listItem of node.children) {
    const listItemText = normalizeText(toString(listItem));
    if (listItemText !== "") {
      return listItemText;
    }
  }

  return "";
}

function normalizeText(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

function truncateText(value: string, maxChars: number): string {
  if (value.length <= maxChars) {
    return value;
  }

  const candidate = value.slice(0, maxChars + 1);
  const boundaryIndex = candidate.lastIndexOf(" ");
  const minAcceptedBoundary = Math.floor(maxChars * 0.6);
  if (boundaryIndex >= minAcceptedBoundary) {
    return candidate.slice(0, boundaryIndex).trimEnd() + "...";
  }

  return value.slice(0, maxChars).trimEnd() + "...";
}

function hasChildren(
  node: MarkdownNode | undefined,
): node is MarkdownNode & { children: MarkdownNode[] } {
  if (node === undefined) {
    return false;
  }
  if (node.children === undefined) {
    return false;
  }
  return Array.isArray(node.children);
}
