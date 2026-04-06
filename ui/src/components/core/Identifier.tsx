import { Button, Caption1, tokens } from "@fluentui/react-components";
import { CopyRegular } from "@fluentui/react-icons";

export function Identifier({ value }: { value: string }) {
  const handleCopy = () => {
    navigator.clipboard.writeText(value).catch(() => undefined);
  };

  return (
    <Caption1>
      <code>
        <span style={{ color: tokens.colorNeutralForeground5 }}>[</span>
        <span style={{ color: tokens.colorNeutralForeground3 }}>{value}</span>
        <span style={{ color: tokens.colorNeutralForeground5 }}>]</span>
      </code>
      <Button
        appearance="transparent"
        size="small"
        icon={<CopyRegular />}
        onClick={handleCopy}
      />
    </Caption1>
  );
}
