import {SwitchButton} from "@seij/common-ui";
import {useDetailLevelContext} from "../components/business/DetailLevelContext.tsx";

export function PreferencesPage() {
  const {isDetailLevelTech, toggle} = useDetailLevelContext()
  return <div>
    <SwitchButton
      value={isDetailLevelTech}
      onValueChange={toggle}
      labelTrue={"Switch to business mode"}
      labelFalse={"Switch to technical mode"}/>
  </div>
}