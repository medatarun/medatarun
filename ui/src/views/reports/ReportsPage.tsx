import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {
  Avatar,
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Button,
  Dropdown,
  type DropdownProps,
  Field,
  Input,
  Option,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Tag,
  TagGroup,
  TagPicker,
  TagPickerControl,
  TagPickerGroup,
  TagPickerInput,
  TagPickerList,
  TagPickerOption,
  type TagPickerProps,
  tokens,
} from "@fluentui/react-components";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {
  AddRegular,
  ArrowDownloadRegular,
  DeleteRegular,
  DocumentBulletListRegular,
  SearchFilled,
} from "@fluentui/react-icons";
import {ContainedFixed, ContainedMixedScrolling, ContainedScrollable,} from "@/components/layout/Contained.tsx";
import {
  type ModelSearchFilter,
  type ModelSearchModelItemField,
  type ModelSearchModelItemFieldCondition,
  type ModelSearchReq,
  type ModelSearchTagFilter,
  type ModelSearchTagFilterCondition,
  type SearchResultLocation,
  useModelSearch,
} from "@/business/model";
import {useState} from "react";
import {useNavigate} from "@tanstack/react-router";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";
import {sortBy} from "lodash-es";
import {AttributeIcon, EntityIcon, ModelIcon, RelationshipIcon,} from "@/components/business/model/model.icons.tsx";
import {type Tags, useTags} from "@/business/tag";
import {createCsv} from "./ReportsPage.csvexport.tsx";

const LOCAL_STORAGE_KEY = "reports-query-builder";
const ENABLE_MODEL_ITEM_FIELD_FILTER = false;

type FilterRowType = "tags" | "modelItemField";


function createFilterId() {
  return Math.random().toString(36).slice(2, 10);
}

function createDefaultTagFilter(): ModelSearchTagFilter {
  return {
    id: createFilterId(),
    type: "tags",
    condition: "anyOf",
    tagIds: [],
  };
}

function createDefaultModelItemFieldFilter(): ModelSearchFilter {
  return {
    id: createFilterId(),
    type: "modelItemField",
    field: "name",
    condition: "contains",
    value: "",
  };
}

function createDefaultQuery(): ModelSearchReq {
  return {
    operator: "and",
    items: [createDefaultTagFilter()],
  };
}

function loadStoredQuery(): ModelSearchReq {
  const rawValue = localStorage.getItem(LOCAL_STORAGE_KEY);
  if (!rawValue) {
    return createDefaultQuery();
  }
  try {
    return JSON.parse(rawValue) as ModelSearchReq;
  } catch {
    return createDefaultQuery();
  }
}

function availableFilterTypes(): FilterRowType[] {
  if (ENABLE_MODEL_ITEM_FIELD_FILTER) {
    return ["tags", "modelItemField"];
  }
  return ["tags"];
}

function changeFilterType(type: FilterRowType): ModelSearchFilter {
  if (type === "modelItemField") {
    return createDefaultModelItemFieldFilter();
  }
  return createDefaultTagFilter();
}

function isTagConditionUsingTags(condition: ModelSearchTagFilterCondition) {
  return condition === "anyOf" || condition === "allOf" || condition === "noneOf";
}

