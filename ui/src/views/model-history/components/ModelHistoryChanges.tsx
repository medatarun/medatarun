import { type ModelChangeEventDto } from "@/business/model";
import { useAppI18n } from "@/services/appI18n.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { Badge, Text } from "@fluentui/react-components";
import type { ReactNode } from "react";
import { Markdown } from "@/components/core/Markdown.tsx";
import { useModelContext } from "@/components/business/model/ModelContext.tsx";
import { useTags } from "@/business/tag";
import { modelTagScope } from "@/components/core/Tag.tsx";

export function ModelHistoryChanges({
  items,
}: {
  items: ModelChangeEventDto[];
}) {
  return (
    <div>
      {items.length ? (
        <ModelHistoryChangesLog items={items} />
      ) : (
        <ModelHistoryChangesNoChanges />
      )}
    </div>
  );
}

function ModelHistoryChangesLog({ items }: { items: ModelChangeEventDto[] }) {
  return (
    <div>
      {items.map((it) => (
        <EventRenderer key={it.eventId} event={it} />
      ))}
    </div>
  );
}

function ModelHistoryChangesNoChanges() {
  const { t } = useAppI18n();
  return (
    <MissingInformation>{t("modelHistoryPage_noChanges")}</MissingInformation>
  );
}

function EventRenderer({ event }: { event: ModelChangeEventDto }) {
  return (
    <div style={{ marginBottom: "1em" }}>
      <div>
        <Text weight={"semibold"}>{event.actorDisplayName}</Text>{" "}
        <EventPayloadName event={event} />
      </div>
      <div>{new Date(event.createdAt).toLocaleString()}</div>
      <div>
        <EventPayloadRenderer event={event} />
      </div>
    </div>
  );
}

function EventPayloadName({ event }: { event: ModelChangeEventDto }) {
  const repr = eventMap[event.eventType];
  if (repr) {
    return repr.label(event);
  }
  return event.eventType;
}

function EventPayloadRenderer({ event }: { event: ModelChangeEventDto }) {
  const repr = eventMap[event.eventType];
  return (
    <div>
      {repr && repr.payload(event)}
      {/* <pre>{JSON.stringify(event.payload, null, 2)}</pre> */}
      <hr />
    </div>
  );
}

type EventDisplayMode = {
  label: (event: ModelChangeEventDto) => ReactNode;
  payload: (event: ModelChangeEventDto) => ReactNode;
};

function EntityNameOf({ entityId }: { entityId: string }) {
  const model = useModelContext();
  return model.findEntityNameOrKey(entityId) ?? "[unknown]";
}

function EntityAttributeNameOf({
  entityId,
  attributeId,
}: {
  entityId: string;
  attributeId: string;
}) {
  const model = useModelContext();
  return (
    model.findEntityAttributeNameOrKey(entityId, attributeId) ?? "[unknown]"
  );
}

function TypeNameOf({ typeId }: { typeId: string }) {
  const model = useModelContext();
  return model.findTypeNameOrKey(typeId) ?? "[unknown]";
}

function RelationshipNameOf({ relationshipId }: { relationshipId: string }) {
  const model = useModelContext();
  return model.findRelationshipNameOrKey(relationshipId) ?? "[unknown]";
}

function RelationshipAttributeNameOf({
  relationshipId,
  attributeId,
}: {
  relationshipId: string;
  attributeId: string;
}) {
  const model = useModelContext();
  return (
    model.findRelationshipAttributeDto(relationshipId, attributeId)?.name ??
    model.findRelationshipAttributeDto(relationshipId, attributeId)?.key ??
    "[unknown]"
  );
}

function RelationshipRoleNameOf({
  relationshipId,
  relationshipRoleId,
}: {
  relationshipId: string;
  relationshipRoleId: string;
}) {
  const model = useModelContext();
  const role = model
    .findRelationshipDto(relationshipId)
    ?.roles.find((it) => it.id === relationshipRoleId);
  return role?.name ?? role?.key ?? "[unknown]";
}

function TagNameOf({ tagId }: { tagId: string }) {
  const model = useModelContext();
  const { tags } = useTags(modelTagScope(model.id));
  const tagName = tags.nameOrKeyOrId(tagId);
  return <Badge appearance={"outline"}>{tagName}</Badge>;
}

function SlotTextValue({ value }: { value: unknown }) {
  return <Text weight={"semibold"}>{value}</Text>;
}

function SlotBadgeValue({ value }: { value: unknown }) {
  return <Badge appearance={"outline"}>{String(value)}</Badge>;
}

function SlotLinkValue({ href }: { href: string }) {
  return (
    <a href={href} target={"_blank"} rel={"noreferrer"}>
      {href}
    </a>
  );
}

function OptionalityValue({ optional }: { optional: boolean }) {
  return <SlotBadgeValue value={optional ? "optional" : "required"} />;
}

