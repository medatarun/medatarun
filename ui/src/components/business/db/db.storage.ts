import { useQuery } from "@tanstack/react-query";
import type { DatasourceDto, DriverDto } from "@/business/db/db.dto.ts";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";

export function useDatabaseDrivers() {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["admin", "database", "drivers"],
    queryFn: async () => {
      const resp = await performer.executeJson<{ drivers: DriverDto[] }>(
        "databases",
        "driver_list",
        {},
      );
      return resp.drivers;
    },
  });
}

export function useDatabaseDatasources() {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["admin", "database", "datasources"],
    queryFn: async () => {
      const resp = await performer.executeJson<{
        datasources: DatasourceDto[];
      }>("databases", "datasource_list", {});
      return resp.datasources;
    },
  });
}
