import {useEffect, useState} from "react";
import {Link, useNavigate} from "@tanstack/react-router";
import {type EntityDto, Model, type ModelDto} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
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
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon} from "../components/business/Icons.tsx";
import {AttributesTable} from "../components/business/AttributesTable.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {InfoRegular} from "@fluentui/react-icons";
import {TabPanel} from "../components/core/TabPanel.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ActionsBar} from "../components/business/ActionsBar.tsx";
import {EntityOverview} from "../components/business/EntityOverview.tsx";


export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {

  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("/ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId, entityDefId])
  const entity = model?.entityDefs?.find(it => it.id === entityDefId)
  if (!model) return null
  if (!entity) return null
  return <ModelContext value={new Model(model)}><EntityView entity={entity}/></ModelContext>
}

export function EntityView({entity}: { entity: EntityDto }) {
  const model = useModelContext()
  const [selectedTab, setSelectedTab] = useState<TabValue>("info");
  const navigate = useNavigate()
  const relationshipsInvolved = model.dto.relationshipDefs
    .filter(it => it.roles.some(r => r.entityId === entity.id));

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  return <ViewLayoutContained title={
    <Breadcrumb>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>}><Link
        to="/">Models</Link></BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>}
                                        onClick={handleClickModel}>{model.nameOrId}</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<EntityIcon/>}
                                        current>{entity.name ?? entity.id}</BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
  }>
    <div style={{display: "flex", height: "100%", overflow: "hidden", flexDirection: "column"}}>
      <div>
        <ViewTitle eyebrow="Entity">{entity.name ?? entity.id}</ViewTitle>
        <ActionsBar location="entity" params={{
          modelKey: model.id,
          entityKey: entity.id
        }}/>
        <TabList selectedValue={selectedTab} onTabSelect={(_, data) => setSelectedTab(data.value)}>
          <Tab value="info" icon={<InfoRegular/>}>Overview</Tab>
          <Tab value="attributes" icon={<AttributeIcon/>}>Attributes</Tab>
          <Tab value="relationships" icon={<RelationshipIcon/>}>Relationships</Tab>
        </TabList>
        <Divider/>
      </div>
      <div style={{flexGrow: 1, overflowY: "auto"}}>
        {selectedTab === "info" && (
          <TabPanel>
            <EntityOverview entity={entity}/>
          </TabPanel>
        )}
        {selectedTab == "attributes" && (
          <TabPanel>
            <AttributesTable attributes={entity.attributes}/>
          </TabPanel>
        )}
        {selectedTab === "relationships" && (
          <TabPanel>
            <RelationshipsTable relationships={relationshipsInvolved}/>
          </TabPanel>
        )}
      </div>
    </div>
  </ViewLayoutContained>
}