import type {ReactNode} from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';
import {FeatureCard} from "@site/src/components/FeatureCard";


export default function HomepageFeatures(): ReactNode {
  return (
    <section className={clsx("homeBlocks", styles.features)}>
      <div className="container">
        <div className="row homeGrid">
          <FeatureCard title="The Problem">
            <p>
              Most organizations don’t have a clear, shared description of the data they actually manage.
              Spread across Word and Wikis, inexistant or outdated, static documentation never works.
            </p>
            <hr/>
            <p>
              Read more on our statement:<br /><a href="/docs/resources/problem_en">The Missing Operational Data Model</a>
            </p>
          </FeatureCard>
          <FeatureCard title="What Medatarun provides">
            <p>Medatarun is a centralized, living reference to describe, document, tag, and interact with conceptual data models.
              For business users, IT, developers, tools, and AI agents.</p>
          </FeatureCard>
          <FeatureCard title="What it’s for">
            <p>Business and dev communication, IT governance, data architecture, tooling, and AI automation.</p>
          </FeatureCard>
        </div>
      </div>
    </section>
  );
}
