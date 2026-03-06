import {useSecurityRuleDescriptionRegistry} from "@/business/config";
import {Badge, makeStyles, Text, Tooltip} from "@fluentui/react-components";
import {CodeRegular, ShieldRegular} from "@fluentui/react-icons";
import Markdown from "react-markdown";

const useStyles = makeStyles({
  codeLine: {
    display: "inline-flex",
    alignItems: "center",
    gap: "0.35rem",
  },
});

export function SecurityRuleBadge(
  {
    securityRule,
  }: {
    securityRule: string;
  }) {
  const styles = useStyles();
  const { registry: securityRuleDescriptionRegistry } =
    useSecurityRuleDescriptionRegistry();
  const name = securityRuleDescriptionRegistry.findNameOrDefault(securityRule);
  const description = securityRuleDescriptionRegistry.findDescription(securityRule);

  const badge = (
    <Badge
      appearance="tint"
      color="brand"
      icon={<ShieldRegular />}
    >
      {name}
    </Badge>
  );

  if (!description) {
    return badge;
  }

  return (
    <Tooltip
      content={
        <>
          <p><Text weight="bold">Security rule</Text></p>
          <p><Text>{name}</Text></p>
          <p><Markdown>{description}</Markdown></p>
          <p className={styles.codeLine}><CodeRegular /> Key: <code>{securityRule}</code></p>
        </>
      }
      relationship="description"
    >
      {badge}
    </Tooltip>
  );
}
