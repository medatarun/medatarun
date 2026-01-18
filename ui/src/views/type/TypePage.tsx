import {Link, useNavigate} from "@tanstack/react-router";
import {Model, type TypeDto, useActionRegistry, useModel} from "../../business";
import {ModelContext} from "../../components/business/ModelContext.tsx";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Divider,
  Text
} from "@fluentui/react-components";
import {AttributeIcon, ModelIcon, TypeIcon} from "../../components/business/Icons.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {Markdown} from "../../components/core/Markdown.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {
  ContainedFixed,
  ContainedHeader,
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {createActionTemplateType} from "../../components/business/actionTemplates.ts";
import {useDetailLevelContext} from "../../components/business/DetailLevelContext.tsx";
import {PropertiesForm} from "../../components/layout/PropertiesForm.tsx";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";


export function TypePage({modelId, typeId}: {
  modelId: string,
  typeId: string
}) {

  const {data: modelDto} = useModel(modelId)

  if (!modelDto) return null
  const model = new Model(modelDto)


  const type = model.findTypeDto(typeId)
  if (!type) return <ErrorBox error={toProblem("Type not found")} />

  return <ModelContext value={model}>
    <TypeView model={model} type={type}/>
  </ModelContext>
}

function TypeView({model, type}: {
  type: TypeDto,
  model: Model
}) {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions("type")

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const actionParams = createActionTemplateType(model.id, type.id)


  return <ViewLayoutContained title={
    <Breadcrumb>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon/>}><Link to="/models">Models</Link></BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon/>}
          onClick={handleClickModel}>{model.nameOrId}</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<AttributeIcon/>}
          current>{type.name ?? type.id}</BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  }>
    <ContainedMixedScrolling>
      <ContainedFixed>
        <ContainedHumanReadable>
          <ContainedHeader>
            <ViewTitle eyebrow={<span><TypeIcon/> Data type</span>}>
              {type.name ?? type.id} {" "}
              <ActionMenuButton
                itemActions={actions}
                actionParams={actionParams}/>
            </ViewTitle>
            <Divider/>
          </ContainedHeader>
        </ContainedHumanReadable>
      </ContainedFixed>

      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <TypeOverview model={model} type={type}/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL">
            {type.description ? <Markdown value={type.description}/> :
              <MissingInformation>No description provided.</MissingInformation>}
          </SectionPaper>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

export function TypeOverview({type, model}: {
  type:TypeDto,
  model: Model
}) {
  const {isDetailLevelTech} = useDetailLevelContext()
  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Type&nbsp;code</Text></div>}
    {isDetailLevelTech && <div><Text><code>{type.id}</code></Text></div>}
    <div><Text>From&nbsp;model</Text></div>
    <div>
      <Link
        to="/model/$modelId"
        params={{modelId: model.id}}>{model.nameOrId}</Link>
    </div>

  </PropertiesForm>
}
