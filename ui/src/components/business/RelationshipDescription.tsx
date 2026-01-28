import type {RelationshipDto, RelationshipRoleDto} from "../../business";
import {useModelContext} from "./ModelContext.tsx";
import {Link} from "@tanstack/react-router";
import {Markdown} from "../core/Markdown.tsx";

export function RelationshipDescription(props: { rel: RelationshipDto }) {



  const {rel} = props
  if (rel.roles.length !== 2) {
    return <div>{rel.roles.length}-ary relationship.</div>
  }

  const [r1, r2] = rel.roles

  const render = (role: RelationshipRoleDto, other: RelationshipRoleDto) => {
    switch (role.cardinality) {
      case "one":
        return <div><EntityLink id={role.entityId}/> can be associated with exactly one <EntityLink
          id={other.entityId}/>.</div>
      case "zeroOrOne":
        return <div><EntityLink id={role.entityId}/> can be associated with at most one <EntityLink
          id={other.entityId}/>.</div>
      case "many":
        return <div><EntityLink id={role.entityId}/> can be associated with one or more <EntityLink
          id={other.entityId}/>.</div>
      case "unknown":
        return <div><EntityLink id={role.entityId}/> can be associated with <EntityLink id={other.entityId}/>, with no
          defined maximum.</div>
      default:
        return <div><EntityLink id={role.entityId}/> can be associated with <EntityLink id={other.entityId}/>.</div>
    }
  }

  if (rel.description) return <div><Markdown value={rel.description} /></div>

  return <div>
    <div>{render(r1, r2)}</div>
    <div>{render(r2, r1)}</div>
  </div>
}

function EntityLink(props: { id: string }) {
  const model = useModelContext()
  const name = model.findEntityNameOrKey(props.id)
  if (name !== null) {
    return <Link to="/model/$modelId/entity/$entityId" params={{modelId: model.dto.id, entityId: props.id}}>
      {name}
    </Link>
  } else {
    return <code><Link to="/model/$modelId/entity/$entityId" params={{modelId: model.dto.id, entityId: props.id}}>
      {props.id}
    </Link></code>
  }

}