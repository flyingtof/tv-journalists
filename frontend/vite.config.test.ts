import { describe, expect, it } from 'vitest';
import config from './vite.config';

describe('vite dev server dependency optimization', () => {
  it('eagerly optimizes the core React entrypoints used by the app shell', () => {
    expect(config.optimizeDeps?.holdUntilCrawlEnd).toBe(true);
    expect(config.optimizeDeps?.include).toEqual(
      expect.arrayContaining([
        'react',
        'react-dom',
        'react-dom/client',
        'react-router-dom',
        'react/jsx-dev-runtime',
        'react/jsx-runtime',
      ]),
    );
  });
});
