import type {
  ModelCompareDto,
  ModelCompareEntryDto,
} from "@/business/model/model.dto.ts";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
  TypeIcon,
} from "@/components/business/model/model.icons.tsx";
import { Text, tokens } from "@fluentui/react-components";
import { useAppI18n } from "@/services/appI18n.tsx";
import { useState, type ReactNode } from "react";

export function ModelCompareDiffView({ diff }: { diff: ModelCompareDto }) {
  const { t } = useAppI18n();

  if (diff.entries.length === 0) {
    return (
      <div
        style={{
          marginTop: tokens.spacingVerticalM,
          padding: tokens.spacingHorizontalM,
          borderRadius: tokens.borderRadiusMedium,
          background: "var(--colorNeutralBackground3)",
        }}
      >
        <Text>{t("modelComparePage_noDifferences")}</Text>
      </div>
    );
  }

  const modelEntries = diff.entries.filter((entry) => entry.objectType === "model");
  const typeEntries = diff.entries.filter((entry) => entry.objectType === "type");
  const entityEntries = diff.entries.filter(
    (entry) => entry.objectType === "entity" || entry.objectType === "entityAttribute",
  );
  const relationshipEntries = diff.entries.filter(
    (entry) =>
      entry.objectType === "relationship" ||
      entry.objectType === "relationshipRole" ||
      entry.objectType === "relationshipAttribute",
  );

  return (
    <div
      style={{
        marginTop: tokens.spacingVerticalM,
        display: "grid",
        gap: tokens.spacingVerticalM,
      }}
    >
      <SummaryBar entries={diff.entries} />
      <DiffGrid>
        {modelEntries.length > 0 && (
          <FlatSectionRows title="Model" entries={modelEntries} />
        )}
        {typeEntries.length > 0 && (
          <FlatSectionRows title="Types" entries={typeEntries} />
        )}
        {entityEntries.length > 0 && (
          <GroupedSectionRows
            title="Entities"
            entries={entityEntries}
            groupBy={(entry) => entry.entityKey ?? "unknown-entity"}
          />
        )}
        {relationshipEntries.length > 0 && (
          <GroupedSectionRows
            title="Relationships"
            entries={relationshipEntries}
            groupBy={(entry) => entry.relationshipKey ?? "unknown-relationship"}
          />
        )}
      </DiffGrid>
    </div>
  );
}

function SummaryBar({ entries }: { entries: ModelCompareEntryDto[] }) {
  const added = entries.filter((entry) => entry.status === "ADDED").length;
  const deleted = entries.filter((entry) => entry.status === "DELETED").length;
  const modified = entries.filter((entry) => entry.status === "MODIFIED").length;

  return (
    <div
      style={{
        display: "flex",
        gap: tokens.spacingHorizontalS,
        flexWrap: "wrap",
      }}
    >
      <SummaryPill label="+ Added" value={added} tone="added" />
      <SummaryPill label="- Deleted" value={deleted} tone="deleted" />
      <SummaryPill label="~ Modified" value={modified} tone="modified" />
    </div>
  );
}

function SummaryPill({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: "added" | "deleted" | "modified";
}) {
  const background =
    tone === "added"
      ? "var(--colorPaletteGreenBackground1)"
      : tone === "deleted"
        ? "var(--colorPaletteRedBackground1)"
        : "var(--colorPaletteYellowBackground1)";

  return (
    <div
      style={{
        background: background,
        border: "1px solid var(--colorNeutralStroke2)",
        borderRadius: tokens.borderRadiusMedium,
        padding: tokens.spacingVerticalXS + " " + tokens.spacingHorizontalS,
        display: "inline-flex",
        gap: tokens.spacingHorizontalXS,
        alignItems: "center",
      }}
    >
      <Text>{label}</Text>
      <Text weight="semibold">{value}</Text>
    </div>
  );
}

function DiffGrid({ children }: { children: ReactNode }) {
  return (
    <div
      style={{
        border: "1px solid var(--colorNeutralStroke2)",
        borderRadius: tokens.borderRadiusMedium,
        overflow: "hidden",
      }}
    >
      <DiffRow
        hierarchy={<Text weight="semibold">Hierarchy</Text>}
        left={<Text weight="semibold">Left</Text>}
        right={<Text weight="semibold">Right</Text>}
        background="var(--colorNeutralBackground3)"
      />
      {children}
    </div>
  );
}

