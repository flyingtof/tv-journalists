# Copilot instructions for `tv-journalists`

## Build, test, and lint commands

- Backend run: `cd backend && mvn spring-boot:run`
- Backend tests: `mvn -q -pl backend test`
- Single backend test class: `mvn -q -pl backend -Dtest=JournalistApplicationServiceTest test`
- Multiple backend test classes: `mvn -q -pl backend -Dtest=CreateJournalistCommandTest,LogInteractionCommandTest test`
- Backend checkstyle: `mvn -q -pl backend checkstyle:check`
- Backend verification (includes integration tests): `mvn -q -pl backend verify`
- Frontend dev server: `cd frontend && npm run dev`
- Frontend build: `cd frontend && npm run build`
- Frontend lint: `cd frontend && npm run lint`
- Frontend tests (single run): `cd frontend && npm test -- --run`
- Single frontend test file: `cd frontend && npm test -- --run src/components/__tests__/JournalistForm.test.tsx`
- Local CI-equivalent checks: `mvn -q -pl backend checkstyle:check && mvn -q -pl backend verify && cd frontend && npm run lint && npm test -- --run`

Backend integration tests use Testcontainers with PostgreSQL, so Docker must be available. The root Maven project only includes the `backend` module; the frontend is a separate npm project.

## High-level architecture

- The backend is structured as a hexagonal Spring Boot application:
  - `domain`: core records, query objects, and repository ports with no Spring dependencies
  - `application`: use-case interfaces, application services, commands, exceptions, and explicit application-layer validation
  - `api`: Spring MVC controllers, DTOs, exception handling, and MapStruct API mappers
  - `infrastructure`: Spring bean wiring, JPA adapters, Spring Data repositories, persistence entities, and JPA specifications
- `ApplicationBeansConfig` wires the application layer explicitly. Keep Spring annotations and framework wiring out of `application/*`.
- The frontend is a Vite/React SPA that talks to the backend through `/api/*`. Authentication is session/cookie based: the frontend bootstraps auth state with `fetchAuthBootstrap('/api/v1/auth/me')` to avoid redirecting on first load, while normal authenticated calls use `fetchWithAuth`, which redirects `401` responses to `/login` and stores the pre-login URL in `sessionStorage`.
- Pagination stays Spring-free in the domain layer (`PageResult`) and is converted to the Spring-style JSON shape the frontend expects through `PageResponse.from(...)`.
- Journalist search is a cross-layer flow:
  - `JournalistController` parses repeated `sort` query params manually
  - `JournalistRepositoryAdapter` runs the paged query, refetches activities and themes, restores the page order, then maps to domain records
  - the frontend keeps filters, paging, and sort state in the URL and expects repeated query params plus the backend page response shape to stay stable

## Key conventions

- Validation rules live on application command records such as `CreateJournalistCommand` and `LogInteractionCommand`, not on transport DTOs. Application services call `ApplicationValidator`, and the API converts `ConstraintViolationException` into `ValidationErrorResponse`.
- Request DTOs in `api/dto` are transport-only. Keep Lombok annotations there if present, but do not move business validation back into DTOs.
- Search sorting uses repeated Spring-style params like `sort=lastName,asc&sort=firstName,desc`. The controller reads raw `sort` params to avoid Spring's comma-splitting behavior and restricts sorting to an allowlist.
- Repository adapters may do multi-step loading to preserve domain semantics. For journalist search and lookup, activities and themes are loaded in separate queries and reattached to avoid lazy-loading/N+1 issues while preserving page order.
- `ApiExceptionHandler` is the shared HTTP error contract. Keep validation failures returning `ValidationErrorResponse`, and rely on mapped application exceptions for not-found/conflict cases instead of controller-specific error handling.
- The two frontend fetch helpers are intentionally different: use `fetchAuthBootstrap` only for initial auth detection, and use `fetchWithAuth` for normal authenticated requests so redirect-after-login behavior stays consistent.
- Integration tests inherit from `AbstractIntegrationTest` and boot the full Spring context with Testcontainers. Fast service/controller tests are plain JUnit/Mockito tests and should stay separate from the Spring integration suite.
- For interactive browser automation in this repository, use the committed repo-scoped MCP config with `COPILOT_HOME=$PWD/.copilot copilot`. Start the frontend and backend servers separately before using the Playwright MCP server against the app.
- When a test or build issue appears only inside a git worktree, compare it against the main checkout before assuming the branch code is broken. This repository has shown different behavior between worktrees and the main checkout.

