import { useEffect, useRef } from "react";

/**
 * Runs the "edit has started" callback.
 *
 * This is a component because React mounts it when the editor opens and unmounts
 * it when the editor closes. That gives us one call per edit session without a
 * second flag next to the editing state.
 */
export function InlineEditStarted({
  onEditStarted,
}: {
  onEditStarted?: () => void;
}) {
  const onEditStartedRef = useRef(onEditStarted);

  useEffect(() => {
    onEditStartedRef.current = onEditStarted;
  }, [onEditStarted]);

  useEffect(() => {
    // Call onEditStarted once per edit session.
    // Do not put onEditStarted in this effect dependencies. A parent can
    // recreate the function while the editor is still open. That is not a new
    // edit session.
    onEditStartedRef.current?.();
  }, []);

  return null;
}
