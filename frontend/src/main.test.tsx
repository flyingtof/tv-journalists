import { describe, it, expect, vi, beforeEach } from 'vitest';
import { StrictMode } from 'react';

describe('main.tsx bootstrap', () => {
  beforeEach(() => {
    vi.resetModules();
  });

  it('should mount I18nProvider in the production bootstrap tree', async () => {
    // Mock react-dom/client to capture what gets rendered
    let renderedElement: React.ReactElement | null = null;
    
    vi.doMock('react-dom/client', () => ({
      createRoot: () => ({
        render: (element: React.ReactElement) => {
          renderedElement = element;
        },
      }),
    }));

    // Mock document.getElementById to avoid DOM errors
    const mockRootElement = document.createElement('div');
    vi.spyOn(document, 'getElementById').mockReturnValue(mockRootElement);

    // Import main.tsx after mocking - this will execute the bootstrap
    await import('./main.tsx');

    // Verify we captured something
    expect(renderedElement).not.toBeNull();

    // Check that the rendered element is StrictMode
    expect(renderedElement?.type).toBe(StrictMode);

    // Navigate down the tree: StrictMode > BrowserRouter > I18nProvider > AuthProvider > App
    const strictModeChildren = (renderedElement as React.ReactElement)?.props?.children;
    expect(strictModeChildren).toBeDefined();

    // BrowserRouter is the first child
    const browserRouter = strictModeChildren;
    expect(browserRouter?.type?.name).toBe('BrowserRouter');

    // I18nProvider is the child of BrowserRouter
    const i18nProvider = browserRouter?.props?.children;
    expect(i18nProvider?.type?.name).toBe('I18nProvider');

    // AuthProvider is the child of I18nProvider
    const authProvider = i18nProvider?.props?.children;
    expect(authProvider?.type?.name).toBe('AuthProvider');

    // App is the child of AuthProvider
    const app = authProvider?.props?.children;
    expect(app?.type?.name).toBe('App');
  });
});
