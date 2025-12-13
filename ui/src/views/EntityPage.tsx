import {useEffect, useState} from "react";
import {Link, useNavigate} from "@tanstack/react-router";
import {ExternalUrl, Markdown, Origin} from "./ModelPage.tsx";
import {type EntityDto, Model, type ModelDto} from "../business/model.tsx";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Caption1,
  Divider,
  Tab,
  TabList,
  type TabValue,
  Text
} from "@fluentui/react-components";
import {ViewSubtitle} from "../components/core/ViewSubtitle.tsx";
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon} from "../components/business/Icons.tsx";
import {AttributesTable} from "../components/business/AttributesTable.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {InfoRegular} from "@fluentui/react-icons";
import {TabPanel} from "../components/core/TabPanel.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";


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
  const model = useModelContext()
  const [selectedTab, setSelectedTab] = useState<TabValue>("info");
  const navigate = useNavigate()
  const relationshipsInvolved = model.dto.relationshipDefs
    .filter(it => it.roles.some(r => r.entityId === entity.id));

  const handleClickModel = () => { navigate({
    to: "/model/$modelId",
    params: {modelId: model.id}
  }) };

  return <ViewLayoutContained title={
    <Breadcrumb>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>}><Link to="/">Models</Link></BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>} onClick={handleClickModel}>{model.nameOrId}</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<EntityIcon/>} current>{entity.name ?? entity.id}</BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
  }>
    <ViewTitle>Entity {entity.name ?? entity.id}</ViewTitle>

    <TabList selectedValue={selectedTab} onTabSelect={(_, data) => setSelectedTab(data.value)}>
      <Tab value="info" icon={<InfoRegular/>}>Overview</Tab>
      <Tab value="attributes" icon={<AttributeIcon/>}>Attributes</Tab>
      <Tab value="relationships" icon={<RelationshipIcon/>}>Relationships</Tab>
    </TabList>
    <Divider/>

    {selectedTab === "info" && (
      <TabPanel>
        <div>
          <div style={{float: "right", display: "float", border: "1px solid #ccc", padding: "1em", width: "20em", overflow: "hidden"}}>
            <div>
              <div style={{marginBottom: "1em"}}>
                <div><Caption1>Key</Caption1></div>
                <div><Text><code>{entity.id}</code></Text></div>
              </div>
              <div style={{marginBottom: "1em"}}>
                <div><Caption1>Model</Caption1></div>
                <div><Link to="/model/$modelId"
                           params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link></div>
              </div>
              <div style={{marginBottom: "1em"}}>
                <div><Caption1>External documentation</Caption1></div>
                <div><ExternalUrl url={entity.documentationHome}/></div>
              </div>
              <div style={{marginBottom: "1em"}}>
                <div><Caption1>Tags</Caption1></div>
                <div><Tags tags={entity.hashtags}/></div>
              </div>
              <div style={{marginBottom: "1em"}}>
                <div><Caption1>Origin</Caption1></div>
                <div><Origin value={entity.origin}/></div>
              </div>
            </div>
          </div>
          <div>
            <Markdown value={entity.description}/>
            <div>
              <Caption1 truncate style={{overflow: "hidden", width: "30em", display: "block",}}
                        wrap={false}>{entity.attributes.map(it => (it.id ?? it.name)).join(", ")}</Caption1>
            </div>
          </div>

        </div>


      </TabPanel>
    )}
    {selectedTab == "attributes" && (
      <TabPanel>
        <ViewSubtitle icon={AttributeIcon}>Attributes</ViewSubtitle>
        <AttributesTable attributes={entity.attributes}/>
      </TabPanel>
    )}
    {selectedTab === "relationships" && (
      <TabPanel>
        <ViewSubtitle icon={RelationshipIcon}>Relationships involved</ViewSubtitle>
        <RelationshipsTable relationships={relationshipsInvolved}/>
      </TabPanel>
    )}


  </ViewLayoutContained>
}