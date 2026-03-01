import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {Button, Dropdown, type DropdownProps, Option, tokens,} from "@fluentui/react-components";
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
  type ModelSearchReq,
  type ModelSearchTagFilter,
  useModelSearch,
} from "@/business/model";
import {useState} from "react";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";
import {createCsv} from "./ReportsPage.csvexport.tsx";
import {v7 as uuidv7} from "uuid";
import {ResultTable} from "@/views/reports/components/ResultTable.tsx";
import {FilterTagRowEditor} from "./components/FilterTagRowEditor.tsx";
import {FilterModelItemFieldRowEditor} from "./components/FilterModelItemFieldRowEditor.tsx";

const LOCAL_STORAGE_KEY = "reports-query-builder";
const ENABLE_MODEL_ITEM_FIELD_FILTER = false;

type FilterRowType = "tags" | "modelItemField";


function createFilterId() {
  return uuidv7();
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
                gridTemplateColumns: "1fr auto",
                gap: tokens.spacingHorizontalM,
                alignItems: "end",
              }}
            >
              <div />
              <Button
                appearance="primary"
                icon={<SearchFilled />}
                onClick={handleClickSearch}
              >
                Search
              </Button>
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "auto auto 1fr auto",
                rowGap: tokens.spacingVerticalM,
                columnGap: tokens.spacingHorizontalM,
                alignItems: "center",
              }}
            >
              {draftQuery.items.map((filter, index) => (
                <FilterRowEditor
                  key={filter.id}
                  filter={filter}
                  isFirstRow={index === 0}
                  operator={draftQuery.operator}
                  onDelete={handleDeleteFilter}
                  onOperatorChange={handleChangeOperator}
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
            <ResultTable items={items} />

          )}
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

function FilterRowEditor({
  filter,
  isFirstRow,
  operator,
  onDelete,
  onOperatorChange,
  onTypeChange,
  onChange,
}: {
  filter: ModelSearchFilter;
  isFirstRow: boolean;
  operator: ModelSearchReq["operator"];
  onDelete: (filterId: string) => void;
  onOperatorChange: DropdownProps["onOptionSelect"];
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
    <>
      {isFirstRow ? (
        <div />
      ) : (
        <Dropdown
          aria-label="Combine filters with"
          value={operator.toUpperCase()}
          selectedOptions={[operator]}
          onOptionSelect={onOperatorChange}
          appearance="outline"
        >
          <Option value="and">AND</Option>
          <Option value="or">OR</Option>
        </Dropdown>
      )}
      <Dropdown
        aria-label="Filter type"
        value={filter.type === "tags" ? "Tag" : "Model item field"}
        selectedOptions={[filter.type]}
        onOptionSelect={handleChangeType}
      >
        <Option value="tags">Tag</Option>
        {ENABLE_MODEL_ITEM_FIELD_FILTER && (
          <Option value="modelItemField">Model item field</Option>
        )}
      </Dropdown>

      <div>
        {filter.type === "tags" ? (
          <FilterTagRowEditor filter={filter} onChange={onChange} />
        ) : (
          <FilterModelItemFieldRowEditor filter={filter} onChange={onChange} />
        )}
      </div>
      <Button
        appearance="subtle"
        icon={<DeleteRegular />}
        onClick={() => onDelete(filter.id)}
      >
        Remove
      </Button>
    </>
  );
}
