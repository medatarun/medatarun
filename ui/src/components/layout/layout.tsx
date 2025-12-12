import {Outlet, useNavigate, useRouterState} from "@tanstack/react-router";
import {
  AppItem,
  makeStyles,
  NavDrawer,
  NavDrawerBody,
  NavDrawerHeader,
  NavItem,
  tokens
} from "@fluentui/react-components";
import {BoxMultipleArrowRight24Regular, CodeRegular} from "@fluentui/react-icons";
import {ModelIcon} from "../business/Icons.tsx";

const useStyles = makeStyles({
  root: {display: "grid", gridTemplateColumns: "261px auto", height: "100vh", maxHeight: "100vh", overflow: "hidden"},
  left: {
    color: tokens.colorNeutralForeground3,
    backgroundColor: tokens.colorNeutralBackground3,
    borderRight: `1px solid ${tokens.colorNeutralStroke3}`
  },
  right: {
    overflowY: "auto",
  },
  nav: {
    minWidth: "260px"
  }
});
const MedatarunIcon = BoxMultipleArrowRight24Regular;

export function Layout() {
  const styles = useStyles();
  const {location} = useRouterState()
  const selectedValue = (() => {
    if (location.pathname.startsWith("/commands")) return "commands"
    return "models"
  })();

  return <div className={styles.root}>
    <div className={styles.left}>
      <NavDrawerControlled selectedValue={selectedValue}/>
    </div>
    <main className={styles.right}>
      <Outlet/>
    </main>
  </div>
}

const NavDrawerControlled = ({selectedValue}: {
  selectedValue: string,


}) => {
  const styles = useStyles();
  const navigate = useNavigate()
  const handleNavigate = (item: string) => {
    if (item == "models") navigate({to: "/"})
    if (item == "commands") navigate({to: "/commands"})
  }
  return <NavDrawer
    selectedValue={selectedValue}
    open={true}
    density="medium"
    type="inline"
    className={styles.nav}
    onNavItemSelect={(_, data) => handleNavigate(data.value)}
  >
    <NavDrawerHeader>
    </NavDrawerHeader>
    <NavDrawerBody>
      <AppItem icon={<MedatarunIcon/>} onClick={()=>handleNavigate("models")}>Medatarun</AppItem>
      <NavItem icon={<ModelIcon/>} value="models">Models</NavItem>
      <NavItem icon={<CodeRegular/>} value="commands">Commands</NavItem>
    </NavDrawerBody>
  </NavDrawer>
}
