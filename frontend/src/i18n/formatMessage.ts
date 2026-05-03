import type { Messages, MessageKey, MessageParams } from './types';

export function formatMessage(
  messages: Messages,
  key: MessageKey,
  params?: MessageParams
): string {
  const message = messages[key];
  
  if (message === undefined) {
    return `[missing: ${key}]`;
  }
  
  if (!params) {
    return message;
  }
  
  return message.replace(/\{(\w+)\}/g, (match, paramName) => {
    const value = params[paramName];
    return value !== undefined ? String(value) : match;
  });
}
