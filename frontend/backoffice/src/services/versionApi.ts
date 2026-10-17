import { getApiBaseUrl } from './http';

export const FALLBACK_APP_VERSION = 'dev';

export const getAppVersion = async (): Promise<string> => {
  const response = await fetch(`${getApiBaseUrl()}/app/version`);

  if (!response.ok) {
    throw new Error('Unable to retrieve the application version.');
  }

  const payload = await response.json() as { version?: string };

  return payload.version || FALLBACK_APP_VERSION;
};

