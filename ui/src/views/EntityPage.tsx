import {useEffect, useState} from "react";
import {Link} from "@tanstack/react-router";
import {ExternalUrl, Markdown, Origin} from "./ModelPage.tsx";
import {RelationshipDescription} from "../components/business/RelationshipDescription.tsx";
import {type EntityDto, Model, type ModelDto} from "../business/model.tsx";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text
} from "@fluentui/react-components";
import {ViewSubtitle} from "../components/core/ViewSubtitle.tsx";
import {AttributeIcon, RelationshipIcon} from "../components/business/Icons.tsx";


export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {

  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("/ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId, entityDefId])
  const entity = model?.entityDefs?.find(it => it.id === entityDefId)
  return <div>
    {model && entity && <ModelContext value={new Model(model)}><EntityView entity={entity}/></ModelContext>}
  </div>
}

export function EntityView({entity}: { entity: EntityDto }) {
  const model = useModelContext().dto

  const relationshipsInvolved = model.relationshipDefs
    .filter(it => it.roles.some(r => r.entityId === entity.id));

  return <div>
    <ViewTitle>Entity {entity.name ?? entity.id}</ViewTitle>
    <Breadcrumb>
      <BreadcrumbItem><BreadcrumbButton><Link to="/">Models</Link></BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton><Link to="/model/$modelId"
                                              params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link></BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
    <div style={{display: "grid", gridTemplateColumns: "min-content auto", columnGap: "1em", marginBottom: "1em"}}>
      <div>Identifier</div>
      <div>{entity.id}</div>
      <div>Model</div>
      <div>
        <Link to="/model/$modelId" params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link>
      </div>
      <div>Documentation</div>
      <div><ExternalUrl url={entity.documentationHome}/></div>
      <div>Hashtags</div>
      <div><Tags tags={entity.hashtags}/></div>
      <div>Origin</div>
      <div><Origin value={entity.origin}/></div>
    </div>
    <div>
      <Markdown value={entity.description}/>
    </div>
    <ViewSubtitle icon={AttributeIcon}>Attributes</ViewSubtitle>
    <Table size="small">
      <TableBody>{entity.attributes.map(a => <TableRow key={a.id}>
        <TableCell style={{width: "10em"}}>{a.name ?? a.id}</TableCell>
        <TableCell>
          <div>
            <Markdown value={a.description}/>
          </div>
          <div>
            <code>{a.id}</code>
            {" "}
            <code>{a.type} {a.optional ? "?" : ""}</code>
            {a.identifierAttribute ? "🔑" : ""}
          </div>
        </TableCell>
      </TableRow>)}</TableBody>
    </Table>
    <p></p>
    <ViewSubtitle icon={RelationshipIcon}>Relationships involved</ViewSubtitle>
    <div>
      {relationshipsInvolved.length == 0 ? <Text italic>No relationships involved</Text> : null}
    </div>
    <div>
      <Table size="small">
        <TableBody>{relationshipsInvolved
          .map(r => <TableRow key={r.id}>
            <TableCell style={{width: "10em", wordBreak:"break-all"}}>{r.name ?? r.id}</TableCell>
            <TableCell style={{width: "auto"}}><RelationshipDescription rel={r}/></TableCell>
          </TableRow>)}</TableBody>
      </Table>
    </div>
  </div>
}