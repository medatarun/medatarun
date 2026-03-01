import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {Button, Dropdown, type DropdownProps, Option, tokens,} from "@fluentui/react-components";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {
  AddRegular,
  ArrowDownloadRegular,
  DismissRegular,
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
import {FilterTextRowEditor} from "./components/FilterTextRowEditor.tsx";
import {useCompactDropdownStyles} from "./components/Reports.styles.tsx";
import {ButtonBar, Loader} from "@seij/common-ui";

const LOCAL_STORAGE_KEY = "reports-query-builder";

type FilterRowType = "tags" | "text";


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

function createDefaultTextFilter(): ModelSearchFilter {
  return {
    id: createFilterId(),
    type: "text",
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
  return ["tags", "text"];
}

function changeFilterType(type: FilterRowType): ModelSearchFilter {
  if (type === "text") {
    return createDefaultTextFilter();
  }
  return createDefaultTagFilter();
}

export function ReportsPage() {
  const styles = useCompactDropdownStyles();
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
                <DocumentBulletListRegular /> Report model items
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
                  compactDropdownClassName={styles.compactDropdown}
                  onDelete={handleDeleteFilter}
                  onOperatorChange={handleChangeOperator}
                  onTypeChange={handleChangeFilterType}
                  onChange={handleUpdateFilter}
                />
              ))}
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "auto auto 1fr auto",
                columnGap: tokens.spacingHorizontalM,
                alignItems: "center",
              }}
            >
              <div style={{ gridColumn: "1 / 4" }}>
                <ButtonBar>
                  <Button
                    appearance="primary"
                    icon={<SearchFilled />}
                    disabled={draftQuery.items.length === 0}
                    onClick={handleClickSearch}
                  >
                    Show results
                  </Button>
                  {items.length > 0 && (
                    <Button
                      icon={<ArrowDownloadRegular />}
                      onClick={() => createCsv(items)}
                    >
                      Download CSV
                    </Button>
                  )}
                </ButtonBar>
              </div>
              <Button appearance="outline" icon={<AddRegular />} onClick={handleAddFilter}>
                Add condition
              </Button>
            </div>
          </div>
        </ContainedFixed>
        <ContainedScrollable>
          {!hasFilters && (
            <div style={{ padding: tokens.spacingVerticalM }}>
              <MissingInformation>Add at least one filter to search.</MissingInformation>
            </div>
          )}
          {query.isPending && (
            <div style={{ padding: tokens.spacingVerticalM }}>
              <Loader loading={true} />
            </div>
          )}
          {hasFilters && !query.isPending && items.length === 0 && (
            <div style={{ padding: tokens.spacingVerticalM }}>
              <MissingInformation>No results.</MissingInformation>
            </div>
          )}
          {!query.isPending && items.length > 0 && (
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
  compactDropdownClassName,
  onDelete,
  onOperatorChange,
  onTypeChange,
  onChange,
}: {
  filter: ModelSearchFilter;
  isFirstRow: boolean;
  operator: ModelSearchReq["operator"];
  compactDropdownClassName: string;
  onDelete: (filterId: string) => void;
  onOperatorChange: DropdownProps["onOptionSelect"];
  onTypeChange: (filterId: string, type: FilterRowType) => void;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const handleChangeType: DropdownProps["onOptionSelect"] = (_, data) => {
    const type = data.optionValue;
    if (type !== "tags" && type !== "text") {
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
          className={compactDropdownClassName}
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
        className={compactDropdownClassName}
        aria-label="Filter type"
        value={filter.type === "tags" ? "Tags" : "Text contains"}
        selectedOptions={[filter.type]}
        onOptionSelect={handleChangeType}
      >
        <Option value="tags">Tags</Option>
        <Option value="text">Text contains</Option>
      </Dropdown>

      <div>
        {filter.type === "tags" ? (
          <FilterTagRowEditor filter={filter} onChange={onChange} />
        ) : (
          <FilterTextRowEditor filter={filter} onChange={onChange} />
        )}
      </div>
      <Button
        appearance="subtle"
        icon={<DismissRegular />}
        onClick={() => onDelete(filter.id)}
      >
        Remove
      </Button>
    </>
  );
}
