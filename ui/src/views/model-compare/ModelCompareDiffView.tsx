/**
 * HONEST NOTE:
 * This file was built with heavy AI help and many quick refactors.
 * The result is ugly, hard to follow, and not at our normal code quality level.
 *
 * We did this on purpose to test many UI comparison ideas very fast with real data.
 * It helped us validate ergonomics and reject bad visual options.
 *
 * So yes, this code is messy.
 * We know it.
 *
 * Also we know we don't want to keep this visual and try other ways because reading
 * is too hard for business users.
 *
 * We keep it for now because it already covers a good part of the business need.
 * It stays temporary until we replace it with cleaner comparison views
 * (see TODO: [COMPARE_UI_VARIANTS]).
 */
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
import { type ReactNode, useState } from "react";

export function ModelCompareDiffView({ diff }: { diff: ModelCompareDto }) {
  const { t } = useAppI18n();

  if (diff.entries.length === 0) {
    return (
      <div
        style={{
          marginTop: tokens.spacingVerticalM,
          padding: tokens.spacingHorizontalM,
          borderRadius: tokens.borderRadiusMedium,
          background: tokens.colorNeutralBackground3,
        }}
      >
        <Text>{t("modelComparePage_noDifferences")}</Text>
      </div>
    );
  }

  const modelEntries = diff.entries.filter(
    (entry) => entry.objectType === "model",
  );
  const typeEntries = diff.entries.filter(
    (entry) => entry.objectType === "type",
  );
  const entityEntries = diff.entries.filter(
    (entry) =>
      entry.objectType === "entity" || entry.objectType === "entityAttribute",
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
  const modified = entries.filter(
    (entry) => entry.status === "MODIFIED",
  ).length;

  return (
    <div
      style={{
        display: "flex",
        gap: tokens.spacingHorizontalS,
        flexWrap: "wrap",
      }}
    >
      <SummaryPill label="Added" value={added} tone="added" />
      <SummaryPill label="Deleted" value={deleted} tone="deleted" />
      <SummaryPill label="Modified" value={modified} tone="modified" />
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
  const status = toStatusFromTone(tone);
  const visual = toStatusVisual(status);

  return (
    <div
      style={{
        background: visual.backgroundSoft,
        border: "1px solid var(--colorNeutralStroke2)",
        borderRadius: tokens.borderRadiusMedium,
        padding: tokens.spacingVerticalXS + " " + tokens.spacingHorizontalS,
        display: "inline-flex",
        gap: tokens.spacingHorizontalXS,
        alignItems: "center",
      }}
    >
      <Text>{label}</Text>
      <Text weight="semibold" style={{ color: visual.accent }}>
        {value}
      </Text>
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
        status={<Text weight="semibold"> </Text>}
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
      <SpacerRow height={tokens.spacingVerticalXL} />
      <SectionRow title={title} />
      {Array.from(grouped.entries())
        .sort((left, right) => left[0].localeCompare(right[0]))
        .map(([groupKey, groupedEntries]) => (
          <GroupRows
            key={title + "-" + groupKey}
            groupKey={groupKey}
            entries={groupedEntries}
          />
        ))}
    </>
  );
}

function SectionRow({ title }: { title: string }) {
  return (
    <DiffRow
      hierarchy={
        <Text
          style={{
            fontSize: "1.05em",
            letterSpacing: "0.01em",
            fontWeight: tokens.fontWeightRegular,
          }}
        >
          ▸ {title}
        </Text>
      }
      status={null}
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
  const parentEntry = entries.find((entry) => {
    if (entry.objectType !== "entity" && entry.objectType !== "relationship") {
      return false;
    }
    return toObjectBusinessLabel(entry) === groupKey;
  });
  const children = entries.filter((entry) => entry !== parentEntry);
  const sorted = sortEntries(children);
  const parentVisual =
    parentEntry == null ? null : toStatusVisual(parentEntry.status);
  const parentAccent = parentVisual == null ? undefined : parentVisual.accent;
  const parentBackground =
    parentVisual == null ? undefined : parentVisual.backgroundStrong;
  const parentFieldRows = parentEntry == null ? [] : toFieldRows(parentEntry);
  const defaultOpen =
    parentEntry == null ? true : parentEntry.status === "MODIFIED";
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <>
      <SpacerRow height={tokens.spacingVerticalM} />
      <DiffRow
        hierarchy={
          <IndentedText level={1} kind="group">
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
            {groupHeaderLabel(groupKey, parentEntry)}
          </IndentedText>
        }
        status={
          parentEntry == null ? null : (
            <StatusDot accentColor={parentAccent ?? "inherit"} />
          )
        }
        left={null}
        right={null}
        background={parentBackground}
        hierarchyAccent={parentAccent}
      />
      {isOpen &&
        parentEntry != null &&
        parentFieldRows.map((fieldRow) => (
          <DiffRow
            key={groupKey + "-parent-field-" + fieldRow.field}
            hierarchy={
              <IndentedText level={2} kind="field">
                ↳ {toBusinessFieldLabel(fieldRow.field)}
              </IndentedText>
            }
            status={null}
            left={<Text>{fieldRow.leftValue}</Text>}
            right={<Text>{fieldRow.rightValue}</Text>}
            background={
              parentVisual == null ? undefined : parentVisual.backgroundSubtle
            }
            hierarchyAccent={parentAccent}
          />
        ))}
      {isOpen &&
        sorted.map((entry, index) => (
          <EntryRows key={groupKey + "-" + index} entry={entry} level={2} />
        ))}
    </>
  );
}

function EntryRows({
  entry,
  level,
}: {
  entry: ModelCompareEntryDto;
  level: number;
}) {
  const label = lineObjectLabel(entry);
  const objectLabel = toObjectBusinessLabel(entry);
  const fieldRows = toFieldRows(entry);
  const defaultOpen = entry.status === "MODIFIED";
  const [isOpen, setIsOpen] = useState(defaultOpen);
  const canExpand = fieldRows.length > 0;
  const visual = toStatusVisual(entry.status);
  const parentLevel = isPrimaryObjectType(entry.objectType);
  const rowBackground = parentLevel
    ? visual.backgroundStrong
    : visual.backgroundSoft;
  const rowAccent = parentLevel ? visual.accent : visual.accentSoft;

  return (
    <>
      <DiffRow
        hierarchy={
          <IndentedText level={level} kind="object">
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
        status={<StatusDot accentColor={rowAccent} />}
        left={null}
        right={null}
        background={rowBackground}
        hierarchyAccent={rowAccent}
      />
      {isOpen &&
        fieldRows.map((fieldRow) => (
          <DiffRow
            key={objectLabel + "-" + fieldRow.field}
            hierarchy={
              <IndentedText level={level + 1} kind="field">
                ↳ {toBusinessFieldLabel(fieldRow.field)}
              </IndentedText>
            }
            status={null}
            left={<Text>{fieldRow.leftValue}</Text>}
            right={<Text>{fieldRow.rightValue}</Text>}
            background={visual.backgroundSubtle}
            hierarchyAccent={rowAccent}
          />
        ))}
    </>
  );
}

function DiffRow({
  hierarchy,
  status,
  left,
  right,
  background,
  hierarchyAccent,
}: {
  hierarchy: ReactNode;
  status: ReactNode;
  left: ReactNode;
  right: ReactNode;
  background?: string;
  hierarchyAccent?: string;
}) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "44% 4% 26% 26%",
        borderBottom: "1px solid var(--colorNeutralStroke2)",
        background: background,
      }}
    >
      <Cell leftAccentColor={hierarchyAccent}>{hierarchy}</Cell>
      <Cell>{status}</Cell>
      <Cell>{left}</Cell>
      <Cell>{right}</Cell>
    </div>
  );
}

