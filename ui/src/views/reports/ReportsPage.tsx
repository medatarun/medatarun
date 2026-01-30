import {ViewTitle} from "../../components/core/ViewTitle.tsx";
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
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ArrowDownloadRegular, DocumentBulletListRegular, SearchFilled} from "@fluentui/react-icons";
import {ContainedFixed, ContainedMixedScrolling, ContainedScrollable} from "../../components/layout/Contained.tsx";
import {useQuery} from "@tanstack/react-query";
import {executeAction} from "../../business";
import {useState} from "react";
import {toProblem} from "@seij/common-types";
import {useNavigate} from "@tanstack/react-router";
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon} from "../../components/business/Icons.tsx";
import {downloadCsv} from "@seij/common-ui-csv-export";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {sortBy} from "lodash-es";


interface SearchResult {
  id: string
  locationType: string
  modelId: string
  modelLabel: string
  entityId: string | null
  entityLabel: string | null
  entityAttributeId: string | null
  entityAttributeLabel: string | null
  relationshipId: string | null
  relationshipLabel: string | null
  relationshipAttributeId: string | null
  relationshipAttributeLabel: string | null
  tags: string[]
}

interface SearchResults {
  results: SearchResult[]
}

async function tagSearch(tags: string) {
  if (tags == "") return {results:[]}
  const result = await executeAction<SearchResults>("model", "tag_search", {
    tags: tags
  })
  if (result.contentType === "json") {
    return result.json
  } else throw toProblem("Invalid response content type")
}


const useTagSearch = (tags: string) => {
  return useQuery({
    queryKey: ["tag_search", tags],
    queryFn: () => tagSearch(tags)
  })
}

function createCsv(items: SearchResult[]) {
  downloadCsv<SearchResult>("tag-report.csv", [
      {
        code: "model",
        label: "Model",
        render: (it) => it.modelLabel
      },
      {
        code: "type",
        label: "Type",
        render: (it) => {
          if (it.entityId == null && it.entityId == null) return "Model"
          if (it.entityId != null && it.entityAttributeId == null) return "Entity"
          if (it.entityId != null && it.entityAttributeId != null) return "Entity attribute"
          if (it.relationshipId != null && it.relationshipAttributeId == null) return "Relationship"
          if (it.relationshipId != null && it.relationshipAttributeId != null) return "Relationship attribute"
          return "unknown"
        }
      },
      {
        code: "entity",
        label: "Entity/Relationship",
        render: (it) => {
          if (it.entityLabel != null) return it.entityLabel
          if (it.relationshipLabel != null) return it.relationshipLabel
          return ""
        }
      },
      {
        code: "attribute",
        label: "Attribute",
        render: (it) => {
          if (it.entityAttributeLabel != null) return it.entityAttributeLabel
          if (it.relationshipAttributeLabel != null) return it.relationshipAttributeLabel
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
  const data = useTagSearch(tags)
  const results = data?.data?.results ?? []
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
        {results.length > 0 &&
          <div style={{padding: tokens.spacingVerticalM}}>
            <Button icon={<ArrowDownloadRegular/>} onClick={() => createCsv(results)}>Download CSV</Button>
          </div>
        }
      </ContainedFixed>
      <ContainedScrollable>
        {results.length == 0 &&
          <div style={{padding: tokens.spacingVerticalM}}>
            <MissingInformation>No results.</MissingInformation>
          </div>}
        <Table>
          <TableBody>
            {results.map(it => {
              return <TableRow key={it.id}>
                <TableCell>
                  <Path key={it.id} searchResult={it}/>
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

function Path({searchResult}: { searchResult: SearchResult }) {
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
  } = searchResult
  return <Breadcrumb>

    <BreadcrumbItem>
      <BreadcrumbButton
        icon={<ModelIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId",
          params: {modelId: modelId}
        })}>{modelLabel}</BreadcrumbButton>
    </BreadcrumbItem>

    {entityId !== null && entityLabel !== null && <BreadcrumbDivider/>}
    {entityId !== null && entityLabel !== null && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<EntityIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/entity/$entityId",
          params: {modelId: modelId, entityId: entityId}
        })}>{entityLabel}</BreadcrumbButton>
    </BreadcrumbItem>}

    {entityId !== null && entityAttributeId !== null && entityAttributeLabel !== null && <BreadcrumbDivider/>}
    {entityId !== null && entityAttributeId !== null && entityAttributeLabel !== null && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<AttributeIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/entity/$entityId/attribute/$attributeId",
          params: {modelId: modelId, entityId: entityId, attributeId: entityAttributeId}
        })}>{entityAttributeLabel}</BreadcrumbButton>
    </BreadcrumbItem>}


    {relationshipId !== null && relationshipLabel !== null && <BreadcrumbDivider/>}
    {relationshipId !== null && relationshipLabel !== null && <BreadcrumbItem>
      <BreadcrumbButton
        icon={<RelationshipIcon/>}
        onClick={() => navigate({
          to: "/model/$modelId/relationship/$relationshipId",
          params: {modelId: modelId, relationshipId: relationshipId}
        })}>{relationshipLabel}</BreadcrumbButton>
    </BreadcrumbItem>}

    {relationshipId !== null && relationshipAttributeId !== null && relationshipAttributeLabel !== null &&
      <BreadcrumbDivider/>}
    {relationshipId !== null && relationshipAttributeId !== null && relationshipAttributeLabel !== null &&
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