export function ReportsPage() {
  const [draftQuery, setDraftQuery] = useState<ModelSearchReq>(loadStoredQuery);
  const [appliedQuery, setAppliedQuery] = useState<ModelSearchReq>(
    loadStoredQuery,
  );

  const query = useModelSearch(appliedQuery);
  const items = query.data?.items ?? [];
  const hasFilters = appliedQuery.items.length > 0;

  const handleChangeOperator: DropdownProps["onOptionSelect"] = (_, data) => {
    const operator = data.optionValue;
    if (operator !== "and" && operator !== "or") {
      return;
    }
    setDraftQuery((previous) => ({
      ...previous,
      operator: operator,
    }));
  };

  const handleClickSearch = () => {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(draftQuery));
    setAppliedQuery(draftQuery);
  };

  const handleAddFilter = () => {
    const filterTypes = availableFilterTypes();
    const nextType = filterTypes[0];
    setDraftQuery((previous) => ({
      ...previous,
      items: [...previous.items, changeFilterType(nextType)],
    }));
  };

  const handleDeleteFilter = (filterId: string) => {
    setDraftQuery((previous) => ({
      ...previous,
      items: previous.items.filter((it) => it.id !== filterId),
    }));
  };

  const handleChangeFilterType = (filterId: string, type: FilterRowType) => {
    setDraftQuery((previous) => ({
      ...previous,
      items: previous.items.map((it) =>
        it.id === filterId ? changeFilterType(type) : it,
      ),
    }));
  };

  const handleUpdateFilter = (filter: ModelSearchFilter) => {
    setDraftQuery((previous) => ({
      ...previous,
      items: previous.items.map((it) => (it.id === filter.id ? filter : it)),
    }));
  };

  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div style={{ width: "100%" }}>
                <DocumentBulletListRegular /> Report: tagged items
              </div>
            </div>
          </ViewTitle>
        </div>
      }
    >
      <ContainedMixedScrolling>
        <ContainedFixed>
          <div
            style={{
              display: "grid",
              rowGap: tokens.spacingVerticalM,
              padding: tokens.spacingHorizontalM,
            }}
          >
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "180px 1fr auto",
                gap: tokens.spacingHorizontalM,
                alignItems: "end",
              }}
            >
              <Field label="Combine filters with">
                <Dropdown
                  value={draftQuery.operator.toUpperCase()}
                  selectedOptions={[draftQuery.operator]}
                  onOptionSelect={handleChangeOperator}
                >
                  <Option value="and">AND</Option>
                  <Option value="or">OR</Option>
                </Dropdown>
              </Field>
              <div />
              <Button
                appearance="primary"
                icon={<SearchFilled />}
                onClick={handleClickSearch}
              >
                Search
              </Button>
            </div>

            <div style={{ display: "grid", rowGap: tokens.spacingVerticalM }}>
              {draftQuery.items.map((filter) => (
                <FilterRowEditor
                  key={filter.id}
                  filter={filter}
                  onDelete={handleDeleteFilter}
                  onTypeChange={handleChangeFilterType}
                  onChange={handleUpdateFilter}
                />
              ))}
            </div>

            <div>
              <Button icon={<AddRegular />} onClick={handleAddFilter}>
                Add filter
              </Button>
            </div>

            {items.length > 0 && (
              <div>
                <Button
                  icon={<ArrowDownloadRegular />}
                  onClick={() => createCsv(items)}
                >
                  Download CSV
                </Button>
              </div>
            )}
          </div>
        </ContainedFixed>
        <ContainedScrollable>
          {!hasFilters && (
            <div style={{ padding: tokens.spacingVerticalM }}>
              <MissingInformation>Add at least one filter to search.</MissingInformation>
            </div>
          )}
          {hasFilters && items.length === 0 && (
            <div style={{ padding: tokens.spacingVerticalM }}>
              <MissingInformation>No results.</MissingInformation>
            </div>
          )}
          {items.length > 0 && (
            <Table>
              <TableBody>
                {items.map((it) => {
                  return (
                    <TableRow key={it.id}>
                      <TableCell>
                        <Path key={it.id} location={it.location} />
                      </TableCell>
                      <TableCell>
                        <TagGroup>
                          {sortBy(it.tags ?? []).map((tag, index) => (
                            <Tag key={index} appearance="outline" size="small">
                              {tag}
                            </Tag>
                          ))}
                        </TagGroup>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

function FilterRowEditor({
  filter,
  onDelete,
  onTypeChange,
  onChange,
}: {
  filter: ModelSearchFilter;
  onDelete: (filterId: string) => void;
  onTypeChange: (filterId: string, type: FilterRowType) => void;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const handleChangeType: DropdownProps["onOptionSelect"] = (_, data) => {
    const type = data.optionValue;
    if (type !== "tags" && type !== "modelItemField") {
      return;
    }
    onTypeChange(filter.id, type);
  };

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "160px 1fr auto",
        gap: tokens.spacingHorizontalM,
        alignItems: "start",
        padding: tokens.spacingHorizontalM,
        border: `1px solid ${tokens.colorNeutralStroke2}`,
        borderRadius: tokens.borderRadiusMedium,
      }}
    >
      <Field label="Filter type">
        <Dropdown
          value={filter.type === "tags" ? "Tag" : "Model item field"}
          selectedOptions={[filter.type]}
          onOptionSelect={handleChangeType}
        >
          <Option value="tags">Tag</Option>
          {ENABLE_MODEL_ITEM_FIELD_FILTER && (
            <Option value="modelItemField">Model item field</Option>
          )}
        </Dropdown>
      </Field>

      {filter.type === "tags" ? (
        <TagFilterRowEditor filter={filter} onChange={onChange} />
      ) : (
        <ModelItemFieldFilterRowEditor filter={filter} onChange={onChange} />
      )}

      <div style={{ paddingTop: "28px" }}>
        <Button
          appearance="subtle"
          icon={<DeleteRegular />}
          onClick={() => onDelete(filter.id)}
        >
          Remove
        </Button>
      </div>
    </div>
  );
}

function TagFilterRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTagFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const { tags, isPending } = useTags();

  const handleChangeCondition: DropdownProps["onOptionSelect"] = (_, data) => {
    const condition = data.optionValue;
    if (
      condition !== "anyOf" &&
      condition !== "allOf" &&
      condition !== "noneOf" &&
      condition !== "empty" &&
      condition !== "notEmpty"
    ) {
      return;
    }
    onChange({
      ...filter,
      condition: condition,
    });
  };

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: isTagConditionUsingTags(filter.condition)
          ? "220px 1fr"
          : "220px",
        gap: tokens.spacingHorizontalM,
        alignItems: "start",
      }}
    >
      <Field label="Tag filter">
        <Dropdown
          value={tagConditionLabel(filter.condition)}
          selectedOptions={[filter.condition]}
          onOptionSelect={handleChangeCondition}
        >
          <Option value="anyOf">Any of</Option>
          <Option value="allOf">All of</Option>
          <Option value="noneOf">None of</Option>
          <Option value="empty">Empty</Option>
          <Option value="notEmpty">Not empty</Option>
        </Dropdown>
      </Field>

      {isTagConditionUsingTags(filter.condition) && (
        <Field label="Tags">
          <TagFilterTagPicker
            disabled={isPending}
            selectedTagIds={filter.tagIds}
            tags={tags}
            onChange={(tagIds) =>
              onChange({
                ...filter,
                tagIds: tagIds,
              })
            }
          />
        </Field>
      )}
    </div>
  );
}

