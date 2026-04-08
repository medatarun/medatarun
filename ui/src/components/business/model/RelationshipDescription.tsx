import type { RelationshipDto, RelationshipRoleDto } from "@/business/model";
import { useModelContext } from "./ModelContext.tsx";
import { Link } from "@tanstack/react-router";
import { useAppI18n } from "@/services/appI18n.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";

export function RelationshipDescription(props: { rel: RelationshipDto }) {
  const { t } = useAppI18n();
  const { rel } = props;
  if (rel.roles.length !== 2) {
    return (
      <div>
        {t("relationshipDescription_nAry", { count: rel.roles.length })}
      </div>
    );
  }

  const [r1, r2] = rel.roles;

  const render = (role: RelationshipRoleDto, other: RelationshipRoleDto) => {
    switch (role.cardinality) {
      case "one":
        return (
          <div>
            <EntityLink id={role.entityId} />
            {t("relationshipDescription_exactlyOneBetween")}
            <EntityLink id={other.entityId} />.
          </div>
        );
      case "zeroOrOne":
        return (
          <div>
            <EntityLink id={role.entityId} />
            {t("relationshipDescription_atMostOneBetween")}
            <EntityLink id={other.entityId} />.
          </div>
        );
      case "many":
        return (
          <div>
            <EntityLink id={role.entityId} />
            {t("relationshipDescription_oneOrMoreBetween")}
            <EntityLink id={other.entityId} />.
          </div>
        );
      case "unknown":
        return (
          <div>
            <EntityLink id={role.entityId} />
            {t("relationshipDescription_withoutMaximumPrefix")}
            <EntityLink id={other.entityId} />
            {t("relationshipDescription_withoutMaximumSuffix")}
          </div>
        );
      default:
        return (
          <div>
            <EntityLink id={role.entityId} />
            {t("relationshipDescription_genericBetween")}
            <EntityLink id={other.entityId} />.
          </div>
        );
    }
  };

  if (rel.description)
    return (
      <div>
        <MarkdownSummary value={rel.description} maxChars={200} />
      </div>
    );

  return (
    <div>
      <div>{render(r1, r2)}</div>
      <div>{render(r2, r1)}</div>
    </div>
  );
}

function EntityLink(props: { id: string }) {
  const model = useModelContext();
  const name = model.findEntityNameOrKey(props.id);
  if (name !== null) {
    return (
      <Link
        to="/model/$modelId/entity/$entityId"
        params={{ modelId: model.dto.id, entityId: props.id }}
      >
        {name}
      </Link>
    );
  } else {
    return (
      <code>
        <Link
          to="/model/$modelId/entity/$entityId"
          params={{ modelId: model.dto.id, entityId: props.id }}
        >
          {props.id}
        </Link>
      </code>
    );
  }
}
