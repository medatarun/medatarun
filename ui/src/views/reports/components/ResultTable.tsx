import type {SearchResult} from "@/business/model";
import {Table, TableBody, TableCell, TableRow} from "@fluentui/react-components";
import {ResultPath} from "./ResultPath.tsx";
import {ResultTags} from "./ResultTags.tsx";

export function ResultTable({items}: { items: SearchResult[] }) {
  return <Table>
    <TableBody>
      {items.map((it) => {
        return (
          <TableRow key={it.id}>
            <TableCell>
              <ResultPath location={it.location}/>
            </TableCell>
            <TableCell>
              <ResultTags tags={it.tags}/>
            </TableCell>
          </TableRow>
        );
      })}
    </TableBody>
  </Table>

}