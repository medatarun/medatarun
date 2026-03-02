import {type SearchResult} from "@/business/model";
import {downloadCsv} from "@seij/common-ui-csv-export";
import { type AppMessageKey } from "@/services/appI18n.tsx";

export function createCsv(
  items: SearchResult[],
  t: (key: AppMessageKey, values?: Record<string, unknown>) => string,
) {
  downloadCsv<SearchResult>(
    t("modelReportsCsv_filename"),
    [
      {
        code: "model",
        label: t("modelReportsCsv_modelLabel"),
        render: (it) => it.location.modelLabel,
      },
      {
        code: "type",
        label: t("modelReportsCsv_typeLabel"),
        render: (it) => {
          if (it.location.objectType === "model")
            return t("modelReportsCsv_typeModel");
          if (it.location.objectType === "entity")
            return t("modelReportsCsv_typeEntity");
          if (it.location.objectType === "entityAttribute")
            return t("modelReportsCsv_typeEntityAttribute");
          if (it.location.objectType === "relationship")
            return t("modelReportsCsv_typeRelationship");
          if (it.location.objectType === "relationshipAttribute")
            return t("modelReportsCsv_typeRelationshipAttribute");
          return t("modelReportsCsv_typeUnknown");
        },
      },
      {
        code: "entity",
        label: t("modelReportsCsv_entityRelationshipLabel"),
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
        label: t("modelReportsCsv_attributeLabel"),
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
        label: t("modelReportsCsv_tagsLabel"),
        render: (it) => (it.tags ?? []).join(" "),
      },
    ],
    items,
  );
}
