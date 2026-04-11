import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { JournalistForm } from '../JournalistForm';

describe('JournalistForm', () => {
  it('renders form fields correctly', () => {
    render(<JournalistForm onSubmit={vi.fn()} />);
    
    expect(screen.getByLabelText(/First Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Last Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Phone/i)).toBeInTheDocument();
  });

  it('calls onSubmit with form data', () => {
    const handleSubmit = vi.fn();
    render(<JournalistForm onSubmit={handleSubmit} />);
    
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
