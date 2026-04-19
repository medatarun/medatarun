import {
  BoxRegular,
  ClipboardBulletListRegular,
  LinkRegular,
  ScanTypeRegular,
  TextBulletListSquareRegular,
} from "@fluentui/react-icons";

export const ModelIcon = ({
  authority,
  fontSize,
}: {
  authority: "canonical" | "system" | undefined;
  fontSize?: string | number | undefined;
}) => {
  const color =
    authority == "canonical"
      ? "green"
      : authority === "system"
        ? "blue"
        : "grey";
  return <BoxRegular style={{ color: color }} fontSize={fontSize} />;
};
export const EntityIcon = ClipboardBulletListRegular;
export const AttributeIcon = TextBulletListSquareRegular;
export const RelationshipIcon = LinkRegular;
export const TypeIcon = ScanTypeRegular;
