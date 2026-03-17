import {type ModelChangeEventDto} from "@/business/model";
import {useAppI18n} from "@/services/appI18n.tsx";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";
import {Badge, Text,} from "@fluentui/react-components";
import type {ReactNode} from "react";
import {Markdown} from "@/components/core/Markdown.tsx";
import {useModelContext} from "@/components/business/model/ModelContext.tsx";
import {useTags} from "@/business/tag";
import {modelTagScope} from "@/components/core/Tag.tsx";

export function ModelHistoryChanges({items}: { items: ModelChangeEventDto[] }) {
  return <div>
    {
      items.length
        ? <ModelHistoryChangesLog items={items}/>
        : <ModelHistoryChangesNoChanges/>
    }
  </div>
}

function ModelHistoryChangesLog({items}: { items: ModelChangeEventDto[] }) {
  return (<div>
    {items.map(it => <EventRenderer key={it.eventId} event={it}/>)}
  </div>)
}

function ModelHistoryChangesNoChanges() {
  const {t} = useAppI18n();
  return <MissingInformation>{t("modelHistoryPage_noChanges")}</MissingInformation>
}

function EventRenderer({event}: { event: ModelChangeEventDto }) {
  return <div style={{marginBottom: "1em"}}>
    <div><Text weight={"semibold"}>{event.actorDisplayName}</Text> <EventPayloadName event={event}/></div>
    <div>{new Date(event.createdAt).toLocaleString()}</div>
    <div><EventPayloadRenderer event={event}/></div>
  </div>
}

function EventPayloadName({event}: { event: ModelChangeEventDto }) {
  const repr = eventMap[event.eventType]
  if (repr) {
    return repr.label(event)
  }
  return event.eventType
}

function EventPayloadRenderer({event}: { event: ModelChangeEventDto }) {
  const repr = eventMap[event.eventType]
  return <div>
    {repr && repr.payload(event)}
    { /* <pre>{JSON.stringify(event.payload, null, 2)}</pre> */}
    <hr/>
  </div>;
}

type EventDisplayMode = {
  label: (event: ModelChangeEventDto) => ReactNode,
  payload: (event: ModelChangeEventDto) => ReactNode
}


function EntityNameOf({entityId}: { entityId: string }) {
  const model = useModelContext()
  return model.findEntityNameOrKey(entityId) ?? "[unknown]"
}

function EntityAttributeNameOf({entityId, attributeId}: { entityId: string, attributeId: string }) {
  const model = useModelContext()
  return model.findEntityAttributeNameOrKey(entityId, attributeId) ?? "[unknown]"
}

function TagNameOf({tagId}: { tagId: string }) {
  const model = useModelContext()
  const {tags} = useTags(modelTagScope(model.id))
  const tagName = tags.nameOrKeyOrId(tagId)
  return <Badge appearance={"outline"} >{tagName}</Badge>
}

function SlotTextValue({value}: { value: unknown }) {
  return <Text weight={"semibold"}>{value}</Text>
}

const eventMap: Record<string, EventDisplayMode> = {
  "model_aggregate_stored": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created from import"),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_created": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("created manually"),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_name_updated": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed name to {name}", {
        name: <SlotTextValue value={event.payload.name}/>
      }),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_key_updated": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed key to {key}", {
        key: <SlotTextValue value={event.payload.key}/>
      }),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_description_updated": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed model description"),
    payload: (event: ModelChangeEventDto) => <Markdown value={(event.payload.description as string) ?? ""}/>
  },
  "model_authority_updated": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed authority to {authority}", {
        authority: <SlotTextValue value={event.payload.authority}/>
      }),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_release": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("released version {version}", {version: <SlotTextValue value={event.payload.version}/>}),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_documentation_home_updated": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("changed documentation home to {documentationHome}", {
        documentationHome: <SlotTextValue value={event.payload.documentationHome}/>
      }),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_tag_added": {
    label: (event: ModelChangeEventDto) =>
      renderTranslatedSlots("added tag {tag}", {
        tag: <TagNameOf tagId={event.payload.tagId as string}/>
      }),
    payload: (event: ModelChangeEventDto) => null
  },
  "model_tag_deleted": {
    label: (event: ModelChangeEventDto) => renderTranslatedSlots("removed tag {tag}", {
      tag: <TagNameOf tagId={event.payload.tagId as string}/>
    }),
    payload: (event: ModelChangeEventDto) => null
  },

  "type_created": {
    label: (event: ModelChangeEventDto) => renderTranslatedSlots("created type {type}", {type: <SlotTextValue value={event.payload.key} /> }),
    payload: (event: ModelChangeEventDto) => null
  },

  "entity_name_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf
      entityId={event.payload.entityId as string}/></Text>,
    payload: (event: ModelChangeEventDto) => <span>New name: {event.payload.name as string}</span>
  },
  "entity_attribute_name_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf
      entityId={event.payload.entityId as string}/>'s attribute <EntityAttributeNameOf
      entityId={event.payload.entityId as string} attributeId={event.payload.attributeId as string}/></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "entity_attribute_description_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf
      entityId={event.payload.entityId as string}/>'s attribute <EntityAttributeNameOf
      entityId={event.payload.entityId as string} attributeId={event.payload.attributeId as string}/></Text>,
    payload: (event: ModelChangeEventDto) => <Markdown value={(event.payload.description as string) ?? ""}/>
  }
}


function renderTranslatedSlots(
  template: string,
  slots: Record<string, ReactNode> = {},
): ReactNode {
  return template.split(/(\{[a-zA-Z0-9_]+\})/g).map((part, index) => {
    const match = part.match(/^\{([a-zA-Z0-9_]+)\}$/);
    if (!match) return <span key={index}>{part}</span>;
    return <span key={index}>{slots[match[1]] ?? part}</span>;
  });
}