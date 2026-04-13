# TV Journalists - Relations Management

[![CI](https://github.com/flyingtof/tv-journalists/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/flyingtof/tv-journalists/actions/workflows/ci.yml)

A relations management application for Terre Vivante to manage journalists and influencers, focusing on environmental themes.

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Security** (OAuth2 Resource Server + Spring Authorization Server sidecar)
- **Spring Data JPA** (PostgreSQL)
- **Flyway** (Database Migrations)
- **MapStruct** (Mapping)
- **Testcontainers** (Integration Tests)

### Frontend
- **React 18** (TypeScript)
- **Vite**
- **Tailwind CSS**

## Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 21
- Maven 3.9+
- Node.js 20+

### Database & Security
Start the PostgreSQL database:
```bash
docker-compose up -d
```

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Features
- **Profile Management**: CRUD for journalists and influencers.
- **Media Activities**: Link profiles to specific media outlets with roles and themes.
- **Multi-criteria Search**: Filter journalists by name, media, or environmental themes.
- **Interaction Logs**: Track history of contacts, optionally linked to a specific media context.

## Testing
Run backend tests (requires Docker for Testcontainers):
```bash
cd backend
mvn test
```
