declare global {
  interface MedatarunConfig {
  }

  interface Window {
    __MEDATARUN_CONFIG__?: MedatarunConfig;
  }
}

export {}
