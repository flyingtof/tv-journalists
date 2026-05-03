import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { I18nProvider } from '../I18nProvider';
import { useI18n } from '../useI18n';

describe('I18nProvider', () => {
  it('should provide locale and t function to children', () => {
    function TestComponent() {
      const { locale, t } = useI18n();
      return (
        <div>
          <span data-testid="locale">{locale}</span>
          <span data-testid="message">{t('greeting')}</span>
        </div>
      );
    }

    render(
      <I18nProvider>
        <TestComponent />
      </I18nProvider>
    );

    expect(screen.getByTestId('locale')).toHaveTextContent('fr');
    expect(screen.getByTestId('message')).toHaveTextContent('Bonjour');
  });

  it('should pass params to translation function', () => {
    function TestComponent() {
      const { t } = useI18n();
      return <div>{t('welcome', { name: 'Bob' })}</div>;
    }

    render(
      <I18nProvider>
        <TestComponent />
      </I18nProvider>
    );

    expect(screen.getByText('Bienvenue, Bob!')).toBeInTheDocument();
  });
});

describe('useI18n', () => {
  it('should throw when used outside provider', () => {
    function TestComponent() {
      useI18n();
      return <div>Test</div>;
    }

    expect(() => render(<TestComponent />)).toThrow(
      'useI18n must be used within I18nProvider'
    );
  });
});
