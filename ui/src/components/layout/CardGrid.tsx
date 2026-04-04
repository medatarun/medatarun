import { Card, CardHeader, tokens } from "@fluentui/react-components";
import type { ReactNode } from "react";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";

interface CardGridItemType {
  id: string;
}

export function CardGrid<T extends CardGridItemType>({
  data,
  columns = 3,
  renderName,
  renderDescription,
  renderBody,
  renderEmpty,
}: {
  data: T[];
  columns?: number;
  renderName: (item: T) => ReactNode;
  renderDescription: (item: T) => ReactNode;
  renderBody: (item: T) => ReactNode;
  renderEmpty: () => ReactNode;
}) {
  return (
    <>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: `repeat(${columns}, 1fr)`,
          columnGap: tokens.spacingHorizontalM,
          rowGap: tokens.spacingVerticalM,
          marginTop: tokens.spacingVerticalL,
        }}
      >
        {data.map((item) => (
          <CardGridItem
            key={item.id}
            item={item}
            renderName={renderName}
            renderDescription={renderDescription}
            renderBody={renderBody}
          />
        ))}
      </div>
      {data.length === 0 && (
        <MissingInformation>{renderEmpty()}</MissingInformation>
      )}
    </>
  );
}

export function CardGridItem<T extends CardGridItemType>({
  item,
  renderName,
  renderDescription,
  renderBody,
}: {
  item: T;
  renderName: (item: T) => ReactNode;
  renderDescription: (item: T) => ReactNode;
  renderBody: (item: T) => ReactNode;
}) {
  return (
    <Card style={{ height: "fit-content", maxWidth: "100%" }} key={item.id}>
      <CardHeader
        header={<div>{renderName(item)}</div>}
        description={<div>{renderDescription(item)}</div>}
      />
      {renderBody(item)}
    </Card>
  );
}
