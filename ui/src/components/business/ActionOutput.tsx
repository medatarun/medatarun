import type {ActionResp} from "../../business";
import {makeStyles, tokens} from "@fluentui/react-components";

export function ActionOutput({resp}:{resp:ActionResp|null|undefined}) {
  const output = resp
  if (output === null || output === undefined) {
    return String(output);
  }

  if (output.contentType == "text") {
    return output.text;
  }

  try {
    return JSON.stringify(output.json, null, 2);
  } catch {
    return String(output);
  }
}

const useStyles = makeStyles({
  root: {
    fontFamily: tokens.fontFamilyMonospace,
    padding: tokens.spacingHorizontalS,
    borderRadius: tokens.borderRadiusMedium,
    border: `${tokens.strokeWidthThick} solid ${tokens.colorNeutralStroke3}`,
    whiteSpace: "pre-wrap",
    overflow: "auto",
    maxHeight: "20em"
  }
})
export function ActionOutputBox({resp}:{resp:ActionResp|null|undefined}) {
  const styles = useStyles();
  if (!resp) return null
  return <div className={styles.root}><ActionOutput resp={resp}/></div>
}