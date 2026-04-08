import { Identifier } from "@/components/core/Identifier.tsx";
import { Key } from "@/components/core/Key.tsx";
import { Caption1, tokens } from "@fluentui/react-components";
import { Fragment } from "react";
import type { ReactNode } from "react";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";

type ViewLayoutTechnicalInfosProps = {
  technicalKey?: string;
  id?: string;
  keyLabel?: string;
  idLabel?: string;
};

export function ViewLayoutTechnicalInfos({
  technicalKey,
  id,
  keyLabel,
  idLabel,
}: ViewLayoutTechnicalInfosProps) {
  // Hooks
  const detailLevelContext = useDetailLevelContext();

  // Don't display that to business users
  if (!detailLevelContext.isDetailLevelTech) return null;

  // Derived - compose depending on if key or id have been provided
  const keyLabelValue = keyLabel ?? "key";
  const idLabelValue = idLabel ?? "id";
  const items: Array<{ label: string; content: ReactNode }> = [];
  if (technicalKey) {
    items.push({ label: keyLabelValue, content: <Key value={technicalKey} /> });
  }
  if (id) {
    items.push({ label: idLabelValue, content: <Identifier value={id} /> });
  }

  // Dont display if nothing to display
  if (items.length === 0) {
    return null;
  }

  return (
    <p
      style={{
        marginTop: "12em",
        borderTop: "1px solid #CCC",
        textAlign: "right",
      }}
    >
      <Caption1 style={{ color: tokens.colorNeutralForeground5 }}>
        {items.map((item, index) => (
          <Fragment key={String(index)}>
            {index > 0 ? " - " : null}
            {item.label}: {item.content}
          </Fragment>
        ))}
      </Caption1>
    </p>
  );
}
