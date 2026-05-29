import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { JournalistForm } from '../JournalistForm';
import { I18nProvider } from '../../i18n/I18nProvider';
import type { JournalistWrite, LookupOption } from '../../types';

describe('JournalistForm', () => {
  it('renders form fields correctly', () => {
    render(
      <I18nProvider>
        <JournalistForm onSubmit={vi.fn()} />
      </I18nProvider>
    );
    
    expect(screen.getByLabelText(/^Prénom$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Nom$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Email$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Téléphone$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Prénom$/i)).toHaveClass('journalist-form-input');
    expect(screen.getByLabelText(/^Nom$/i)).toHaveClass('journalist-form-input');
  });

  it('declares autocomplete metadata on personal contact fields', () => {
    render(
      <I18nProvider>
        <JournalistForm onSubmit={vi.fn()} />
      </I18nProvider>
    );

    expect(screen.getByLabelText(/^Prénom$/i)).toHaveAttribute('autocomplete', 'given-name');
    expect(screen.getByLabelText(/^Nom$/i)).toHaveAttribute('autocomplete', 'family-name');
    expect(screen.getByLabelText(/^Email$/i)).toHaveAttribute('autocomplete', 'email');
    expect(screen.getByLabelText(/^Téléphone$/i)).toHaveAttribute('autocomplete', 'tel');
  });

  it('calls onSubmit with form data', () => {
    const handleSubmit = vi.fn();
    render(
      <I18nProvider>
        <JournalistForm onSubmit={handleSubmit} />
      </I18nProvider>
    );
    
    fireEvent.change(screen.getByLabelText(/^Prénom$/i), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText(/^Nom$/i), { target: { value: 'Doe' } });
    fireEvent.change(screen.getByLabelText(/^Email$/i), { target: { value: 'john@example.com' } });
    
    fireEvent.click(screen.getByRole('button', { name: /Sauver/i }));
    
    expect(handleSubmit).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      globalEmail: 'john@example.com',
      globalPhone: '',
      activities: [],
    });
  });

  it('renders activity inputs when initial data includes activities', () => {
    const initialData: JournalistWrite = {
      firstName: 'Jane',
      lastName: 'Doe',
      globalEmail: '',
      globalPhone: '',
      activities: [
        {
          id: 'activity-1',
          mediaId: 'media-1',
          role: 'Reporter',
          specificEmail: 'jane.doe@press.com',
          specificPhone: '+33123456789',
          themeIds: ['theme-1'],
        },
      ],
    };
    const mediaOptions: LookupOption[] = [{ id: 'media-1', name: 'Green Press' }];
    const themeOptions: LookupOption[] = [{ id: 'theme-1', name: 'Biodiversity' }];

    render(
      <I18nProvider>
        <JournalistForm
          onSubmit={vi.fn()}
          initialData={initialData}
          mediaOptions={mediaOptions}
          themeOptions={themeOptions}
        />
      </I18nProvider>
    );

    expect(screen.getByLabelText(/Média/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Rôle/i)).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /^Activités$/i })).toBeInTheDocument();
    expect(screen.getByText(/^Thématiques 1$/i)).toBeInTheDocument();
  });

  describe('Media autocomplete', () => {
    it('uses autocomplete input for media selection', () => {
      const mediaOptions: LookupOption[] = [
        { id: 'media-1', name: 'France 2' },
        { id: 'media-2', name: 'France 3' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: '',
            role: '',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={vi.fn()}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={[]}
          />
        </I18nProvider>
      );

      // Should render an activity with media input
      expect(screen.getByLabelText(/Média 1/i)).toBeInTheDocument();
      // Should be an input, not a select
      const mediaInput = screen.getByLabelText(/Média 1/i) as HTMLInputElement;
      expect(mediaInput.tagName).toBe('INPUT');
      expect(mediaInput.type).toBe('text');
    });

    it('shows the current media name when editing an activity', () => {
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: 'media-1',
            role: 'Reporter',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };
      const mediaOptions: LookupOption[] = [
        { id: 'media-1', name: 'France 2' },
        { id: 'media-2', name: 'France 3' },
      ];

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={vi.fn()}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={[]}
          />
        </I18nProvider>
      );

      expect(screen.getByLabelText(/Média 1/i)).toHaveValue('France 2');
    });

    it('filters media options as user types', async () => {
      const mediaOptions: LookupOption[] = [
        { id: 'media-1', name: 'France 2' },
        { id: 'media-2', name: 'France 3' },
        { id: 'media-3', name: 'Le Monde' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: '',
            role: '',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };

      const user = userEvent.setup();

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={vi.fn()}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={[]}
          />
        </I18nProvider>
      );

      const mediaInput = screen.getByLabelText(/Média 1/i) as HTMLInputElement;
      await user.type(mediaInput, 'France');
      
      // Should show filtered suggestions
      await waitFor(() => {
        expect(screen.getByText('France 2')).toBeInTheDocument();
        expect(screen.getByText('France 3')).toBeInTheDocument();
      });
      expect(screen.queryByText('Le Monde')).not.toBeInTheDocument();
    });

    it('selects media and updates form data', async () => {
      const handleSubmit = vi.fn();
      const mediaOptions: LookupOption[] = [
        { id: 'media-1', name: 'France 2' },
        { id: 'media-2', name: 'France 3' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: '',
            role: '',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };

      const user = userEvent.setup();

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={handleSubmit}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={[]}
          />
        </I18nProvider>
      );

      const mediaInput = screen.getByLabelText(/Média 1/i) as HTMLInputElement;
      await user.type(mediaInput, 'France 2');
      
      // Wait for the suggestion to appear
      await waitFor(() => {
        expect(screen.getByText('France 2')).toBeInTheDocument();
      });

      // Click on the France 2 suggestion
      const suggestion = screen.getByText('France 2');
      fireEvent.mouseDown(suggestion);

      // Check that form data includes the selected mediaId
      fireEvent.click(screen.getByRole('button', { name: /Sauver/i }));

      expect(handleSubmit).toHaveBeenCalled();
      const callArgs = handleSubmit.mock.calls[0][0];
      expect(callArgs.activities[0].mediaId).toBe('media-1');
    });
  });

  describe('Theme autocomplete', () => {
    it('uses autocomplete input for theme selection', () => {
      const themeOptions: LookupOption[] = [
        { id: 'theme-1', name: 'Politics' },
        { id: 'theme-2', name: 'Sports' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: 'media-1',
            role: 'Reporter',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };
      const mediaOptions: LookupOption[] = [{ id: 'media-1', name: 'France 2' }];

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={vi.fn()}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={themeOptions}
          />
        </I18nProvider>
      );

      // Should have autocomplete input for themes
      expect(screen.getByText(/Thématiques 1/i)).toBeInTheDocument();
    });

    it('filters theme options as user types', async () => {
      const themeOptions: LookupOption[] = [
        { id: 'theme-1', name: 'Politics' },
        { id: 'theme-2', name: 'Sports' },
        { id: 'theme-3', name: 'Science' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: 'media-1',
            role: 'Reporter',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };
      const mediaOptions: LookupOption[] = [{ id: 'media-1', name: 'France 2' }];

      const user = userEvent.setup();

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={vi.fn()}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={themeOptions}
          />
        </I18nProvider>
      );

      // Look for the theme input within the fieldset
      const fieldset = screen.getByText(/Thématiques 1/i).closest('fieldset');
      expect(fieldset).toBeInTheDocument();
      
      const themeInputs = fieldset!.querySelectorAll('input[type="text"]');
      expect(themeInputs.length).toBeGreaterThan(0);
      
      const themeInput = themeInputs[0] as HTMLInputElement;
      await user.type(themeInput, 'Sp');
      
      // Should show filtered suggestions
      await waitFor(() => {
        expect(screen.getByText('Sports')).toBeInTheDocument();
      });
      expect(screen.queryByText('Science')).not.toBeInTheDocument();
    });

    it('adds theme and displays as tag when selected', async () => {
      const handleSubmit = vi.fn();
      const themeOptions: LookupOption[] = [
        { id: 'theme-1', name: 'Politics' },
        { id: 'theme-2', name: 'Sports' },
      ];
      const initialData: JournalistWrite = {
        firstName: 'Jane',
        lastName: 'Doe',
        globalEmail: '',
        globalPhone: '',
        activities: [
          {
            id: 'activity-1',
            mediaId: 'media-1',
            role: 'Reporter',
            specificEmail: '',
            specificPhone: '',
            themeIds: [],
          },
        ],
      };
      const mediaOptions: LookupOption[] = [{ id: 'media-1', name: 'France 2' }];

      const user = userEvent.setup();

      render(
        <I18nProvider>
          <JournalistForm
            onSubmit={handleSubmit}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={themeOptions}
          />
        </I18nProvider>
      );

      const fieldset = screen.getByText(/Thématiques 1/i).closest('fieldset');
      const themeInput = fieldset!.querySelector('input[type="text"]') as HTMLInputElement;
      
      await user.type(themeInput, 'Politics');
      
      // Wait for suggestion to appear
      await waitFor(() => {
        expect(screen.getByText('Politics')).toBeInTheDocument();
      });

      // Click on the Politics suggestion
      const suggestion = screen.getByText('Politics');
      fireEvent.mouseDown(suggestion);

      // Theme should be displayed as a tag
      await waitFor(() => {
        expect(screen.getByText('Politics')).toBeInTheDocument();
      });

      // Form submission should include the theme
      fireEvent.click(screen.getByRole('button', { name: /Sauver/i }));

      expect(handleSubmit).toHaveBeenCalled();
      const callArgs = handleSubmit.mock.calls[0][0];
      expect(callArgs.activities[0].themeIds).toContain('theme-1');
    });
  });
});