function FlatSectionRows({
  title,
  entries,
}: {
  title: string;
  entries: ModelCompareEntryDto[];
}) {
  const sorted = sortEntries(entries);
  return (
    <>
      <SectionRow title={title} />
      {sorted.map((entry, index) => (
        <EntryRows key={title + "-" + index} entry={entry} level={1} />
      ))}
    </>
  );
}

function GroupedSectionRows({
  title,
  entries,
  groupBy,
}: {
  title: string;
  entries: ModelCompareEntryDto[];
  groupBy: (entry: ModelCompareEntryDto) => string;
}) {
  const grouped = new Map<string, ModelCompareEntryDto[]>();
  entries.forEach((entry) => {
    const key = groupBy(entry);
    const value = grouped.get(key);
    if (value == null) {
      grouped.set(key, [entry]);
      return;
    }
    value.push(entry);
  });

  return (
    <>
      <SectionRow title={title} />
      {Array.from(grouped.entries())
        .sort((left, right) => left[0].localeCompare(right[0]))
        .map(([groupKey, groupedEntries]) => (
          <GroupRows key={title + "-" + groupKey} groupKey={groupKey} entries={groupedEntries} />
        ))}
    </>
  );
}

function SectionRow({ title }: { title: string }) {
  return (
    <DiffRow
      hierarchy={<Text weight="semibold">▸ {title}</Text>}
      left={null}
      right={null}
      background="var(--colorNeutralBackground2)"
    />
  );
}

function GroupRows({
  groupKey,
  entries,
}: {
  groupKey: string;
  entries: ModelCompareEntryDto[];
}) {
  const sorted = sortEntries(entries);
  return (
    <>
      <DiffRow hierarchy={<IndentedText level={1}>• {groupKey}</IndentedText>} left={null} right={null} />
      {sorted.map((entry, index) => (
        <EntryRows key={groupKey + "-" + index} entry={entry} level={2} />
      ))}
    </>
  );
}

function EntryRows({ entry, level }: { entry: ModelCompareEntryDto; level: number }) {
  const label = lineObjectLabel(entry);
  const objectLabel = toObjectBusinessLabel(entry);
  const fieldRows = toFieldRows(entry);
  const sideSummaries = entrySideSummary(entry);
  const entryCellColors = toEntryCellColors(entry.status);
  const fieldCellColors = toFieldCellColors(entry.status);
  const defaultOpen = entry.status === "MODIFIED";
  const [isOpen, setIsOpen] = useState(defaultOpen);
  const canExpand = fieldRows.length > 0;

  return (
    <>
      <DiffRow
        hierarchy={
          <IndentedText level={level}>
            {canExpand ? (
              <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                style={{
                  border: "none",
                  background: "transparent",
                  cursor: "pointer",
                  padding: 0,
                  marginRight: tokens.spacingHorizontalXXS,
                  width: 16,
                  textAlign: "left",
                  color: "var(--colorNeutralForeground2)",
                }}
              >
                {isOpen ? "▾" : "▸"}
              </button>
            ) : (
              <span
                style={{
                  display: "inline-block",
                  width: 16,
                  marginRight: tokens.spacingHorizontalXXS,
                }}
              />
            )}
            {label}
          </IndentedText>
        }
        left={<Text>{sideSummaries.left}</Text>}
        right={<Text>{sideSummaries.right}</Text>}
        leftBackground={entryCellColors.leftBackground}
        rightBackground={entryCellColors.rightBackground}
      />
      {isOpen &&
        fieldRows.map((fieldRow) => (
        <DiffRow
          key={objectLabel + "-" + fieldRow.field}
          hierarchy={
            <IndentedText level={level + 1}>↳ {toBusinessFieldLabel(fieldRow.field)}</IndentedText>
          }
          left={<Text>{fieldRow.leftValue}</Text>}
          right={<Text>{fieldRow.rightValue}</Text>}
          background="var(--colorNeutralBackground1)"
          leftBackground={fieldCellColors.leftBackground}
          rightBackground={fieldCellColors.rightBackground}
        />
      ))}
    </>
  );
}

function DiffRow({
  hierarchy,
  left,
  right,
  background,
  leftBackground,
  rightBackground,
}: {
  hierarchy: ReactNode;
  left: ReactNode;
  right: ReactNode;
  background?: string;
  leftBackground?: string;
  rightBackground?: string;
}) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "45% 27.5% 27.5%",
        borderBottom: "1px solid var(--colorNeutralStroke2)",
        background: background,
      }}
    >
      <Cell>{hierarchy}</Cell>
      <Cell background={leftBackground}>{left}</Cell>
      <Cell background={rightBackground}>{right}</Cell>
    </div>
  );
}