function CardinalityValue({ cardinality }: { cardinality: unknown }) {
  return <SlotBadgeValue value={String(cardinality)} />;
}

const eventMap: Record<string, EventDisplayMode> = {
  model_aggregate_stored: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created from import"),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created manually"),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed name to {name}", {
        name: <SlotTextValue value={event.payload.name} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed key to {key}", {
        key: <SlotTextValue value={event.payload.key} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed model description"),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  model_authority_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed authority to {authority}", {
        authority: <SlotTextValue value={event.payload.authority} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_release: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("released version {version}", {
        version: <SlotTextValue value={event.payload.version} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_documentation_home_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed documentation home to {documentationHome}",
        {
          documentationHome: (
            <SlotTextValue value={event.payload.documentationHome} />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_tag_added: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("added tag {tag}", {
        tag: <TagNameOf tagId={event.payload.tagId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_tag_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("removed tag {tag}", {
        tag: <TagNameOf tagId={event.payload.tagId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  model_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("deleted model"),
    payload: (event: ModelChangeEventDto) => null,
  },
  type_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created type {type}", {
        type: <SlotTextValue value={event.payload.name ?? event.payload.key} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  type_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed key of type {type} to {key}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
        key: <SlotTextValue value={event.payload.key} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  type_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed name of type {type} to {name}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
        name: <SlotTextValue value={event.payload.name} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  type_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed description of type {type}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  type_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("deleted type {type}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created entity {entity}", {
        entity: (
          <SlotTextValue value={event.payload.name ?? event.payload.key} />
        ),
      }),
    payload: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "identifier attribute {attribute} with type {type} and mode {optional}",
        {
          attribute: (
            <SlotTextValue
              value={
                event.payload.identityAttributeName ??
                event.payload.identityAttributeKey
              }
            />
          ),
          type: (
            <TypeNameOf
              typeId={event.payload.identityAttributeTypeId as string}
            />
          ),
          optional: (
            <OptionalityValue
              optional={Boolean(event.payload.identityAttributeOptional)}
            />
          ),
        },
      ),
  },
  entity_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed key of entity {entity} to {key}", {
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        key: <SlotTextValue value={event.payload.key} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed name of entity {entity} to {name}", {
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        name: <SlotTextValue value={event.payload.name} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed description of entity {entity}", {
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  entity_identifier_attribute_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed identifier attribute of entity {entity} to {attribute}",
        {
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.identifierAttributeId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_documentation_home_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed documentation home of entity {entity} to {documentationHome}",
        {
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          documentationHome: (
            <SlotLinkValue href={String(event.payload.documentationHome)} />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_tag_added: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("added tag {tag} to entity {entity}", {
        tag: <TagNameOf tagId={event.payload.tagId as string} />,
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_tag_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("removed tag {tag} from entity {entity}", {
        tag: <TagNameOf tagId={event.payload.tagId as string} />,
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("deleted entity {entity}", {
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "created attribute {attribute} on entity {entity}",
        {
          attribute: (
            <SlotTextValue value={event.payload.name ?? event.payload.key} />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("type {type}, mode {optional}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
        optional: (
          <OptionalityValue optional={Boolean(event.payload.optional)} />
        ),
      }),
  },
  entity_attribute_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "deleted attribute {attribute} from entity {entity}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed key of attribute {attribute} on entity {entity} to {key}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          key: <SlotTextValue value={event.payload.key} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed name of attribute {attribute} on entity {entity} to {name}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          name: <SlotTextValue value={event.payload.name} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed description of attribute {attribute} on entity {entity}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  entity_attribute_type_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed type of attribute {attribute} on entity {entity} to {type}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          type: <TypeNameOf typeId={event.payload.typeId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_optional_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed mode of attribute {attribute} on entity {entity} to {optional}",
        {
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
          optional: (
            <OptionalityValue optional={Boolean(event.payload.optional)} />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_tag_added: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "added tag {tag} to attribute {attribute} on entity {entity}",
        {
          tag: <TagNameOf tagId={event.payload.tagId as string} />,
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  entity_attribute_tag_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "removed tag {tag} from attribute {attribute} on entity {entity}",
        {
          tag: <TagNameOf tagId={event.payload.tagId as string} />,
          attribute: (
            <EntityAttributeNameOf
              entityId={event.payload.entityId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created relationship {relationship}", {
        relationship: (
          <SlotTextValue value={event.payload.name ?? event.payload.key} />
        ),
      }),
    payload: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created with {roleCount} role(s)", {
        roleCount: (
          <SlotBadgeValue
            value={
              Array.isArray(event.payload.roles)
                ? event.payload.roles.length
                : 0
            }
          />
        ),
      }),
  },
  relationship_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed key of relationship {relationship} to {key}",
        {
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          key: <SlotTextValue value={event.payload.key} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed name of relationship {relationship} to {name}",
        {
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          name: <SlotTextValue value={event.payload.name} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed description of relationship {relationship}",
        {
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  relationship_role_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "created role {role} on relationship {relationship}",
        {
          role: (
            <SlotTextValue value={event.payload.name ?? event.payload.key} />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("entity {entity}, cardinality {cardinality}", {
        entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        cardinality: (
          <CardinalityValue cardinality={event.payload.cardinality} />
        ),
      }),
  },
  relationship_role_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed key of role {role} on relationship {relationship} to {key}",
        {
          role: (
            <RelationshipRoleNameOf
              relationshipId={event.payload.relationshipId as string}
              relationshipRoleId={event.payload.relationshipRoleId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          key: <SlotTextValue value={event.payload.key} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_role_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed name of role {role} on relationship {relationship} to {name}",
        {
          role: (
            <RelationshipRoleNameOf
              relationshipId={event.payload.relationshipId as string}
              relationshipRoleId={event.payload.relationshipRoleId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          name: <SlotTextValue value={event.payload.name} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_role_entity_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed entity of role {role} on relationship {relationship} to {entity}",
        {
          role: (
            <RelationshipRoleNameOf
              relationshipId={event.payload.relationshipId as string}
              relationshipRoleId={event.payload.relationshipRoleId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          entity: <EntityNameOf entityId={event.payload.entityId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_role_cardinality_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed cardinality of role {role} on relationship {relationship} to {cardinality}",
        {
          role: (
            <RelationshipRoleNameOf
              relationshipId={event.payload.relationshipId as string}
              relationshipRoleId={event.payload.relationshipRoleId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          cardinality: (
            <CardinalityValue cardinality={event.payload.cardinality} />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_tag_added: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("added tag {tag} to relationship {relationship}", {
        tag: <TagNameOf tagId={event.payload.tagId as string} />,
        relationship: (
          <RelationshipNameOf
            relationshipId={event.payload.relationshipId as string}
          />
        ),
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_tag_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "removed tag {tag} from relationship {relationship}",
        {
          tag: <TagNameOf tagId={event.payload.tagId as string} />,
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("deleted relationship {relationship}", {
        relationship: (
          <RelationshipNameOf
            relationshipId={event.payload.relationshipId as string}
          />
        ),
      }),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_role_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "deleted role {role} from relationship {relationship}",
        {
          role: (
            <RelationshipRoleNameOf
              relationshipId={event.payload.relationshipId as string}
              relationshipRoleId={event.payload.relationshipRoleId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_created: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "created attribute {attribute} on relationship {relationship}",
        {
          attribute: (
            <SlotTextValue value={event.payload.name ?? event.payload.key} />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("type {type}, mode {optional}", {
        type: <TypeNameOf typeId={event.payload.typeId as string} />,
        optional: (
          <OptionalityValue optional={Boolean(event.payload.optional)} />
        ),
      }),
  },
  relationship_attribute_name_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed name of attribute {attribute} on relationship {relationship} to {name}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          name: <SlotTextValue value={event.payload.name} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_description_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed description of attribute {attribute} on relationship {relationship}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => (
      <Markdown value={(event.payload.description as string) ?? ""} />
    ),
  },
  relationship_attribute_key_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed key of attribute {attribute} on relationship {relationship} to {key}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          key: <SlotTextValue value={event.payload.key} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_type_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed type of attribute {attribute} on relationship {relationship} to {type}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          type: <TypeNameOf typeId={event.payload.typeId as string} />,
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_optional_updated: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "changed mode of attribute {attribute} on relationship {relationship} to {optional}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
          optional: (
            <OptionalityValue optional={Boolean(event.payload.optional)} />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_tag_added: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "added tag {tag} to attribute {attribute} on relationship {relationship}",
        {
          tag: <TagNameOf tagId={event.payload.tagId as string} />,
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_tag_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "removed tag {tag} from attribute {attribute} on relationship {relationship}",
        {
          tag: <TagNameOf tagId={event.payload.tagId as string} />,
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
  relationship_attribute_deleted: {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots(
        "deleted attribute {attribute} from relationship {relationship}",
        {
          attribute: (
            <RelationshipAttributeNameOf
              relationshipId={event.payload.relationshipId as string}
              attributeId={event.payload.attributeId as string}
            />
          ),
          relationship: (
            <RelationshipNameOf
              relationshipId={event.payload.relationshipId as string}
            />
          ),
        },
      ),
    payload: (event: ModelChangeEventDto) => null,
  },
};

function renderTranslatedSlots(
  template: string,
  slots?: Record<string, ReactNode>,
): ReactNode {
  const resolvedSlots = slots ?? {};
  return template.split(/(\{[a-zA-Z0-9_]+\})/g).map((part, index) => {
    const match = part.match(/^\{([a-zA-Z0-9_]+)\}$/);
    if (!match) return <span key={index}>{part}</span>;
    return <span key={index}>{resolvedSlots[match[1]] ?? part}</span>;
  });
}
