import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'The Problem',
    Svg: require('@site/static/img/problem.svg').default,
    description: (
      <>
        Most organizations don’t have a clear, shared description of the data they actually manage.
          Spread across Word and Wikis, inexistant or outdated, static documentation never works.
      </>
    ),
  },
  {
    title: 'What Medatarun provides',
    Svg: require('@site/static/img/referenciel.svg').default,
    description: (
      <>
        Medatarun is a centralized, living reference to describe, document, tag, and interact with conceptual data models.
        For business users, IT, developers, tools, and AI agents.
      </>
    ),
  },
  {
    title: 'What it’s for',
    Svg: require('@site/static/img/usage.svg').default,
    description: (
      <>Business and dev communication, IT governance, data architecture, tooling, and AI automation.</>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('homeCard')}>
{/*
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img"/>
      </div>
*/}
      <div className="text--left padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={clsx("homeBlocks", styles.features)}>
      <div className="container">
        <div className="row homeGrid">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
