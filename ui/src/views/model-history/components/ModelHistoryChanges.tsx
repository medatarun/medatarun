import type {ModelChangeEventDto} from "@/business/model";
import {useAppI18n} from "@/services/appI18n.tsx";
import {MissingInformation} from "@/components/core/MissingInformation.tsx";

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
  return <div>
    {items.map((change) => (
      <pre key={change.eventId}>{JSON.stringify(change, null, 2)}</pre>
    ))}
  </div>
}

function ModelHistoryChangesNoChanges() {
  const {t} = useAppI18n();
  return <MissingInformation>{t("modelHistoryPage_noChanges")}</MissingInformation>
}