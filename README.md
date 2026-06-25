# Currencies

> **Status: work in progress, incomplete.** This is an actively-developed learning/portfolio project, not a finished product. See [Project status](#project-status) below for exactly what's done and what isn't yet.

A Spring Boot service that fetches currency exchange rate data from an external API ([CurrencyBeacon](https://currencybeacon.com/)) and persists it into a relational data model for later querying — a from-scratch Java/Spring Boot rewrite of an existing Python/FastAPI/SQLAlchemy ETL pipeline that does the same job. The original is a separate, independent project; this repo is a deliberate practice exercise in implementing the same data model and import logic idiomatically in Java/Spring, not a copy of that codebase.

**The currencies and rates themselves have no business value.** There's no real use case for importing or querying this particular data — it's included purely as an illustration, a stand-in domain to exercise real Spring Boot machinery against something resembling actual data, rather than a toy `Foo`/`Bar` example. What this repo is actually meant to demonstrate is that machinery done properly: JPA modeling of a non-trivial relationship (two FKs from one entity to another), externalized config that stays portable across machines and OSes, and a test setup that actually exercises that config rather than mocking around it.

## AI use

This README and the Javadoc comments throughout the codebase are AI-generated (Claude). The application code itself — entities, services, repositories, the JSON sanitizer, error handling, everything that actually does the work — was written by me, not by AI. Claude's role was mentoring, not coding: advancing my Spring Boot knowledge, reviewing code I'd already written and pointing out bugs or design issues in it, and answering "why does this happen" questions — but deliberately *not* writing the implementation itself. That was a rule I set on purpose, specifically so I'd actually build real Spring Boot ability through this project rather than end up with working code I didn't understand.

## What it does

- **Fetches** currency metadata and exchange rates from the CurrencyBeacon API.
- **Persists** them into Postgres using a small dimensional model: a `Currency` dimension table and a `Rate` fact table referencing it twice (target currency and base currency).
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
- `Currency` / `Rate` JPA entities, with the dual foreign-key relationship on `Rate` (target currency + base currency, both pointing at `Currency`) and a composite unique constraint on `(currency_id, base_currency_id, rate_date)`.
- `ApiService` — typed HTTP client for the CurrencyBeacon API (`GET /currencies`, `GET /latest`), with JUnit tests verifying real API responses.
- `StringCleaner` — a generic, vendor-agnostic JSON sanitizer (`JsonNode` in, cleaned `JsonNode` out) that recursively walks any object/array shape and cleans every string leaf it finds, with no references to `Currency`/`Rate` or CurrencyBeacon — reusable for any future external API client, not just this one. Wired into both `ApiService` methods, ahead of converting the raw response into its typed DTO. String cleaning rules: HTML-unescape, NFKC Unicode normalization, stripping control characters, carriage returns, zero-width characters, collapsing whitespace runs to a single space, and trimming. Covered by a dedicated unit test suite (`StringCleanerTest`) — no Spring context needed, plain JUnit against the static method.
- Error handling and logging in `ApiService`: an empty response body and a malformed (non-JSON) response body are both handled explicitly (returning `null` rather than throwing an unhandled exception), and both failure paths are logged via SLF4J to `logs/app.log`.
- Local Postgres via Docker Compose, schema auto-created from the entities (`spring.jpa.hibernate.ddl-auto=update`).
- `CurrencyRepository` (plain `JpaRepository<Currency, Long>`, plus a `findByCurrencyCode` lookup) and `CurrencyService` — fetches all currencies from `ApiService`, maps each one onto a `Currency` entity, skips any whose code already exists (idempotent re-runs), and persists the rest. Verified against the live API: 161 currencies imported on first run, 0 imported/161 skipped on a second run. A failure on one currency is logged and skipped rather than aborting the whole import.
- `RateRepository` (a `findByRateDateAndCurrencyAndBaseCurrency` dedup lookup) and `RateService` — given a base currency and its fetched rates, looks up both `Currency` entities by code, skips any rate that's `null` (CurrencyBeacon doesn't always publish a rate for every pair) or already stored for that date, and persists the rest. `CurrencyService.createAllRates()` orchestrates the full run: loops over every stored currency as a base currency, fetches its rates, and delegates to `RateService`. Deliberately kept in a *separate* Spring bean from the orchestrating loop — calling a `@Transactional` method on `this` from inside the same class silently skips Spring's transaction proxy, which would otherwise leave the entire ~161-currency run as a single all-or-nothing transaction (see Design notes).
- A read timeout (50s) on `ApiService`'s `RestClient`, so a hung or rate-limited CurrencyBeacon call fails fast and gets logged, instead of blocking indefinitely with no error at all.

**Not yet built:**
- Whether/how to apply the same sanitizer to this app's own inbound REST request bodies, not just outbound CurrencyBeacon responses — undecided.
- Public REST API for querying stored rates (a temporary `/currency-testing/*` controller exists only for manually exercising `ApiService`/`CurrencyService` during development).

## Running locally

1. Copy `.env.example` to `.env` and fill in real values — Postgres credentials and a CurrencyBeacon API key (`CURRENCYBEACON_API_KEY`, `CURRENCYBEACON_API_ROOT`). Get a free key by signing up at [currencybeacon.com](https://currencybeacon.com/) (their free tier is enough to run this project).
2. Start Postgres: `docker compose up -d`
3. Run the app:
   - **Windows:** `.\gradlew bootRun`
   - **Mac/Linux:** `./gradlew bootRun`

The app loads `.env` itself (independent of working directory) and creates the schema automatically on first run.

## Development

To verify the code compiles without starting the app:

- **Windows:** `.\gradlew compileJava`
- **Mac/Linux:** `./gradlew compileJava`

## Running tests

- **Windows:** `.\gradlew test`
- **Mac/Linux:** `./gradlew test`

Most tests run against an in-memory H2 database. `ApiServiceTest` calls the real CurrencyBeacon API and loads `.env` for credentials. `StringCleanerTest` needs neither — it tests a pure static method with no Spring context or database.

## Design notes

A few decisions here weren't obvious, so they're written down rather than left implicit.

**Package-by-layer, not package-by-feature.** The codebase is structured as `entity/`, `repository/`, `service/`, `controller/`, `dto/`, `exception/` — all entities together, all services together, etc. — rather than grouping everything related to one domain concept (e.g. a `currency/` package containing its entity, repository, service, and controller). Package-by-feature scales better for large codebases with many domains, but package-by-layer is simpler and the more conventional starting point for a focused app like this one.

**Why `.env`, and not OS-level environment variables.** Secrets and per-machine config (Postgres credentials, the CurrencyBeacon API key) are read from a `.env` file via the `dotenv-java` library, never from real OS/system environment variables. `.env` is git-ignored; `.env.example` documents the expected shape with placeholder values. The reasoning: OS environment variables are set outside the repo, on a specific machine, in a way that doesn't travel with the code — `.env` keeps configuration colocated with the project and equally usable on any OS, with no host-machine setup step beyond copying a file.

**How `.env` loading is made working-directory-independent.** `Dotenv`'s default file resolution is relative to the JVM's current working directory, which isn't reliably the project root — it depends on how the app is launched (IDE run configuration, `gradle bootRun`, a packaged jar run from an arbitrary directory). `CurrenciesApplication.loadEnvFile()` instead uses Spring Boot's `ApplicationHome` to find the directory the running code actually lives in, then walks upward through parent directories until it finds `build.gradle` (the project root marker), and loads `.env` from there. Each entry is set via `System.setProperty(...)` — a JVM-internal property table, not an OS environment variable — which is what lets Spring's `${VAR}`-style placeholders in `application.properties` resolve correctly.

**Making `.env` loading work under tests.** `loadEnvFile()` was originally only called from `main()`. `@SpringBootTest` builds the Spring application context directly, without ever invoking `main()` — so none of the `.env` values were available, and any `@Value("${...}")`-injected bean failed with an unresolved placeholder. Fix: `loadEnvFile()` is `public static`, called explicitly from a `@BeforeAll` method in the test class, before the Spring context is built.

A second, less obvious issue surfaced once that fix was in place: Spring caches `@SpringBootTest` application contexts by configuration — including *failed* builds. The project's auto-generated `CurrenciesApplicationTests` stub (an empty `contextLoads()` test, present from the initial Spring Initializr scaffold) had no such `@BeforeAll` fix and failed first, which poisoned the cache for any other test class sharing the same configuration — making a correctly-fixed test appear to still fail. Removing that stub (it asserted nothing and tested nothing) resolved it.

**CurrencyBeacon's `/latest` response is more redundant than it looks.** The raw JSON contains `date`/`base`/`rates` *twice* — once nested under a `response` key, and again flattened at the top level alongside a `meta` block. This briefly looked like a bug in `StringCleaner` (string leaves appeared to be visited twice), but it's genuine API behavior, confirmed by inspecting the raw response directly. It's harmless for the current DTOs — `RatesApiResponse`/`RatesData` only map against the nested `response` object, so the duplicate top-level copies are simply ignored during conversion — but worth knowing about before assuming a tree-walk over this payload is buggy just because the same value shows up more than once.

A third gotcha: `src/test/resources/application.properties` doesn't merge with `src/main/resources/application.properties` — Gradle's test classpath puts test resources ahead of main resources, so whichever file the classloader finds first fully shadows the other. The test properties file therefore redeclares the CurrencyBeacon placeholders too (not just the Postgres ones it overrides with an in-memory H2 datasource), otherwise those properties would simply be missing during tests.

**`@Transactional` on a self-invoked method does nothing — and that turned a real bug into a useful lesson about transaction scope.** `RateService.createRate` was originally a method on `CurrencyService` itself, called as `this.createRate(...)` from inside `createAllRates()`. Spring's transaction handling only intercepts calls that go through its proxy; a call from a class to its own method skips that proxy entirely, so the `@Transactional` on `createRate` was silently doing nothing — the *only* active transaction boundary was `createAllRates()`'s own, which wrapped the entire ~161-currency run as one transaction. In practice this meant nothing committed to Postgres until the whole run finished (confirmed by querying the `rate` table mid-run and finding it empty), and any single failure risked rolling back everything already processed. Fix: moved `createRate` into its own bean, `RateService`, and removed `@Transactional` from `createAllRates()` itself (now just an orchestrating loop). Calling `rateService.createRate(...)` from a different bean correctly goes through Spring's proxy, and — because `createAllRates()` no longer holds a transaction open — each call now opens its own independent transaction, committing as soon as that one base currency's rates are saved. Net effect: ~161 small transactions instead of one giant one, each one's Hibernate session (and the entities it's tracking) released as soon as it commits, rather than the whole run's worth of entities sitting in memory until the very end.
