import {type SearchResult} from "@/business/model";
import {downloadCsv} from "@seij/common-ui-csv-export";

export function createCsv(items: SearchResult[]) {
  downloadCsv<SearchResult>(
    "tag-report.csv",
    [
      {
        code: "model",
        label: "Model",
        render: (it) => it.location.modelLabel,
      },
      {
        code: "type",
        label: "Type",
        render: (it) => {
          if (it.location.objectType === "model") return "Model";
          if (it.location.objectType === "entity") return "Entity";
          if (it.location.objectType === "entityAttribute")
            return "Entity attribute";
          if (it.location.objectType === "relationship") return "Relationship";
          if (it.location.objectType === "relationshipAttribute")
            return "Relationship attribute";
          return "unknown";
        },
      },
      {
        code: "entity",
        label: "Entity/Relationship",
        render: (it) => {
          if (it.location.entityLabel) return it.location.entityLabel;
          if (it.location.relationshipLabel != null) {
            return it.location.relationshipLabel;
          }
          return "";
        },
      },
      {
        code: "attribute",
        label: "Attribute",
        render: (it) => {
          if (it.location.entityAttributeLabel) {
            return it.location.entityAttributeLabel;
          }
          if (it.location.relationshipAttributeLabel) {
            return it.location.relationshipAttributeLabel;
          }
          return "";
        },
      },
      {
        code: "tags",
        label: "Tags",
        render: (it) => (it.tags ?? []).join(" "),
      },
    ],
    items,
  );
}
