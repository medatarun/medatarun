import ReactMarkdown from "react-markdown";
import {
  makeStaticStyles,
  makeStyles,
  tokens,
} from "@fluentui/react-components";
const useStyles = makeStyles({
  root: {
    "& p": {
      marginTop: 0,
      marginBottom: tokens.spacingVerticalS,
      color: tokens.colorNeutralForeground2,
    },
    "& p:last-child": {
      marginBottom: 0,
    },
  },
});
export function Markdown({ value }: { value: string | null }) {
  const styles = useStyles();
  if (value == null) return null;

  return (
    <div className={styles.root}>
      <ReactMarkdown>{value}</ReactMarkdown>
    </div>
  );
}
