import { createContext, useMemo, type ReactNode } from 'react';
import { messages } from './messages';
import { formatMessage } from './formatMessage';
import type { MessageKey, MessageParams, I18nContextValue } from './types';

// eslint-disable-next-line react-refresh/only-export-components
export const I18nContext = createContext<I18nContextValue | null>(null);

interface I18nProviderProps {
  children: ReactNode;
}

export function I18nProvider({ children }: I18nProviderProps) {
  const locale = 'fr';
  
  const contextValue = useMemo(() => {
    const localeMessages = messages[locale];
    
    return {
      locale,
      t: (key: MessageKey, params?: MessageParams) =>
        formatMessage(localeMessages, key, params),
    };
  }, [locale]);
  
  return (
    <I18nContext.Provider value={contextValue}>
      {children}
    </I18nContext.Provider>
  );
}
