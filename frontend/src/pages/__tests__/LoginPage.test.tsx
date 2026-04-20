import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { LoginPage } from '../LoginPage';

describe('LoginPage', () => {
  it('declares explicit autocomplete semantics for login autofill', () => {
    render(<LoginPage />);

    expect(screen.getByLabelText('Utilisateur')).toHaveAttribute('autocomplete', 'username');
    expect(screen.getByLabelText('Mot de passe')).toHaveAttribute('autocomplete', 'current-password');
  });
});
