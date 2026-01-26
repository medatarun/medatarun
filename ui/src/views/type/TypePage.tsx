import {Link, useNavigate} from "@tanstack/react-router";
import {ActionUILocations, Model, type TypeDto, useActionRegistry, useModel} from "../../business";
import {ModelContext} from "../../components/business/ModelContext.tsx";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens
} from "@fluentui/react-components";
import {ModelIcon} from "../../components/business/Icons.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {Markdown} from "../../components/core/Markdown.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {
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
  const actions = actionRegistry.findActions(ActionUILocations.type)

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const actionParams = createActionTemplateType(model.id, type.id)


  return <ViewLayoutContained title={<div>
    <div style={{marginLeft: "-22px"}}>
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon/>}
          onClick={handleClickModel}>{model.nameOrKey}</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
    </Breadcrumb>
    </div>
    <div>
      <ViewTitle eyebrow={"Data type"}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div>
        {type.name ?? type.id} {" "}
          </div>
          <div>

        <ActionMenuButton
          label="Actions"
          itemActions={actions}
          actionParams={actionParams}/>
          </div>
        </div>
      </ViewTitle>
    </div>
  </div>
  }>
    <ContainedMixedScrolling>
      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <TypeOverview model={model} type={type}/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL">
            {type.description ? <Markdown value={type.description}/> :
              <MissingInformation>add description</MissingInformation>}
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
    {isDetailLevelTech && <div><Text>Type&nbsp;key</Text></div>}
    {isDetailLevelTech && <div><Text><code>{type.key}</code></Text></div>}
    <div><Text>From&nbsp;model</Text></div>
    <div>
      <Link
        to="/model/$modelId"
        params={{modelId: model.id}}>{model.nameOrKey}</Link>
    </div>
    {isDetailLevelTech && <div><Text>Type&nbsp;Id</Text></div>}
    {isDetailLevelTech && <div><Text><code>{type.id}</code></Text></div>}

  </PropertiesForm>
}
