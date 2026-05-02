import {
  Component,
  type ErrorInfo,
  type PropsWithChildren,
  type ReactNode,
} from "react";
import { toProblem } from "@seij/common-types";
import { appT } from "@/services/appI18n.tsx";
import { Logger } from "tslog";

type ErrorBoundaryProps = PropsWithChildren;
type ErrorBoundaryState = {
  hasError: boolean;
  error: Error | null;
};
const logger = new Logger();
export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    logger.error(error, info);
  }

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        <div>
          <div style={{ color: "red" }}>
            {appT("errorBoundary_fallbackMessage")}
          </div>
          <pre>{JSON.stringify(toProblem(this.state.error))}</pre>
        </div>
      );
    }

    return this.props.children;
  }
}
