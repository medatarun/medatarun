import {Link, useNavigate} from "@tanstack/react-router";
import {type EntityDto, Model, useActionRegistry, useModel} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Divider,
  tokens
} from "@fluentui/react-components";
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon} from "../components/business/Icons.tsx";
import {AttributesTable} from "../components/business/AttributesTable.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {EntityOverview} from "../components/business/EntityOverview.tsx";
import {SectionTitle} from "../components/layout/SectionTitle.tsx";
import {ActionMenuButton} from "../components/business/TypesTable.tsx";


export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {

  const {data: model} = useModel(modelId)

  const entity = model?.entityDefs?.find(it => it.id === entityDefId)
  if (!model) return null
  if (!entity) return null
  return <ModelContext value={new Model(model)}><EntityView entity={entity}/></ModelContext>
}

export function EntityView({entity}: { entity: EntityDto }) {
  const model = useModelContext()
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions("entity")
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
      <BreadcrumbItem>
        <BreadcrumbButton icon={<ModelIcon/>}><Link to="/models">Models</Link></BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem>
        <BreadcrumbButton icon={<ModelIcon/>}
                          onClick={handleClickModel}>{model.nameOrId}</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem>
        <BreadcrumbButton icon={<EntityIcon/>} current>{entity.name ?? entity.id}</BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
  }>
    <div style={{display: "flex", height: "100%", overflow: "hidden", flexDirection: "column"}}>
      <div>
        <ViewTitle eyebrow="Entity">
          {entity.name ?? entity.id} {" "}
          <ActionMenuButton
            itemActions={actions}
            actionParams={{
              modelKey: model.id,
              entityKey: entity.id
            }}/>
        </ViewTitle>
        <Divider/>
      </div>
      <div style={{flexGrow: 1, overflowY: "auto"}}>
        <EntityOverview entity={entity}/>

        <div style={{marginTop: tokens.spacingVerticalXXXL}}>
          <SectionTitle
            icon={<AttributeIcon/>}
            actionParams={{modelKey: model.id, entityKey: entity.id}}
            location="entity.attributes">Attributes</SectionTitle>
          <AttributesTable entityId={entity.id} attributes={entity.attributes}/>
        </div>

        <div style={{marginTop: tokens.spacingVerticalXXXL}}>
          <SectionTitle
            icon={<RelationshipIcon/>}
            actionParams={{modelKey: model.id, entityKey: entity.id}}
            location="entity.relationships">Relationships</SectionTitle>
          <RelationshipsTable relationships={relationshipsInvolved}/>
        </div>
      </div>
    </div>
  </ViewLayoutContained>
}