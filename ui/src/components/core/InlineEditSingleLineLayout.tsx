import {type ReactElement, type ReactNode, useEffect, useState} from "react";
import {type Problem, toProblem} from "@seij/common-types";
import {makeStyles, Popover, PopoverSurface, PopoverTrigger, tokens, Tooltip} from "@fluentui/react-components";
import {EditRegular as EditIcon} from "@fluentui/react-icons";
import {Button, ButtonBar, ErrorBox} from "@seij/common-ui";

const useStyles = makeStyles({
  readRoot: {
    position: "relative",
    cursor: "text",
    boxSizing: "border-box",
    width: "100%",
    "&:hover": {
      backgroundColor: tokens.colorBrandBackground2Hover
    },
    "&:hover [data-edit-icon]": {
      opacity: 0.5,
      pointerEvents: "auto",
      verticalAlign: "middle"
    }
  },
  editIcon: {
    display: "inline-block",
    zIndex: 0,
    position: "relative",
    right: "0",
    opacity: 0,
    pointerEvents: "none",
  },
  editorField: {
    position: "relative",
  },
  editorRoot: {
    position: "relative",
    backgroundColor: tokens.colorBrandBackground2
  },
  editorActionBar: {
    borderBottom: "1px solid " + tokens.colorNeutralStroke1,
    position: "absolute",
    top: "100%",
    right: 0,
    marginTop: tokens.spacingVerticalXS,
    //backgroundColor: tokens.colorNeutralBackground1,
    backgroundColor: "red",
    border: "1px solid " + tokens.colorNeutralStroke1,
    borderRadius: tokens.borderRadiusMedium,
    padding: tokens.spacingHorizontalS,
    zIndex: 10,
  }
})

export interface InlineEditSingleLineLayoutProps {
  /**
   * What to display in read mode
   */
  children?: ReactNode | ReactNode[] | null;
  /**
   * Editor to display in EditMode
   */
  editor: (intents: { commit: () => void, cancel: () => void, pending:boolean}) => ReactElement;
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
export const InlineEditSingleLineLayout = (
  {
    children,
    editor,
    onEditStarted,
    onEditStart,
    onEditOK,
    onEditCancel,
  }: InlineEditSingleLineLayoutProps) => {
  const styles = useStyles()
  const [editing, setEditing] = useState<boolean>(false);
  const [editStartedCalled, setEditStartedCalled] = useState<boolean>(false);
  const [error, setError] = useState<Problem | null>(null);
  const [pending, setPending] = useState<boolean>(false);


  const handleEdit = async () => {
    try {
      setError(null);
      await onEditStart();
      setEditing(true);
      setEditStartedCalled(false)
    } catch (err) {
      setError(toProblem(err));
    }
  };

  const handleEditOK = async () => {
    console.log("handleEditOK")
    try {
      setError(null);
      setPending(true);
      await onEditOK();
      setEditing(false);
      setPending(false);
      setEditStartedCalled(false)
    } catch (err) {
      setError(toProblem(err));
      setPending(false);
    }
  };
  const handleEditCancel = async () => {
    console.log("handleEditCancel")
    try {
      setError(null);
      setPending(true);
      await onEditCancel();
      setEditing(false);
      setPending(false);
      setEditStartedCalled(false)
    } catch (err: unknown) {
      setError(toProblem(err));
      setPending(false);
    }
  };


  useEffect(() => {
    if (editing && onEditStarted && !editStartedCalled) {
      onEditStarted()
      setEditStartedCalled(true)
    }
  }, [editing, onEditStarted, editStartedCalled]);

  if (!editing)
    return (
      <div className={styles.readRoot} onClick={handleEdit}>
        <div>{children}
          <div className={styles.editIcon} data-edit-icon>
            <Tooltip content="Edit" relationship="label">
              <EditIcon name="edit"/>
            </Tooltip>
          </div>
        </div>


      </div>
    );

  const Editor = editor({commit:handleEditOK, cancel:handleEditCancel, pending:pending})

  return (
    <div>
      <div className={styles.editorRoot}>
        <div className={styles.editorField}>

          <Popover open={true} unstable_disableAutoFocus={true} positioning={"below-start"} size={"small"}
                   withArrow={true}>
            <PopoverTrigger>
              {Editor}
            </PopoverTrigger>
            <PopoverSurface tabIndex={-1}>
              <div id="editor-action-bar">
                <ButtonBar variant="end">
                  <Button disabled={pending} onClick={handleEditCancel} variant="secondary">
                    Cancel
                  </Button>
                  <Button disabled={pending} onClick={handleEditOK} variant="primary">
                    OK
                  </Button>
                </ButtonBar>
              </div>
            </PopoverSurface>
          </Popover>

        </div>
      </div>
      {error && <ErrorBox error={error}/>}
    </div>
  );
}
