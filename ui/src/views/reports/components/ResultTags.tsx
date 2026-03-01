import {Tag, TagGroup} from "@fluentui/react-components";
import {sortBy} from "lodash-es";


export function ResultTags({tags}:{tags: string[]}) {
  return <TagGroup>
    {sortBy(tags ?? []).map((tag, index) => (
      <Tag key={index} appearance="outline" size="small">
        {tag}
      </Tag>
    ))}
  </TagGroup>
}