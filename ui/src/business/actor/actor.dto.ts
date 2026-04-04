export interface WhoAmIRespDto {
  issuer:string
  sub:string
  fullname: string
  admin: boolean
  roles: string[]
}