import type { WhoAmIRespDto } from "@/business/actor/actor.dto.ts";

export class CurrentActor {
  private readonly dto: WhoAmIRespDto;

  constructor(dto: WhoAmIRespDto) {
    this.dto = dto;
    console.log("whoami", dto)
  }
  isAdmin() {
    return this.dto.admin
  }
}