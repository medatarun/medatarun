import { defaultConnection } from "@seij/common-services";
import { reportUnauthorizedError } from "@/services/unauthorized.ts";

const apiConnection = {
  createHeaders() {
    return defaultConnection.createHeaders();
  },
  async get<T>(url: string): Promise<T> {
    try {
      return await defaultConnection.get<T>(url);
    } catch (error) {
      reportUnauthorizedError(error);
      throw error;
    }
  },
  async post<T>(url: string, data: Record<string, any>): Promise<T> {
    try {
      return await defaultConnection.post<T>(url, data);
    } catch (error) {
      reportUnauthorizedError(error);
      throw error;
    }
  },
  async delete<T>(url: string): Promise<T> {
    try {
      return await defaultConnection.delete<T>(url);
    } catch (error) {
      reportUnauthorizedError(error);
      throw error;
    }
  },
};

/**
 * Current connection to backend (singleton).
 *
 * Application main() should reconfigure it depending on environment variables or what the backend
 * tells it to do.
 */
export const api = () => apiConnection;
