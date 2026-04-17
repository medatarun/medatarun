import { ActionUILocations } from "@/business/action_registry";
import { Tag } from "@/business/tag";

/**
 * Given a tag, gives the filter name for actions so we can display only actions
 * possible on this type of tag
 * @param tag
 */
export function detailActionLocation(tag: Tag) {
  return tag.isGlobal
    ? ActionUILocations.tag_global_detail
    : ActionUILocations.tag_local_detail;
}
