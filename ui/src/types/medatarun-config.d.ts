declare global {
  // Base type for server-provided config; feature-specific files extend this interface.
  interface MedatarunConfig {
  }

  interface Window {
    __MEDATARUN_CONFIG__?: MedatarunConfig;
  }
}

export {}
