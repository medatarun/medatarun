import {Link, useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  Model,
  type TypeDto,
  useActionRegistry,
  useModel,
  useTypeUpdateDescription,
  useTypeUpdateKey,
  useTypeUpdateName
} from "../../business";
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
import {InlineEditDescription} from "../../components/core/InlineEditDescription.tsx";
import {useMemo} from "react";
import {InlineEditSingleLine} from "../../components/core/InlineEditSingleLine.tsx";


export function TypePage({modelId, typeId}: {
  modelId: string,
  typeId: string
}) {

  const {data: modelDto} = useModel(modelId)
  const model = useMemo(() => modelDto ? new Model(modelDto) : null, [modelDto])

  if (!modelDto) return null
  if (!model) return null


  const type = model.findTypeDto(typeId)
  if (!type) return <ErrorBox error={toProblem("Type not found")}/>

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
  const typeUpdateName = useTypeUpdateName()
  const typeUpdateDescription = useTypeUpdateDescription()

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const handleChangeName = (value: string) => {
    return typeUpdateName.mutateAsync({modelId: model.id, typeId: type.id, value: value})
  }
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
          <div style={{width: "100%"}}>
            <InlineEditSingleLine
              value={type.name ?? ""}
              onChange={handleChangeName}>
              {type.name ? model.findTypeNameOrKey(type.id) :
                <span style={{color:tokens.colorNeutralForeground4, fontStyle: "italic"}}>{model.findTypeNameOrKey(type.id)}</span>} {" "}
            </InlineEditSingleLine>
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
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={type.description}
              placeholder={"add description"}
              onChange={v => typeUpdateDescription.mutateAsync({modelId: model.id, typeId: type.id, value: v})}
            />
          </SectionPaper>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

export function TypeOverview({type, model}: {
  type: TypeDto,
  model: Model
}) {
  const typeUpdateKey = useTypeUpdateKey()
  const {isDetailLevelTech} = useDetailLevelContext()


  const handleChangeKey = (value: string) => {
    return typeUpdateKey.mutateAsync({modelId: model.id, typeId: type.id, value: value})
  }

  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Type&nbsp;key</Text></div>}
    {isDetailLevelTech && <InlineEditSingleLine value={type.key} onChange={handleChangeKey}>
      <Text><code>{type.key}</code></Text>
    </InlineEditSingleLine>}
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
