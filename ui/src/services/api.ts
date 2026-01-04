import {defaultConnection} from "@seij/common-services";

/**
 * Current connection to backend (singleton).
 *
 * Application main() should reconfigure it depending on environment variables or what the backend
 * tells it to do.
 */
export const api = ()=>defaultConnection;
