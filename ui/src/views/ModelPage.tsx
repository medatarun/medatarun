import {useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import {useNavigate} from "@tanstack/react-router";
import {type ElementOrigin, Model, type ModelDto} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Divider,
  Tab,
  TabList,
  type TabValue
} from "@fluentui/react-components";
import {EntityIcon, ModelIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";
import {EntityCard} from "../components/business/EntityCard.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {TypesTable} from "../components/business/TypesTable.tsx";
import {TabPanel} from "../components/core/TabPanel.tsx";
import {InfoRegular} from "@fluentui/react-icons";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ActionsBar} from "../components/business/ActionsBar.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";

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

  const displayName = model.name ?? model.id
  const navigate = useNavigate();
  const handleClickModels = () => { navigate({to:"/"})}

  return <ViewLayoutContained title={
    <Breadcrumb>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModels}>Models</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>} current>{displayName}</BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
  }>
    <ViewTitle eyebrow={<span><ModelIcon /> Model</span>}>{displayName}</ViewTitle>
    <ActionsBar location="model" params={{
      modelKey: model.id,
    }} />
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

  </ViewLayoutContained>
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
