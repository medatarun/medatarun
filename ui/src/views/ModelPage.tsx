import {useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import {Link} from "@tanstack/react-router";
import {RelationshipDescription} from "../components/business/RelationshipDescription.tsx";
import {type ElementOrigin, Model, type ModelDto} from "../business/model.tsx";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import type {TabValue} from "@fluentui/react-components";
import {Divider, Tab, Table, TableBody, TableCell, TableRow, TabList} from "@fluentui/react-components";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";

export function ModelPage({modelId}: { modelId: string }) {
  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("/ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId])
  return <div>
    {model && <ModelContext value={new Model(model)}><ModelView/></ModelContext>}
  </div>
}


export function ModelView() {
  const model = useModelContext().dto
  const [selectedTab, setSelectedTab] = useState<TabValue>("entities")
  return <div>
    <ViewTitle>Model {model.name ?? model.id}</ViewTitle>
    <div style={{display: "grid", gridTemplateColumns: "min-content auto", columnGap: "1em"}}>
      <div>Identifier</div>
      <div><code>{model.id}</code></div>
      <div>Version</div>
      <div><code>{model.version}</code></div>
      <div>Documentation</div>
      <div><ExternalUrl url={model.documentationHome}/></div>
      <div>Hashtags</div>
      <div><Tags tags={model.hashtags}/></div>
      <div>Origin</div>
      <div><Origin value={model.origin}/></div>
    </div>
    {model.description && <div><Markdown value={model.description}/></div>}
    <TabList selectedValue={selectedTab} onTabSelect={(_, data) => setSelectedTab(data.value)}>
      <Tab icon={<EntityIcon />} value="entities">Entities</Tab>
      <Tab icon={<RelationshipIcon />} value="relationships">Relationships</Tab>
      <Tab icon={<TypeIcon/>} value="types">Types</Tab>
    </TabList>
    <Divider/>
    {selectedTab === "entities" && (
      <div style={{paddingTop:"1em"}}>
        <Table size="small" style={{marginBottom: "1em"}}>
          <TableBody>
            {
              model.entityDefs.map(entityDef => <TableRow key={entityDef.id}>
                <TableCell style={{width: "20em", wordBreak: "break-all"}}><Link
                  to="/model/$modelId/entityDef/$entityDefId"
                  params={{
                    modelId: model.id,
                    entityDefId: entityDef.id
                  }}>{entityDef.name ?? entityDef.id}</Link></TableCell>
                <TableCell>{entityDef.description}</TableCell>
              </TableRow>)
            }
          </TableBody>
        </Table>
      </div>
    )}
    {selectedTab === "relationships" && (
      <div>
        <Table size="small" style={{marginBottom: "1em"}}>
          <TableBody>
            {model.relationshipDefs.map(r => <TableRow key={r.id}>
              <TableCell style={{width: "20em", wordBreak: "break-all"}}>{r.name ?? r.id}</TableCell>
              <TableCell><RelationshipDescription rel={r}/></TableCell>
            </TableRow>)}
          </TableBody>
        </Table>
      </div>
    )}
    {selectedTab === "types" && (<div>
      <Table size="small" style={{marginBottom: "1em"}}>
        <TableBody>
          {
            model.types.map(t => <TableRow key={t.id}>
              <TableCell style={{width: "20em"}}>{t.name ?? t.id}</TableCell>
              <TableCell><code>{t.id}</code></TableCell>
              <TableCell>{t.description}</TableCell>
            </TableRow>)
          }
        </TableBody>
      </Table>
    </div>)}

  </div>
}

export function Origin({value}: { value: ElementOrigin }) {
  if (value.type == "manual") return "Medatarun (manual)"
  return <ExternalUrl url={value.uri}/>
}

export function ExternalUrl({url}: { url: string | null }) {
  if (!url) return null
  return <a href={url} target="_blank">{url}</a>;
}

export function Markdown({value}: { value: string | null }) {
  if (value == null) return null
  return <ReactMarkdown>{value}</ReactMarkdown>
}
