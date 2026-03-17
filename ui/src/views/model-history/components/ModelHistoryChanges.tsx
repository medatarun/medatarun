import {type ModelChangeEventDto, useModel} from "@/business/model";
import {useAppI18n} from "@/services/appI18n.tsx";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";
import {Text,} from "@fluentui/react-components";
import type {ReactNode} from "react";
import {Markdown} from "@/components/core/Markdown.tsx";
import {useModelContext} from "@/components/business/model/ModelContext.tsx";
import {useTags} from "@/business/tag";
import {modelTagScope, Tags} from "@/components/core/Tag.tsx";

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
    { /* <pre>{JSON.stringify(event.payload, null, 2)}</pre> */ }
    <hr />
  </div>;
}

type EventDisplayMode = {
  label: (event: ModelChangeEventDto) => ReactNode,
  payload: (event: ModelChangeEventDto) => ReactNode
}


function EntityNameOf({entityId}:{entityId: string})  {
  const model = useModelContext()
  return model.findEntityNameOrKey(entityId) ?? "[unknown]"
}

function EntityAttributeNameOf({entityId, attributeId}:{entityId: string, attributeId: string})  {
  const model = useModelContext()
  return model.findEntityAttributeNameOrKey(entityId, attributeId) ?? "[unknown]"
}

function TagNameOf({tagId}:{tagId: string}) {
  const model = useModelContext()
  const {tags} = useTags(modelTagScope(model.id))
  return tags.nameOrKeyOrId(tagId)
}


const eventMap: Record<string, EventDisplayMode> = {
  "model_aggregate_stored": {
    label: (event: ModelChangeEventDto) => "Created from import",
    payload: (event: ModelChangeEventDto) => null
  },
  "model_created": {
    label: (event: ModelChangeEventDto) => "Created manually",
    payload: (event: ModelChangeEventDto) => null
  },
  "model_name_updated": {
    label: (event: ModelChangeEventDto) => "Name changed to " + event.payload.name,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_key_updated": {
    label: (event: ModelChangeEventDto) => "Key changed to " + event.payload.key,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_description_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed model description</Text>,
    payload: (event: ModelChangeEventDto) => <Markdown value={(event.payload.description as string) ?? ""} />
  },
  "model_authority_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed authority to <strong>{"" + event.payload.authority}</strong></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_release": {
    label: (event: ModelChangeEventDto) => <Text>Released version <strong>{"" + event.payload.version}</strong></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_documentation_home_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed documentation home to <strong>{"" + event.payload.url}</strong></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_tag_added": {
    label: (event: ModelChangeEventDto) => <Text>Add tag <TagNameOf tagId={event.payload.tag_id as string} /></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "model_tag_deleted": {
    label: (event: ModelChangeEventDto) => <Text>Deleted tag <TagNameOf tagId={event.payload.tag_id as string} /></Text>,
    payload: (event: ModelChangeEventDto) => null
  },

  "model_type_created": {
    label: (event: ModelChangeEventDto) => <Text>Created type {event.payload.value}</Text>,
    payload: (event: ModelChangeEventDto) => null
  },

  "entity_name_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf entityId={event.payload.entity_id as string} /></Text>,
    payload: (event: ModelChangeEventDto) => <span>New name: {event.payload.value as string}</span>
  },
  "entity_attribute_name_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf entityId={event.payload.entity_id as string} />'s attribute <EntityAttributeNameOf entityId={event.payload.entity_id as string} attributeId={event.payload.attribute_id as string} /></Text>,
    payload: (event: ModelChangeEventDto) => null
  },
  "entity_attribute_description_updated": {
    label: (event: ModelChangeEventDto) => <Text>Changed name of <EntityNameOf entityId={event.payload.entity_id as string} />'s attribute <EntityAttributeNameOf entityId={event.payload.entity_id as string} attributeId={event.payload.attribute_id as string} /></Text>,
    payload: (event: ModelChangeEventDto) => <Markdown value={(event.payload.value as string) ?? ""} />
  }
}

