import { Caption1, tokens } from "@fluentui/react-components";

export function Key({ value }: { value: string }) {
  return (
    <Caption1>
      <code>
        <span style={{ color: tokens.colorNeutralForeground5 }}>[</span>
        <span style={{ color: tokens.colorNeutralForeground3 }}>{value}</span>
        <span style={{ color: tokens.colorNeutralForeground5 }}>]</span>
      </code>
    </Caption1>
  );
}
