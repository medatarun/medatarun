declare global {
  interface MedatarunConfig {
    username?: string;
    auth_ctx?: string;
    error?: string;
    clientName?: string;
    clientId?: string;
    clientInternal?: boolean;
  }
}

export {};