function Cell({ children, background }: { children: ReactNode; background?: string }) {
  return (
    <div
      style={{
        padding: tokens.spacingVerticalXS + " " + tokens.spacingHorizontalS,
        borderRight: "1px solid var(--colorNeutralStroke2)",
        background: background,
      }}
    >
      {children}
    </div>
  );
}

function IndentedText({
  level,
  children,
}: {
  level: number;
  children: ReactNode;
}) {
  return (
    <Text style={{ paddingLeft: level * 16, display: "block" }}>
      {children}
    </Text>
  );
}

function toFieldRows(entry: ModelCompareEntryDto): FieldRow[] {
  if (entry.status === "ADDED") {
    return toAddedFieldRows(entry.right);
  }
  if (entry.status === "DELETED") {
    return toDeletedFieldRows(entry.left);
  }
  return toModifiedFieldRows(entry.left, entry.right);
}

function toAddedFieldRows(snapshot: Record<string, unknown> | null): FieldRow[] {
  if (snapshot == null) return [];
  const keys = sortedFieldKeys(snapshot);
  const rows: FieldRow[] = [];
  keys.forEach((key) => {
    rows.push({
      field: key,
      leftValue: "",
      rightValue: formatBusinessValue(snapshot[key]) ?? "",
    });
  });
  return rows;
}

function toDeletedFieldRows(snapshot: Record<string, unknown> | null): FieldRow[] {
  if (snapshot == null) return [];
  const keys = sortedFieldKeys(snapshot);
  const rows: FieldRow[] = [];
  keys.forEach((key) => {
    rows.push({
      field: key,
      leftValue: formatBusinessValue(snapshot[key]) ?? "",
      rightValue: "",
    });
  });
  return rows;
}

function toModifiedFieldRows(
  left: Record<string, unknown> | null,
  right: Record<string, unknown> | null,
): FieldRow[] {
  if (left == null || right == null) return [];
  const keys = collectComparedKeys(left, right);
  const rows: FieldRow[] = [];
  keys.forEach((key) => {
    const leftRaw = left[key];
    const rightRaw = right[key];
    if (valuesEqual(leftRaw, rightRaw)) return;
    rows.push({
      field: key,
      leftValue: formatBusinessValue(leftRaw) ?? "",
      rightValue: formatBusinessValue(rightRaw) ?? "",
    });
  });
  return rows;
}

function collectComparedKeys(
  left: Record<string, unknown> | null,
  right: Record<string, unknown> | null,
): string[] {
  const keySet = new Set<string>();
  if (left != null) {
    Object.keys(left).forEach((key) => keySet.add(key));
  }
  if (right != null) {
    Object.keys(right).forEach((key) => keySet.add(key));
  }
  return Array.from(keySet)
    .filter((key) => key !== "objectType")
    .sort((a, b) => compareFieldKey(a, b));
}

function valuesEqual(left: unknown, right: unknown): boolean {
  return JSON.stringify(left) === JSON.stringify(right);
}

function formatBusinessValue(value: unknown): string | null {
  if (value == null) return null;
  if (typeof value === "string") return value;
  if (typeof value === "number") return String(value);
  if (typeof value === "boolean") return value ? "Yes" : "No";
  if (Array.isArray(value)) {
    const printable = value
      .map((item) => formatBusinessValue(item) ?? "")
      .filter((item) => item.length > 0);
    return printable.join(", ");
  }
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
}

function entrySideSummary(entry: ModelCompareEntryDto): { left: string; right: string } {
  if (entry.status === "ADDED") {
    return { left: "", right: "Added" };
  }
  if (entry.status === "DELETED") {
    return { left: "Deleted", right: "" };
  }
  return { left: "Changed", right: "Changed" };
}

function sortEntries(entries: ModelCompareEntryDto[]): ModelCompareEntryDto[] {
  return [...entries].sort((left, right) => {
    const leftPriority = objectTypePriority(left.objectType);
    const rightPriority = objectTypePriority(right.objectType);
    if (leftPriority !== rightPriority) {
      return leftPriority - rightPriority;
    }
    return toObjectBusinessLabel(left).localeCompare(toObjectBusinessLabel(right));
  });
}

function objectTypePriority(objectType: string): number {
  const priorities: Record<string, number> = {
    model: 0,
    type: 1,
    entity: 2,
    entityAttribute: 3,
    relationship: 4,
    relationshipRole: 5,
    relationshipAttribute: 6,
  };
  return priorities[objectType] ?? 99;
}

