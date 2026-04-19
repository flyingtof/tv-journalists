# Multi-user management design

## Problem

The application currently authenticates a single in-memory Spring Security user. The next iteration must allow the application to manage multiple internal users, with role-based access and an administrator able to create and manage other accounts.

## Goals

- Replace the single in-memory user with persistent application-managed users.
- Introduce role-based access control with two initial roles: `ADMIN` and `USER`.
- Allow administrators to create users, edit them, disable them, and reset their passwords.
- Keep the existing login flow simple for end users.
- Add an administration screen in the frontend for user management.

## Out of scope

- Invitation links or self-service onboarding.
- Fine-grained permission management per user.
- Permanent user deletion.
- External identity providers.

## Recommended approach

Persist users and roles in the application database, keep Spring Security as the authentication framework, and load users from the database through a custom `UserDetailsService`. Authorization stays role-based with two initial roles, while the frontend exposes an administration UI only to administrators.

This approach removes the current bootstrap limitation without introducing unnecessary permission complexity. It also leaves room to add more roles later without redesigning the authentication model.

## Architecture

### Backend

Add a dedicated user-management slice that follows the current backend layering:

- **Domain**: `ApplicationUser`, `Role`, and repository ports.
- **Application**: use cases and services for listing, creating, updating, disabling, re-enabling, and resetting passwords.
- **Infrastructure**: JPA entities, Spring Data repositories, mappers, and adapters.
- **API**: admin-only controllers and DTOs for user management; authenticated endpoint for current-user details.

Spring Security remains responsible for authentication and authorization. The existing `InMemoryUserDetailsManager` is replaced with a database-backed implementation that resolves the user account, the hashed password, account status, and granted roles.

### Frontend

Keep the existing login page and add:

- a current-user bootstrap call after login to retrieve roles;
- route and navigation protection based on roles;
- an administration page reserved to `ADMIN` users for managing accounts.

The frontend must never be the source of truth for authorization. It only adapts the UI. The backend enforces all access rules.

## Data model

### Tables

#### `application_user`

- `id`
- `username` (unique)
- `password_hash`
- `first_name` (optional)
- `last_name` (optional)
- `enabled`
- `created_at`
- `updated_at`

#### `role`

- `id`
- `code` (unique, seeded with `ADMIN` and `USER`)
- `label`

#### `user_role`

- `user_id`
- `role_id`

This many-to-many structure keeps the implementation simple now while allowing future role expansion.

## Business rules

- A username must be unique.
- A disabled user cannot authenticate.
- Passwords are always stored with BCrypt.
- An administrator assigns the initial password at user creation time.
- Password reset is an explicit admin action that writes a new BCrypt hash immediately.
- The system does not expose or recover an existing password.
- No permanent deletion in this version.
- A bootstrap administrator account must exist from seed data or controlled configuration so the system is administrable from first startup.

## Authorization model

- `USER`: can access the current application features for journalists, media, themes, and interactions.
- `ADMIN`: has all `USER` capabilities and can access user administration endpoints and screens.

Protected routes should follow these rules:

- `/api/v1/users/**` -> `ADMIN`
- `/api/v1/auth/me` -> authenticated user
- existing business APIs -> `USER` or `ADMIN`

## API design

### Admin user management

Add endpoints under `/api/v1/users`:

- `GET /api/v1/users` -> list users
- `POST /api/v1/users` -> create user with initial password and roles
- `PUT /api/v1/users/{id}` -> update profile fields, enabled status, and roles
- `POST /api/v1/users/{id}/password-reset` -> set a new password

Deletion is intentionally omitted.

### Current user endpoint

Add `/api/v1/auth/me` to return:

- user id
- username
- display information
- assigned roles

The frontend uses this endpoint after login to decide whether to show admin navigation and protected screens.

## Frontend experience

### Login

Keep the current login page behavior. No onboarding or invitation flow is added.

### User administration screen

Expose an admin-only screen with:

- a table of existing users;
- a create-user action;
- edit action for account details and roles;
- disable or re-enable action;
- password-reset action.

The UI can use the existing application style and does not need advanced workflow behavior. A straightforward form-based experience is sufficient.

### Access control in the UI

- Hide admin navigation from non-admin users.
- Prevent non-admin users from accessing admin routes in the client router.
- On backend `403`, show an explicit access-denied experience rather than failing silently.

## Error handling

Use explicit responses:

- `400 Bad Request` for validation errors
- `403 Forbidden` for unauthorized admin access
- `404 Not Found` for unknown target users
- `409 Conflict` for duplicate usernames

Frontend forms should keep user input visible when the backend rejects a request and display a clear error message.

## Migration and bootstrap

Add Flyway migrations to:

1. create the new user and role tables;
2. seed `ADMIN` and `USER`;
3. create the first administrator account.

The bootstrap admin can be inserted from controlled initial data for now. The implementation should avoid hardcoding long-term operational secrets in source code; local-development defaults may exist, but they must remain easy to override.

## Testing strategy

### Backend

- Unit tests for user-management application services.
- Integration tests for admin endpoints.
- Security-focused tests proving:
  - `ADMIN` can manage users;
  - `USER` cannot call admin endpoints;
  - disabled users cannot authenticate.

### Frontend

- Component or page tests for the administration screen.
- Tests for role-based navigation visibility.
- Tests for access denial and form error states.

## Notes

This scope is intentionally limited to internal account management with predefined roles. If the project later needs finer access control, the current `role`/`user_role` model can evolve without reworking the authentication foundation.
