import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ActionsBar} from "../components/business/ActionsBar.tsx";

export function DashboardPage() {
  return <ViewLayoutContained title="Dashboard">
    <ActionsBar location="global" params={{


    }} />
  </ViewLayoutContained>
}