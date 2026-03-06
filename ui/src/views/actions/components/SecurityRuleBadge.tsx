import { useSecurityRuleDescriptionRegistry } from "@/business/config";
import { Badge, Tooltip } from "@fluentui/react-components";
import { ShieldRegular } from "@fluentui/react-icons";

export function SecurityRuleBadge(
  {
    securityRule,
  }: {
    securityRule: string;
  }) {
  const { registry: securityRuleDescriptionRegistry } =
    useSecurityRuleDescriptionRegistry();
  const description =
    securityRuleDescriptionRegistry.findDescription(securityRule);

  const badge = (
    <Badge
      appearance="tint"
      color="brand"
      icon={<ShieldRegular />}
    >
      {securityRule}
    </Badge>
  );

  if (!description) {
    return badge;
  }

  return (
    <Tooltip content={description} relationship="description">
      {badge}
    </Tooltip>
  );
}
