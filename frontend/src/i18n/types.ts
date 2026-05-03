export type MessageKey = string;

export type MessageParams = Record<string, string | number>;

export type Messages = Record<MessageKey, string>;

export type TranslateFunction = (key: MessageKey, params?: MessageParams) => string;

export interface I18nContextValue {
  locale: string;
  t: TranslateFunction;
}
