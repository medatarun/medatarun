import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Button,
  Input,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Tag,
  TagGroup,
  tokens
} from "@fluentui/react-components";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {ArrowDownloadRegular, DocumentBulletListRegular, SearchFilled} from "@fluentui/react-icons";
import {ContainedFixed, ContainedMixedScrolling, ContainedScrollable} from "@/components/layout/Contained.tsx";
import {type SearchResult, type SearchResultLocation, useModelSearch} from "@/business";
import {useState} from "react";
import {useNavigate} from "@tanstack/react-router";
import {downloadCsv} from "@seij/common-ui-csv-export";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";
import {sortBy} from "lodash-es";
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon} from "@/components/business/model/model.icons.tsx";


function createCsv(items: SearchResult[]) {
  downloadCsv<SearchResult>("tag-report.csv", [
      {
        code: "model",
        label: "Model",
        render: (it) => it.location.modelLabel
      },
      {
        code: "type",
        label: "Type",
        render: (it) => {
          if (it.location.objectType === "model") return "Model"
          if (it.location.objectType === "entity") return "Entity"
          if (it.location.objectType === "entityAttribute") return "Entity attribute"
          if (it.location.objectType === "relationship") return "Relationship"
          if (it.location.objectType === "relationshipAttribute") return "Relationship attribute"
          return "unknown"
        }
      },
      {
        code: "entity",
        label: "Entity/Relationship",
        render: (it) => {
          if (it.location.entityLabel) return it.location.entityLabel
          if (it.location.relationshipLabel != null) return it.location.relationshipLabel
          return ""
        }
      },
      {
        code: "attribute",
        label: "Attribute",
        render: (it) => {
          if (it.location.entityAttributeLabel) return it.location.entityAttributeLabel
          if (it.location.relationshipAttributeLabel) return it.location.relationshipAttributeLabel
          return ""
        }
      }, {
        code: "tags",
        label: "Tags",
        render: (it) => it.tags.join(" ")
      }
    ],
    items
  )
}

export function ReportsPage() {

  const defaultTags = localStorage.getItem("reports-query") ?? ""

  const [tags, setTags] = useState<string>(defaultTags)
  const [inputTags, setInputTags] = useState<string>(defaultTags)
  const query = useModelSearch(tags)
  const items = query?.data?.items ?? []
  const handleClickSearch = () => {
    localStorage.setItem("reports-query", inputTags)
    setTags(inputTags)

  }
  return <ViewLayoutContained title={
    <div>
      <ViewTitle>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div style={{width: "100%"}}><DocumentBulletListRegular/> Report: tagged items</div>
        </div>
      </ViewTitle>
    </div>
  }>

    <ContainedMixedScrolling>
      <ContainedFixed>
        <div style={{
          display: "flex",
          paddingLeft: tokens.spacingHorizontalM,
          paddingRight: tokens.spacingHorizontalM,
          paddingTop: tokens.spacingVerticalM
        }}>
          <div style={{flex: 1}}>
            <Input style={{width: "100%"}} value={inputTags} onChange={(e, data) => setInputTags(data.value)}
                   placeholder="Search with tag names, comma separated"/>
          </div>
          <div style={{flex: 0}}>
            <Button appearance="primary" icon={<SearchFilled/>} onClick={handleClickSearch}>Search</Button>
          </div>
        </div>
        {items.length > 0 &&
          <div style={{padding: tokens.spacingVerticalM}}>
            <Button icon={<ArrowDownloadRegular/>} onClick={() => createCsv(items)}>Download CSV</Button>
          </div>
        }
      </ContainedFixed>
      <ContainedScrollable>
        {items.length == 0 &&
          <div style={{padding: tokens.spacingVerticalM}}>
            <MissingInformation>No results.</MissingInformation>
          </div>}
        <Table>
          <TableBody>
            {items.map(it => {
              return <TableRow key={it.id}>
                <TableCell>
                  <Path key={it.id} location={it.location}/>
                </TableCell>
                <TableCell>
                  <TagGroup>
                  {sortBy(it.tags).map((tag, index) => <Tag key={index} appearance="outline" size="small">{tag}</Tag>)}
                  </TagGroup>
                </TableCell>
              </TableRow>;
            })}
          </TableBody>
        </Table>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

function Path({location}: { location: SearchResultLocation }) {
  const navigate = useNavigate()
  const {
    modelId,
    modelLabel,
    entityId,
    entityLabel,
    entityAttributeId,
    entityAttributeLabel,
    relationshipId,
    relationshipLabel,
    relationshipAttributeId,
    relationshipAttributeLabel
  } = location
  return <Breadcrumb>

    <BreadcrumbItem>
      <BreadcrumbButton
        icon={<ModelIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId",
          params: {modelId: modelId}
        })}>{modelLabel}</BreadcrumbButton>
    </BreadcrumbItem>

    {entityId && entityLabel && <BreadcrumbDivider/>}
    {entityId && entityLabel && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<EntityIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/entity/$entityId",
          params: {modelId: modelId, entityId: entityId}
        })}>{entityLabel}</BreadcrumbButton>
    </BreadcrumbItem>}

    {entityId && entityAttributeId && entityAttributeLabel && <BreadcrumbDivider/>}
    {entityId && entityAttributeId && entityAttributeLabel && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<AttributeIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/entity/$entityId/attribute/$attributeId",
          params: {modelId: modelId, entityId: entityId, attributeId: entityAttributeId}
        })}>{entityAttributeLabel}</BreadcrumbButton>
    </BreadcrumbItem>}


    {relationshipId && relationshipLabel && <BreadcrumbDivider/>}
    {relationshipId && relationshipLabel && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<RelationshipIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/relationship/$relationshipId",
          params: {modelId: modelId, relationshipId: relationshipId}
        })}>{relationshipLabel}</BreadcrumbButton>
    </BreadcrumbItem>}

    {relationshipId && relationshipAttributeId && relationshipAttributeLabel &&
      <BreadcrumbDivider/>}
    {relationshipId && relationshipAttributeId && relationshipAttributeLabel &&
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<AttributeIcon/>}
          onClick={() => navigate({
            to: "/model/$modelId/relationship/$relationshipId/attribute/$attributeId",
            params: {modelId: modelId, relationshipId: relationshipId, attributeId: relationshipAttributeId}
          })}>{relationshipAttributeLabel}</BreadcrumbButton>
      </BreadcrumbItem>}

  </Breadcrumb>
}
