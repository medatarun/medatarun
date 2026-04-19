import { useNavigate } from "@tanstack/react-router";
import type { SearchResultLocation } from "@/business/model";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
} from "@fluentui/react-components";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
} from "@/components/business/model/model.icons.tsx";

export function ResultPath({ location }: { location: SearchResultLocation }) {
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
          icon={<ModelIcon authority={undefined} />}
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
