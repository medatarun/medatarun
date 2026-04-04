import { useQuery } from "@tanstack/react-query";
import { executeActionJson } from "@/business/action_runner";
import type { DatasourceDto, DriverDto } from "@/business/db/db.dto.ts";

export function useDatabaseDrivers() {
  return useQuery({
    queryKey: ["admin", "database", "drivers"],
    queryFn: async () => {
      const resp = await executeActionJson<{ drivers: DriverDto[] }>(
        "databases",
        "driver_list",
        {},
      );
      return resp.drivers;
    },
  });
}

export function useDatabaseDatasources() {
  return useQuery({
    queryKey: ["admin", "database", "datasources"],
    queryFn: async () => {
      const resp = await executeActionJson<{ datasources: DatasourceDto[] }>(
        "databases",
        "datasource_list",
        {},
      );
      return resp.datasources;
    },
  });
}
