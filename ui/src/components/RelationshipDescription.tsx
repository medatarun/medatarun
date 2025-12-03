import type {RelationshipDefSummaryDto, RelationshipRoleDefDto} from "../business/relationships.tsx";

export function RelationshipDescription(props:{rel: RelationshipDefSummaryDto}) {
  const {rel} = props
  if (rel.roles.length !== 2) {
    return <div>{rel.roles.length}-ary relationship.</div>
  }

  const [r1, r2] = rel.roles

  const render = (role: RelationshipRoleDefDto, other: RelationshipRoleDefDto) => {
    switch (role.cardinality) {
      case "one":
        return <div><EntityLink id={role.entityId} /> can be associated with exactly one <EntityLink id={other.entityId} />.</div>
      case "zeroOrOne":
        return <div><EntityLink id={role.entityId} /> can be associated with at most one <EntityLink id={other.entityId} />.</div>
      case "many":
        return <div><EntityLink id={role.entityId} /> can be associated with one or more <EntityLink id={other.entityId} />.</div>
      case "unknown":
        return <div><EntityLink id={role.entityId} /> can be associated with <EntityLink id={other.entityId} />, with no defined maximum.</div>
      default:
        return <div><EntityLink id={role.entityId} /> can be associated with <EntityLink id={other.entityId} />.</div>
    }
  }

  return <div>
    <div>{render(r1, r2)}</div>
    <div>{render(r2, r1)}</div>
  </div>
}

function EntityLink(props:{id: string}) {
  return <code>{props.id}</code>
}