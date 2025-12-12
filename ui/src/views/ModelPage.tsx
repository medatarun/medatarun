import {useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import {useNavigate} from "@tanstack/react-router";
import {type ElementOrigin, Model, type ModelDto} from "../business/model.tsx";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import type {TabValue} from "@fluentui/react-components";
import {Divider, Tab, TabList} from "@fluentui/react-components";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";
import {EntityCard} from "../components/business/EntityCard.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {TypesTable} from "../components/business/TypesTable.tsx";
import {TabPanel} from "../components/core/TabPanel.tsx";
import {InfoRegular} from "@fluentui/react-icons";

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
  const [selectedTab, setSelectedTab] = useState<TabValue>("info")

  return <div>
    <ViewTitle><div style={{ whiteSpace: "nowrap", overflow: "hidden", textOverflow:"ellipsis"}}>Model {model.name ?? model.id}</div></ViewTitle>
    <TabList selectedValue={selectedTab} onTabSelect={(_, data) => setSelectedTab(data.value)}>
      <Tab icon={<InfoRegular/>} value="info">Overview</Tab>
      <Tab icon={<EntityIcon/>} value="entities">Entities</Tab>
      <Tab icon={<RelationshipIcon/>} value="relationships">Relationships</Tab>
      <Tab icon={<TypeIcon/>} value="types">Types</Tab>
    </TabList>
    <Divider/>
    {selectedTab === "info" && (<TabPanel><ModelOverview/></TabPanel>)}
    {selectedTab === "entities" && (
      <TabPanel>
        <EntitiesCardList/>
      </TabPanel>
    )}
    {selectedTab === "relationships" && (
      <div>
        <RelationshipsTable relationships={model.relationshipDefs}/>
      </div>
    )}
    {selectedTab === "types" && (<div>
      <TypesTable types={model.types}/>
    </div>)}

  </div>
}

export function ModelOverview() {
  const model = useModelContext().dto
  return <div>
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
  </div>
}

export function EntitiesCardList() {
  const navigate = useNavigate()
  const model = useModelContext()
  const modelKey = model.dto.id
  const entities = model.dto.entityDefs
  return <div style={{display: "flex", columnGap: "1em", rowGap: "1em", flexWrap: "wrap"}}>
    {
      entities.map(entityDef => <EntityCard
        key={entityDef.id}
        entity={entityDef}
        onClick={() => navigate({
            to: "/model/$modelId/entityDef/$entityDefId",
            params: {
              modelId: modelKey,
              entityDefId: entityDef.id
            }
          }
        )}/>
      )
    }
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
