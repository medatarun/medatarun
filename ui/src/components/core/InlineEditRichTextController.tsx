import { type ReactNode, useState } from "react";
import { type Problem, toProblem } from "@seij/common-types";
import {
  makeStyles,
  mergeClasses,
  tokens,
  Tooltip,
} from "@fluentui/react-components";
import { Icon } from "@seij/common-ui-icons";
import { Button, ButtonBar, ErrorBox } from "@seij/common-ui";
import { useAppI18n } from "@/services/appI18n.tsx";
import { InlineEditStarted } from "./InlineEditStarted.tsx";

const useStyles = makeStyles({
  readRoot: {
    cursor: "text",
    boxSizing: "border-box",
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
  },
  editable: {
    "&:hover": {
      backgroundColor: tokens.colorBrandBackground2Hover,
    },
    "&:hover > .editIcon": {
      display: "block",
    },
  },
  editIcon: {
    float: "right",
    zIndex: 0,
    position: "relative",
    right: "0",
    opacity: 0.2,
  },
  editorRoot: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    backgroundColor: tokens.colorBrandBackground2,
  },
  editorActionBar: {
    borderBottom: "1px solid " + tokens.colorNeutralStroke1,
  },
});

export interface InlineEditRichTextControllerProps {
  /**
   * What to display in read mode
   */
  children?: ReactNode | ReactNode[] | null;
  /**
   * Editor to display in EditMode
   */
  editor: ReactNode;
  /**
   * Indicates if disabled or not
   */
  disabled?: boolean;
  /**
   * Called before the editor mode is switched to "on" (prepare data)
   * If the promise rejects, we will display an error before the editor
   * (don't really know if error handling is correct yet)
   */
  onEditStart: () => Promise<unknown>;
  /**
   * Called once the editor mode is switched to "on" (you can focus on the editor if needed)
   */
  onEditStarted?: () => void;
  /**
   * Called when the user clicks the "ok" button.
   *
   * If the promise resolves, we switch back to view mode.
   * If the promise rejects, we stay on editor mode, with error displayed.
   *
   * It is typically when you push the new value to your store/backend
   */
  onEditOK: () => Promise<unknown>;
  /**
   * Called when the user clicks the "cancel" button.
   *
   * If the promise resolves, we switch back to view mode.
   * If the promise rejects, we stay on editor mode, with error displayed.
   *
   * It is typically when you rollback the value to your store/backend with its original value.
   **/
  onEditCancel: () => Promise<unknown>;
}

/**
 * Component that switches between read and write modes inline.
 * Suitable for RichTextEditors with an OK an Cancel button on top of the editor.
 *
 * This component does manage the lifecycle and display but NOT the value itself.
 *
 * Editor must be provided
 */
export function InlineEditRichTextController({
  children,
  editor,
  onEditStarted,
  onEditStart,
  onEditOK,
  onEditCancel,
  disabled = false,
}: InlineEditRichTextControllerProps) {
  const { t } = useAppI18n();
  const styles = useStyles();
  const [editing, setEditing] = useState<boolean>(false);
  const [error, setError] = useState<Problem | null>(null);
  const [pending, setPending] = useState<boolean>(false);

  const handleEdit = async () => {
    try {
      setError(null);
      await onEditStart();
      setEditing(true);
    } catch (err) {
      setError(toProblem(err));
    }
  };

  const handleEditOK = async () => {
    try {
      setError(null);
      setPending(true);
      await onEditOK();
      setEditing(false);
      setPending(false);
    } catch (err) {
      setError(toProblem(err));
      setPending(false);
    }
  };
  const handleEditCancel = async () => {
    try {
      setError(null);
      setPending(true);
      await onEditCancel();
      setEditing(false);
      setPending(false);
    } catch (err: unknown) {
      setError(toProblem(err));
      setPending(false);
    }
  };

  if (!editing || disabled) {
    const rootClassName = mergeClasses(
      styles.readRoot,
      !disabled && styles.editable,
    );
    return (
      <div className={rootClassName} onClick={handleEdit}>
        {!disabled && (
          <div className={styles.editIcon}>
            <Tooltip
              content={t("inlineEditRichTextLayout_editTooltip")}
              relationship="label"
            >
              <Icon name="edit" />
            </Tooltip>
          </div>
        )}
        <div>{children}</div>
      </div>
    );
  }
  return (
    <div>
      <InlineEditStarted onEditStarted={onEditStarted} />
      <div className={styles.editorRoot}>
        <div className={styles.editorActionBar}>
          <ButtonBar variant="end">
            <Button
              disabled={pending}
              onClick={handleEditCancel}
              variant="secondary"
            >
              {t("inlineEditRichTextLayout_cancel")}
            </Button>
            <Button disabled={pending} onClick={handleEditOK} variant="primary">
              {t("inlineEditRichTextLayout_confirm")}
            </Button>
          </ButtonBar>
        </div>
        <div>{editor}</div>
      </div>
      {error && <ErrorBox error={error} />}
    </div>
  );
}