function Cell({
  children,
  background,
  leftAccentColor,
}: {
  children: ReactNode;
  background?: string;
  leftAccentColor?: string;
}) {
  return (
    <div
      style={{
        padding: tokens.spacingVerticalXS + " " + tokens.spacingHorizontalS,
        borderRight: "1px solid var(--colorNeutralStroke2)",
        background: background,
        boxShadow:
          leftAccentColor == null
            ? undefined
            : "inset 3px 0 0 " + leftAccentColor,
      }}
    >
      {children}
    </div>
  );
}

function IndentedText({
  level,
  kind,
  children,
}: {
  level: number;
  kind: "group" | "object" | "field";
  children: ReactNode;
}) {
  const styleByKind =
    kind === "group"
      ? {
          fontSize: "1.05em",
          fontWeight: tokens.fontWeightRegular,
          color: "var(--colorNeutralForeground1)",
          paddingTop: tokens.spacingVerticalM,
        }
      : kind === "object"
        ? {
            fontSize: "1em",
            fontWeight: tokens.fontWeightRegular,
            color: tokens.colorNeutralForeground1,
            paddingTop: "0",
          }
        : {
            fontSize: "1em",
            fontWeight: tokens.fontWeightRegular,
            color: "var(--colorNeutralForeground3)",
            paddingTop: "0",
          };

  return (
    <Text
      style={{
        paddingLeft: level * 16,
        display: "block",
        fontSize: styleByKind.fontSize,
        fontWeight: styleByKind.fontWeight,
        color: styleByKind.color,
        paddingTop: styleByKind.paddingTop,
      }}
    >
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

function toAddedFieldRows(
  snapshot: Record<string, unknown> | null,
): FieldRow[] {
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

function toDeletedFieldRows(
  snapshot: Record<string, unknown> | null,
): FieldRow[] {
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

function sortEntries(entries: ModelCompareEntryDto[]): ModelCompareEntryDto[] {
  return [...entries].sort((left, right) => {
    const leftPriority = objectTypePriority(left.objectType);
    const rightPriority = objectTypePriority(right.objectType);
    if (leftPriority !== rightPriority) {
      return leftPriority - rightPriority;
    }
    return toObjectBusinessLabel(left).localeCompare(
      toObjectBusinessLabel(right),
    );
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
        <span
          style={{
            display: "inline-flex",
            color: "var(--colorNeutralForeground3)",
          }}
        >
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
  if (entry.objectType === "entityAttribute")
    return entry.attributeKey ?? "unknown";
  if (entry.objectType === "relationship")
    return entry.relationshipKey ?? "unknown";
  if (entry.objectType === "relationshipRole")
    return entry.roleKey ?? "unknown";
  if (entry.objectType === "relationshipAttribute")
    return entry.attributeKey ?? "unknown";
  return entry.objectType;
}

function groupHeaderLabel(
  groupKey: string,
  parentEntry: ModelCompareEntryDto | undefined,
): ReactNode {
  if (parentEntry == null) {
    return "• " + groupKey;
  }
  const icon = toObjectIcon(parentEntry.objectType);
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: tokens.spacingHorizontalXXS,
      }}
    >
      {icon != null && (
        <span
          style={{
            display: "inline-flex",
            color: "var(--colorNeutralForeground3)",
          }}
        >
          {icon}
        </span>
      )}
      <span>{groupKey}</span>
    </span>
  );
}

function toObjectIcon(objectType: string): ReactNode | null {
  if (objectType === "model")
    return <ModelIcon authority={undefined} fontSize={14} />;
  if (objectType === "type") return <TypeIcon fontSize={14} />;
  if (objectType === "entity") return <EntityIcon fontSize={14} />;
  if (objectType === "entityAttribute") return <AttributeIcon fontSize={14} />;
  if (objectType === "relationship") return <RelationshipIcon fontSize={14} />;
  if (objectType === "relationshipRole")
    return <RelationshipIcon fontSize={14} />;
  if (objectType === "relationshipAttribute")
    return <AttributeIcon fontSize={14} />;
  return null;
}

interface FieldRow {
  field: string;
  leftValue: string;
  rightValue: string;
}

function SpacerRow({ height }: { height: string }) {
  return <div style={{ height: height }} />;
}

function StatusDot({ accentColor }: { accentColor: string }) {
  return (
    <span
      style={{
        display: "inline-flex",
        width: "100%",
        justifyContent: "center",
        color: accentColor,
        fontSize: "1em",
        lineHeight: 1,
      }}
    >
      ●
    </span>
  );
}

function toStatusFromTone(tone: "added" | "deleted" | "modified"): string {
  if (tone === "added") return "ADDED";
  if (tone === "deleted") return "DELETED";
  return "MODIFIED";
}

function toStatusVisual(status: string): {
  accent: string;
  accentSoft: string;
  backgroundStrong: string;
  backgroundSoft: string;
  backgroundSubtle: string;
} {
  if (status === "ADDED") {
    return {
      accent: "rgb(22, 163, 74)",
      accentSoft: "rgba(22, 163, 74, 0.7)",
      backgroundStrong: "rgba(22, 163, 74, 0.14)",
      backgroundSoft: "rgba(22, 163, 74, 0.08)",
      backgroundSubtle: "rgba(22, 163, 74, 0.05)",
    };
  }
  if (status === "DELETED") {
    return {
      accent: "rgb(220, 38, 38)",
      accentSoft: "rgba(220, 38, 38, 0.7)",
      backgroundStrong: "rgba(220, 38, 38, 0.14)",
      backgroundSoft: "rgba(220, 38, 38, 0.08)",
      backgroundSubtle: "rgba(220, 38, 38, 0.05)",
    };
  }
  return {
    accent: "rgb(217, 119, 6)",
    accentSoft: "rgba(217, 119, 6, 0.7)",
    backgroundStrong: "rgba(217, 119, 6, 0.14)",
    backgroundSoft: "rgba(217, 119, 6, 0.08)",
    backgroundSubtle: "rgba(217, 119, 6, 0.05)",
  };
}

function isPrimaryObjectType(objectType: string): boolean {
  if (objectType === "model") return true;
  if (objectType === "type") return true;
  if (objectType === "entity") return true;
  if (objectType === "relationship") return true;
  return false;
}
