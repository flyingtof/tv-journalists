import { describe, it, expect } from 'vitest';
import { formatMessage } from '../formatMessage';

describe('formatMessage', () => {
  const messages = {
    simple: 'Bonjour',
    withParam: 'Bonjour {name}',
    multipleParams: '{count} journalistes trouvés',
  };

  it('should return simple message without interpolation', () => {
    const result = formatMessage(messages, 'simple', {});
    expect(result).toBe('Bonjour');
  });

  it('should interpolate single parameter', () => {
    const result = formatMessage(messages, 'withParam', { name: 'Alice' });
    expect(result).toBe('Bonjour Alice');
  });

  it('should interpolate number parameter', () => {
    const result = formatMessage(messages, 'multipleParams', { count: 42 });
    expect(result).toBe('42 journalistes trouvés');
  });

  it('should return fallback for missing key', () => {
    const result = formatMessage(messages, 'nonexistent', {});
    expect(result).toBe('[missing: nonexistent]');
  });

  it('should handle empty params object', () => {
    const result = formatMessage(messages, 'simple');
    expect(result).toBe('Bonjour');
  });

  it('should leave unmatched placeholders as-is', () => {
    const result = formatMessage(messages, 'withParam', {});
    expect(result).toBe('Bonjour {name}');
  });
});
