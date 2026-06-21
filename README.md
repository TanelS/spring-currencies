# Currencies

A Spring Boot service that fetches currency exchange rate data from an external
API ([CurrencyBeacon](https://currencybeacon.com/)) and persists it into a
relational data model for later querying.

This project is a from-scratch Java/Spring Boot rewrite of an existing
Python/FastAPI/SQLAlchemy ETL pipeline that does the same job. The original is a
separate, independent project; this repo is a deliberate practice exercise in
implementing the same data model and import logic idiomatically in Java/Spring,
not a copy of that codebase.

The domain itself (fetch some currency rates, store them) is intentionally
low-stakes — it's a vehicle, not the point. What this repo is actually meant to
demonstrate is the surrounding Spring Boot machinery done properly: JPA
modeling of a non-trivial relationship (two FKs from one entity to another),
externalized config that stays portable across machines and OSes, and a test
setup that actually exercises that config rather than mocking around it.

## What it does

- **Fetches** currency metadata and exchange rates from the CurrencyBeacon API.
- **Persists** them into Postgres using a small dimensional model: a `Currency`
  dimension table and a `Rate` fact table referencing it twice (target currency
  and base currency).
- **Exposes** the data over a REST API (in progress).

## Tech stack

| | |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.7 |
| Build tool | Gradle |
| Persistence | Spring Data JPA, Postgres (H2 for tests) |
| API docs | springdoc-openapi (Swagger UI) |
| Other | Lombok, Spring Boot Actuator, Docker Compose (local Postgres) |

## Project status

**Done:**
- `Currency` / `Rate` JPA entities, with the dual foreign-key relationship on
  `Rate` (target currency + base currency, both pointing at `Currency`) and a
  composite unique constraint on `(currency_id, base_currency_id, rate_date)`.
- `ApiService` — typed HTTP client for the CurrencyBeacon API
  (`GET /currencies`, `GET /latest`), with JUnit tests verifying real API
  responses.
- Local Postgres via Docker Compose, schema auto-created from the entities
  (`spring.jpa.hibernate.ddl-auto=update`).

**Not yet built:**
- Repository layer.
- Import/deduplication service (loading fetched data into Postgres).
- Public REST API for querying stored rates (a temporary `/currency-testing/*`
  controller exists only for manually exercising `ApiService` during
  development).

## Running locally

1. Copy `.env.example` to `.env` and fill in real values — Postgres credentials
   and a CurrencyBeacon API key (`CURRENCYBEACON_API_KEY`,
   `CURRENCYBEACON_API_ROOT`).
2. Start Postgres: `docker compose up -d`
3. Run the app: `./gradlew bootRun`

The app loads `.env` itself (independent of working directory) and creates the
schema automatically on first run.

## Running tests

```
./gradlew test
```

Tests run against an in-memory H2 database and still load `.env` for the
CurrencyBeacon credentials, since `ApiServiceTest` calls the real API.

## Design notes

A few decisions here weren't obvious, so they're written down rather than left
implicit.

**Why `.env`, and not OS-level environment variables.** Secrets and per-machine
config (Postgres credentials, the CurrencyBeacon API key) are read from a
`.env` file via the `dotenv-java` library, never from real OS/system
environment variables. `.env` is git-ignored; `.env.example` documents the
expected shape with placeholder values. The reasoning: OS environment
variables are set outside the repo, on a specific machine, in a way that
doesn't travel with the code — `.env` keeps configuration colocated with the
project and equally usable on any OS, with no host-machine setup step beyond
copying a file.

**How `.env` loading is made working-directory-independent.**
`Dotenv`'s default file resolution is relative to the JVM's current working
directory, which isn't reliably the project root — it depends on how the app
is launched (IDE run configuration, `gradle bootRun`, a packaged jar run from
an arbitrary directory). `CurrenciesApplication.loadEnvFile()` instead uses
Spring Boot's `ApplicationHome` to find the directory the running code
actually lives in, then walks upward through parent directories until it
finds `build.gradle` (the project root marker), and loads `.env` from there.
Each entry is set via `System.setProperty(...)` — a JVM-internal property
table, not an OS environment variable — which is what lets Spring's
`${VAR}`-style placeholders in `application.properties` resolve correctly.

**Making `.env` loading work under tests.** `loadEnvFile()` was originally
only called from `main()`. `@SpringBootTest` builds the Spring application
context directly, without ever invoking `main()` — so none of the `.env`
values were available, and any `@Value("${...}")`-injected bean failed with
an unresolved placeholder. Fix: `loadEnvFile()` is `public static`, called
explicitly from a `@BeforeAll` method in the test class, before the Spring
context is built.

A second, less obvious issue surfaced once that fix was in place: Spring
caches `@SpringBootTest` application contexts by configuration — including
*failed* builds. The project's auto-generated `CurrenciesApplicationTests`
stub (an empty `contextLoads()` test, present from the initial Spring
Initializr scaffold) had no such `@BeforeAll` fix and failed first, which
poisoned the cache for any other test class sharing the same configuration —
making a correctly-fixed test appear to still fail. Removing that stub (it
asserted nothing and tested nothing) resolved it.

A third gotcha: `src/test/resources/application.properties` doesn't merge
with `src/main/resources/application.properties` — Gradle's test classpath
puts test resources ahead of main resources, so whichever file the classloader
finds first fully shadows the other. The test properties file therefore
redeclares the CurrencyBeacon placeholders too (not just the Postgres
ones it overrides with an in-memory H2 datasource), otherwise those
properties would simply be missing during tests.
