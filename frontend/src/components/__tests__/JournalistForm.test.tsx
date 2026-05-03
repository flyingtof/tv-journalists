import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { JournalistForm } from '../JournalistForm';
import { I18nProvider } from '../../i18n/I18nProvider';

describe('JournalistForm', () => {
  it('renders form fields correctly', () => {
    render(
      <I18nProvider>
        <JournalistForm onSubmit={vi.fn()} />
      </I18nProvider>
    );
    
    expect(screen.getByLabelText(/First Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Last Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Phone/i)).toBeInTheDocument();
  });

  it('declares autocomplete metadata on personal contact fields', () => {
    render(
      <I18nProvider>
        <JournalistForm onSubmit={vi.fn()} />
      </I18nProvider>
    );

    expect(screen.getByLabelText(/First Name/i)).toHaveAttribute('autocomplete', 'given-name');
    expect(screen.getByLabelText(/Last Name/i)).toHaveAttribute('autocomplete', 'family-name');
    expect(screen.getByLabelText(/Email/i)).toHaveAttribute('autocomplete', 'email');
    expect(screen.getByLabelText(/Phone/i)).toHaveAttribute('autocomplete', 'tel');
  });

  it('calls onSubmit with form data', () => {
    const handleSubmit = vi.fn();
    render(
      <I18nProvider>
        <JournalistForm onSubmit={handleSubmit} />
      </I18nProvider>
    );
    
    fireEvent.change(screen.getByLabelText(/First Name/i), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText(/Last Name/i), { target: { value: 'Doe' } });
    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'john@example.com' } });
    
    fireEvent.click(screen.getByRole('button', { name: /Save Profile/i }));
    
    expect(handleSubmit).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      globalEmail: 'john@example.com',
      globalPhone: '',
    });
  });
});