function ModelItemFieldFilterRowEditor({
  filter,
  onChange,
}: {
  filter: Extract<ModelSearchFilter, { type: "modelItemField" }>;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const handleChangeField: DropdownProps["onOptionSelect"] = (_, data) => {
    const field = data.optionValue as ModelSearchModelItemField | undefined;
    if (field == null) {
      return;
    }
    onChange({
      ...filter,
      field: field,
    });
  };

  const handleChangeCondition: DropdownProps["onOptionSelect"] = (_, data) => {
    const condition =
      data.optionValue as ModelSearchModelItemFieldCondition | undefined;
    if (condition == null) {
      return;
    }
    onChange({
      ...filter,
      condition: condition,
    });
  };

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "180px 180px 1fr",
        gap: tokens.spacingHorizontalM,
        alignItems: "start",
      }}
    >
      <Field label="Model item field">
        <Dropdown
          value={modelItemFieldLabel(filter.field)}
          selectedOptions={[filter.field]}
          onOptionSelect={handleChangeField}
        >
          <Option value="name">Name</Option>
          <Option value="key">Key</Option>
          <Option value="description">Description</Option>
        </Dropdown>
      </Field>
      <Field label="Condition">
        <Dropdown
          value={modelItemFieldConditionLabel(filter.condition)}
          selectedOptions={[filter.condition]}
          onOptionSelect={handleChangeCondition}
        >
          <Option value="contains">Contains</Option>
          <Option value="is">Is</Option>
        </Dropdown>
      </Field>
      <Field label="Value">
        <Input
          value={filter.value}
          onChange={(_, data) =>
            onChange({
              ...filter,
              value: data.value,
            })
          }
        />
      </Field>
    </div>
  );
}

