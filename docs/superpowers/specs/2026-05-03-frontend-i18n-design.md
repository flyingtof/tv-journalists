# Frontend i18n design

## Problem

The frontend currently embeds user-visible strings directly inside React components and tests. This makes copy changes error-prone, spreads wording decisions across the codebase, and blocks future localization work.

We want to remove hard-coded frontend text by introducing a real i18n layer. For this first step, the application will support French only, but the architecture must be ready for additional locales later. Only strings produced by the frontend are in scope; backend-provided messages remain unchanged.

## Goals

- Replace hard-coded user-visible frontend strings with i18n keys
- Introduce a reusable i18n infrastructure for React components
- Keep the first rollout limited to a single `fr` locale
- Preserve current UI behavior and copy
- Make future locale additions straightforward

## Non-goals

- Translating backend error payloads
- Adding a language switcher in the UI
- Shipping English content in this first step
- Reworking unrelated component structure or styling

## Recommended approach

Implement a lightweight in-house i18n layer instead of adding a third-party library now.

### Why this approach

- The current frontend has no existing i18n dependency or conventions
- The immediate need is simple key-based lookup with light interpolation
- A small custom layer keeps the implementation proportional to the project
- The design still leaves room to migrate to a library later if pluralization or formatting needs grow

### Rejected alternatives

#### 1. Centralized constants without i18n primitives

This would reduce duplication but would not create a real translation layer. It would not provide locale abstraction, interpolation, or a stable pattern for future languages.

#### 2. Introducing `react-intl` or `i18next` immediately

This would work, but it adds dependency weight and setup complexity that the current requirements do not yet justify.

## Architecture

Add a dedicated `frontend/src/i18n/` area with the following responsibilities:

- `messages/fr.ts`: French translation catalog
- `messages/index.ts`: locale registry
- `I18nProvider.tsx`: provides active locale and translation function
- `useI18n.ts`: hook for components
- `formatMessage.ts` or equivalent helper: key lookup and interpolation
- `types.ts`: message catalog typing helpers

## Translation model

### Locale model

- Start with a single locale: `fr`
- Keep locale selection internal and fixed for now
- Structure the provider so another locale can be added without changing component call sites

### Message keys

Use stable product-oriented keys grouped by feature, for example:

- `app.navigation.search`
- `login.title`
- `journalistSearch.empty`
- `journalistProfile.contact.email`
- `themeAdmin.feedback.loadError`

Avoid keys tied to DOM placement such as `page1.button2.label`.

### Interpolation

Support simple placeholder replacement from the start:

- `"pagination.summary": "{start}-{end} sur {total}"`
- `"nav.user.greeting": "{firstName} {lastName}"`

Interpolation only needs string and number substitution in this first version.

## Provider and hook API

Components will consume translations through a small hook:

```ts
const { t } = useI18n();
```

Expected usage:

```tsx
<h1>{t('journalistSearch.title')}</h1>
<span>{t('pagination.summary', { start, end, total })}</span>
```

The hook should hide catalog access so components do not import locale files directly.

## Missing key behavior

Missing keys should be explicit during development.

Recommended behavior:

- return a visible fallback such as `[missing: journalistSearch.title]`
- optionally log a warning in development

This keeps omissions easy to detect without crashing the app.

## Migration scope

This first implementation should cover all user-visible strings produced by the frontend, including:

- app shell and navigation
- login page
- journalist search page
- journalist profile page
- theme admin page
- user admin page
- shared components such as forms, empty states, buttons, and protected route messages
- frontend-generated loading, empty-state, validation, and error strings

Out of scope:

- backend-provided error messages rendered as-is

## Component migration strategy

Perform the migration in layers:

1. introduce infrastructure and French catalog
2. update root app wiring (`main.tsx`)
3. migrate shared components and layout text
4. migrate pages feature by feature
5. update tests that assert user-visible strings

This keeps the rollout incremental and reduces debugging scope if a regression appears.

## Testing strategy

### Unit coverage

Add focused tests for:

- translation lookup by key
- interpolation behavior
- missing-key fallback behavior
- provider/hook integration

### Component coverage

Update existing component tests to render through the i18n provider when necessary. Tests should continue to assert user-visible text, but now through the configured translation layer instead of scattered literals in component implementations.

### Verification

Run existing frontend checks:

- `cd frontend && npm test -- --run`
- `cd frontend && npm run build`
- `cd frontend && npm run lint`

## Risks and mitigations

### Risk: incomplete migration leaves mixed patterns

Mitigation: migrate all frontend-produced visible strings in the same change set and keep string lookup inside the new i18n layer.

### Risk: tests become brittle during migration

Mitigation: centralize provider setup helpers for tests and update assertions screen by screen.

### Risk: future locale support requires breaking changes

Mitigation: fix the public API now around `I18nProvider` and `useI18n()` so locale expansion only affects catalogs and selection logic.

## Implementation notes

- Keep the implementation dependency-free for now
- Prefer typed catalogs where practical so missing keys are caught early
- Do not expose raw catalogs to components
- Do not translate backend-originated messages in this phase

## Success criteria

- No user-visible frontend strings remain hard-coded in React components targeted by the migration
- All migrated UI text flows through the i18n layer
- The app still behaves in French exactly as before
- The frontend remains ready for future locales without rewriting component call sites