function sortedFieldKeys(snapshot: Record<string, unknown>): string[] {
  return Object.keys(snapshot)
    .filter((key) => key !== "objectType")
    .sort((a, b) => compareFieldKey(a, b));
}

function compareFieldKey(left: string, right: string): number {
  const leftPriority = fieldPriority(left);
  const rightPriority = fieldPriority(right);
  if (leftPriority !== rightPriority) {
    return leftPriority - rightPriority;
  }
  return left.localeCompare(right);
}

function fieldPriority(field: string): number {
  const priorities: Record<string, number> = {
    key: 0,
    name: 1,
    description: 2,
    typeKey: 3,
    optional: 4,
    cardinality: 5,
    roleKey: 6,
    entityKey: 7,
    attributeKey: 8,
    identifierAttributeKey: 9,
    authority: 10,
    version: 11,
    origin: 12,
    documentationHome: 13,
    tags: 14,
  };
  return priorities[field] ?? 99;
}

function toBusinessFieldLabel(field: string): string {
  const map: Record<string, string> = {
    key: "Key",
    name: "Name",
    description: "Description",
    version: "Version",
    origin: "Origin",
    authority: "Authority",
    documentationHome: "Documentation",
    tags: "Tags",
    identifierAttributeKey: "Identifier attribute",
    typeKey: "Type",
    optional: "Optional",
    entityKey: "Entity",
    cardinality: "Cardinality",
  };
  return map[field] ?? field;
}

function lineObjectLabel(entry: ModelCompareEntryDto): ReactNode {
  const icon = toObjectIcon(entry.objectType);
  const label = toObjectBusinessLabel(entry);
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: tokens.spacingHorizontalXXS,
      }}
    >
      {icon != null && (
        <span style={{ display: "inline-flex", color: "var(--colorNeutralForeground3)" }}>
          {icon}
        </span>
      )}
      <span>{label}</span>
    </span>
  );
}

function toObjectBusinessLabel(entry: ModelCompareEntryDto): string {
  if (entry.objectType === "model") return "Model";
  if (entry.objectType === "type") return entry.typeKey ?? "unknown";
  if (entry.objectType === "entity") return entry.entityKey ?? "unknown";
  if (entry.objectType === "entityAttribute") return entry.attributeKey ?? "unknown";
  if (entry.objectType === "relationship") return entry.relationshipKey ?? "unknown";
  if (entry.objectType === "relationshipRole") return entry.roleKey ?? "unknown";
  if (entry.objectType === "relationshipAttribute") return entry.attributeKey ?? "unknown";
  return entry.objectType;
}

function toObjectIcon(objectType: string): ReactNode | null {
  if (objectType === "model") return <ModelIcon fontSize={14} />;
  if (objectType === "type") return <TypeIcon fontSize={14} />;
  if (objectType === "entity") return <EntityIcon fontSize={14} />;
  if (objectType === "entityAttribute") return <AttributeIcon fontSize={14} />;
  if (objectType === "relationship") return <RelationshipIcon fontSize={14} />;
  if (objectType === "relationshipRole") return <RelationshipIcon fontSize={14} />;
  if (objectType === "relationshipAttribute") return <AttributeIcon fontSize={14} />;
  return null;
}

interface FieldRow {
  field: string;
  leftValue: string;
  rightValue: string;
}

function toEntryCellColors(status: string): {
  leftBackground?: string;
  rightBackground?: string;
} {
  if (status === "ADDED") {
    return {
      rightBackground: "rgba(34, 197, 94, 0.14)",
    };
  }
  if (status === "DELETED") {
    return {
      leftBackground: "rgba(239, 68, 68, 0.14)",
    };
  }
  if (status === "MODIFIED") {
    return {
      leftBackground: "rgba(249, 115, 22, 0.16)",
      rightBackground: "rgba(249, 115, 22, 0.16)",
    };
  }
  return {};
}

function toFieldCellColors(status: string): {
  leftBackground?: string;
  rightBackground?: string;
} {
  if (status === "ADDED") {
    return {
      rightBackground: "rgba(34, 197, 94, 0.14)",
    };
  }
  if (status === "DELETED") {
    return {
      leftBackground: "rgba(239, 68, 68, 0.14)",
    };
  }
  if (status === "MODIFIED") {
    return {
      leftBackground: "rgba(249, 115, 22, 0.16)",
      rightBackground: "rgba(249, 115, 22, 0.16)",
    };
  }
  return {};
}
