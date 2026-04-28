import type {
  InlineEditCallbacks,
  InlineEditCoordinator,
  InlineEditIdentifier,
} from "@/components/core/inline-edit-coordinator/InlineEditCoordinator.tsx";

/**
 * Default coordinator used by InlineEditCoordinatorProvider.
 *
 * openInlineEdit stores the inline edit component currently open.
 */
export class InlineEditCoordinatorDefault implements InlineEditCoordinator {
  private openInlineEdit: {
    // mounted inline edit component identifier
    identifier: InlineEditIdentifier;
    // callback functions expected to send signals to the component
    callbacks: InlineEditCallbacks;
  } | null = null;

  async requestEdit(identifier: InlineEditIdentifier): Promise<boolean> {
    if (
      this.openInlineEdit == null ||
      this.openInlineEdit.identifier === identifier
    ) {
      return true;
    }

    return this.openInlineEdit.callbacks.cancel();
  }

  registerActive(
    identifier: InlineEditIdentifier,
    callbacks: InlineEditCallbacks,
  ): void {
    this.openInlineEdit = { identifier, callbacks };
  }

  clearActive(identifier: InlineEditIdentifier): void {
    if (this.openInlineEdit?.identifier !== identifier) return;

    this.openInlineEdit = null;
  }
}