function TagFilterTagPicker({
  disabled,
  selectedTagIds,
  tags,
  onChange,
}: {
  disabled: boolean;
  selectedTagIds: string[];
  tags: Tags;
  onChange: (tagIds: string[]) => void;
}) {
  const [inputValue, setInputValue] = useState("");
  const [open, setOpen] = useState(false);
  const options = tags.search(inputValue, selectedTagIds);

  const handleOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
    onChange(data.selectedOptions);
    setInputValue("");
    setOpen(false);
  };

  return (
    <TagPicker
      open={open}
      selectedOptions={selectedTagIds}
      onOpenChange={(_, data) => setOpen(data.open)}
      onOptionSelect={handleOptionSelect}
    >
      <TagPickerControl>
        <TagPickerGroup aria-label="Selected tags">
          {selectedTagIds.map((tagId) => (
            <Tag
              key={tagId}
              shape="rounded"
              media={
                <Avatar
                  aria-hidden
                  name={tags.formatLabel(tagId)}
                  color="colorful"
                />
              }
              value={tagId}
            >
              {tags.formatLabel(tagId)}
            </Tag>
          ))}
        </TagPickerGroup>
        <TagPickerInput
          value={inputValue}
          aria-label="Search tags"
          disabled={disabled}
          onFocus={() => setOpen(true)}
          onChange={(event) => {
            setInputValue(event.currentTarget.value);
            setOpen(true);
          }}
        />
      </TagPickerControl>
      <TagPickerList>
        {options.length === 0 && (
          <div
            style={{
              padding: "8px 12px",
              color: "var(--colorNeutralForeground3)",
            }}
          >
            No matching tags
          </div>
        )}
        {options.map((option) => (
          <TagPickerOption
            key={option.id}
            value={option.id}
            text={tags.formatLabel(option.id)}
          >
            {tags.formatLabel(option.id)}
          </TagPickerOption>
        ))}
      </TagPickerList>
    </TagPicker>
  );
}

function tagConditionLabel(condition: ModelSearchTagFilterCondition) {
  if (condition === "anyOf") return "Any of";
  if (condition === "allOf") return "All of";
  if (condition === "noneOf") return "None of";
  if (condition === "empty") return "Empty";
  return "Not empty";
}

function modelItemFieldLabel(field: ModelSearchModelItemField) {
  if (field === "name") return "Name";
  if (field === "key") return "Key";
  return "Description";
}

function modelItemFieldConditionLabel(
  condition: ModelSearchModelItemFieldCondition,
) {
  if (condition === "contains") return "Contains";
  return "Is";
}

function Path({ location }: { location: SearchResultLocation }) {
  const navigate = useNavigate();
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
    relationshipAttributeLabel,
  } = location;
  return (
    <Breadcrumb>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon />}
          onClick={() =>
            navigate({
              to: "/model/$modelId",
              params: { modelId: modelId },
            })
          }
        >
          {modelLabel}
        </BreadcrumbButton>
      </BreadcrumbItem>

      {entityId && entityLabel && <BreadcrumbDivider />}
      {entityId && entityLabel && (
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<EntityIcon />}
            onClick={() =>
              navigate({
                to: "/model/$modelId/entity/$entityId",
                params: { modelId: modelId, entityId: entityId },
              })
            }
          >
            {entityLabel}
          </BreadcrumbButton>
        </BreadcrumbItem>
      )}

      {entityId && entityAttributeId && entityAttributeLabel && (
        <BreadcrumbDivider />
      )}
      {entityId && entityAttributeId && entityAttributeLabel && (
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<AttributeIcon />}
            onClick={() =>
              navigate({
                to: "/model/$modelId/entity/$entityId/attribute/$attributeId",
                params: {
                  modelId: modelId,
                  entityId: entityId,
                  attributeId: entityAttributeId,
                },
              })
            }
          >
            {entityAttributeLabel}
          </BreadcrumbButton>
        </BreadcrumbItem>
      )}

      {relationshipId && relationshipLabel && <BreadcrumbDivider />}
      {relationshipId && relationshipLabel && (
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<RelationshipIcon />}
            onClick={() =>
              navigate({
                to: "/model/$modelId/relationship/$relationshipId",
                params: { modelId: modelId, relationshipId: relationshipId },
              })
            }
          >
            {relationshipLabel}
          </BreadcrumbButton>
        </BreadcrumbItem>
      )}

      {relationshipId &&
        relationshipAttributeId &&
        relationshipAttributeLabel && <BreadcrumbDivider />}
      {relationshipId &&
        relationshipAttributeId &&
        relationshipAttributeLabel && (
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<AttributeIcon />}
              onClick={() =>
                navigate({
                  to: "/model/$modelId/relationship/$relationshipId/attribute/$attributeId",
                  params: {
                    modelId: modelId,
                    relationshipId: relationshipId,
                    attributeId: relationshipAttributeId,
                  },
                })
              }
            >
              {relationshipAttributeLabel}
            </BreadcrumbButton>
          </BreadcrumbItem>
        )}
    </Breadcrumb>
  );
}
