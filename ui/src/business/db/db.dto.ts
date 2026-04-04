export interface DriverDto {
  id: string;
  name: string;
  location: string;
  className: string;
}

export interface DatasourceDto {
  id: string;
  driver: string;
  url: string;
  usernamee: string;
  properties: Record<string, string>;
}
