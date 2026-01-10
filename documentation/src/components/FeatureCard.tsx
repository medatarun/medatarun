import {PropsWithChildren} from "react";
import clsx from "clsx";
import Heading from "@theme/Heading";

export function FeatureCard({title, children}: { title: string } & PropsWithChildren) {
  return (
    <div className={clsx('homeCard')}>
      <div className="text--left padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <div>{children}</div>
      </div>
    </div>
  );
}