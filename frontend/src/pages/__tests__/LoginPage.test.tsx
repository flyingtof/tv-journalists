import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { LoginPage } from '../LoginPage';
import { I18nProvider } from '../../i18n/I18nProvider';

describe('LoginPage', () => {
  const renderLoginPage = () =>
    render(
      <I18nProvider>
        <LoginPage />
      </I18nProvider>,
    );

  it('declares explicit autocomplete semantics for login autofill', () => {
    renderLoginPage();

    expect(screen.getByLabelText('Utilisateur')).toHaveAttribute('autocomplete', 'username');
    expect(screen.getByLabelText('Mot de passe')).toHaveAttribute('autocomplete', 'current-password');
  });

  it('renders username label using i18n', () => {
    renderLoginPage();

    expect(screen.getByLabelText('Utilisateur')).toBeInTheDocument();
  });

  it('renders password label using i18n', () => {
    renderLoginPage();

    expect(screen.getByLabelText('Mot de passe')).toBeInTheDocument();
  });

  it('renders submit button with i18n text', () => {
    renderLoginPage();

    expect(screen.getByRole('button', { name: 'Se connecter' })).toBeInTheDocument();
  });
});
