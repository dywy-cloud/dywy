# Deployment guide

This repository ships with:

- `Dockerfile`: builds frontend + backend and ships a single runtime image.
- `render.yaml`: Render Blueprint with required runtime environment variables (preprod).
- `docker-compose.prod.yml`: the app and PostgreSQL as containers (production stack).

## Environments

| Environment | Hosting                                    | Deployed by                                                         |
|-------------|--------------------------------------------|---------------------------------------------------------------------|
| Preprod     | Render + Aiven PostgreSQL                  | `.github/workflows/deploy-preprod.yml`, after CI succeeds on `main` |
| Production  | Docker Compose (`docker-compose.prod.yml`) | Not automated yet                                                   |

Sections 1 to 6 describe **preprod** (Render + Aiven). The production deployment will be
documented in its own section once its workflow exists. [Health endpoints](#health-endpoints) apply
to both.

## Architecture (preprod)

- Backend (Spring Boot) serves API under `/api/**`.
- Backoffice frontend is bundled into the backend jar and served under `/backoffice`.
- Database is external (Aiven PostgreSQL).

## 1) Create the Render web service from `render.yaml` (preprod)

Render will build with Docker and run a single web service.

### 1.1) Enable deployment from GitHub after merge on `main`

This repository includes `.github/workflows/deploy-preprod.yml` (workflow `Deploy preprod`).

- It triggers only when `CI to build dywy` workflow succeeds on `main`

Add this GitHub Actions secret in repository settings:

- `RENDER_DEPLOY_HOOK_URL`: Render deploy hook URL for your web service.

## 2) Configure runtime environment variables in Render

Set these variables in the Render dashboard (or through Blueprint secrets), never in git:

- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET`
- `APP_AUTH_ADMIN_EMAILS`
- `APP_AUTH_READ_ONLY_EMAILS` (optional; comma-separated emails for the **read-only** backoffice
  tier — see [Backoffice read-only permissions](backoffice-read-only-permissions.md))
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_AUTH_SUCCESS_REDIRECT_URL`
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_SSLMODE` (optional, defaults to `require`; set to `disable` for a PostgreSQL container
  without TLS on the same Docker network)
- `APP_MAIL_FROM`
- `APP_MAIL_PROVIDER` (magic-link email transport; **use `brevo` on Render** — see
  [Email delivery](email-delivery.md))
- `APP_GUEST_ACCESS_BASE_URL`
- `APP_GUEST_AREA_URL`
- `SERVER_FORWARD_HEADERS_STRATEGY` (set to `framework` only when requests always come through a
  trusted proxy that strips/overwrites `Forwarded`/`X-Forwarded-*` headers; otherwise keep default
  `none`)
- `APP_DEEZER_ACCESS_TOKEN` / `APP_DEEZER_PLAYLIST_ID` (Deezer playlist sync — see
  [Deezer integration](deezer-integration.md))

Email-transport variables depend on `APP_MAIL_PROVIDER` — see [Email delivery](email-delivery.md)
for the `brevo` (Render) and `smtp` sets.

## 3) OAuth callback

In Google OAuth app settings, configure the callback URL:

`https://<your-render-domain>/login/oauth2/code/google`

## 4) Access the app

- Backoffice: `https://<your-render-domain>/backoffice`
- API base: same origin, under `/api`

## 5) Load the sample data (preprod only)

A complete demo dataset can be loaded on a **preprod** database (see
[Sample data](local-development.md#sample-data) for its content). Never load it on production.

The database must already be migrated (the app deployed and started once). From the repository
root, with the Aiven connection URL (`sslmode=require`) and the database name as `confirm`:

```bash
psql "postgresql://<user>@<host>:<port>/<db>?sslmode=require" \
  -v confirm=<db> \
  -f backend/src/testFixtures/resources/sample/load-sample-data.sql
```

- **Confirm guard**: the loader refuses to run unless `confirm` equals the name of the connected
  database (`current_database()`), so a command pasted against the wrong database does nothing. It
  prints the expected command instead.
- **Scoped cleanup**: before inserting, the dataset only deletes rows of guests with a
  `@sample.dywy.cloud` email, together with their invitations, RSVPs and magic-link tokens. Other
  data is never touched, and rows are upserted by id, so the command can be re-run.
- **All or nothing**: everything runs in a single transaction.

Pass the password with `PGPASSWORD` or the prompt; never put it in the command or in git.

## 6) Security notes

- Never commit `.env` secrets.
- Keep all credentials in Render secret environment variables.
- Avoid printing sensitive env vars in logs, CI summaries, or scripts.
- For IP-based protections (rate limiting), do not trust forwarded headers unless your
  ingress/proxy sanitizes them.

## Releases and versioning

The release tag is the single source of truth for the application version.

1. Trigger the release in one of two ways (the version is always `X.Y.Z`, for example `1.3.0`):
    - close the milestone named `1.3.0`: the workflow tags the head of `main` with `1.3.0`;
    - or push the tag `1.3.0` manually.
      A milestone whose title is not `X.Y.Z`, or whose tag already exists, does not release anything.
2. `.github/workflows/release.yml` builds the `Dockerfile` at that tag with
   `--build-arg APP_VERSION=1.3.0`, checks that the jar inside the image reports `1.3.0` (the job fails otherwise),
   then publishes `ghcr.io/dywy-cloud/dywy:1.3.0`. There is no moving `latest` tag, and the workflow never calls
   Forgejo or the VM: the production deploy is handled by the Forgejo workflow.
3. The running app reports that version: `GET /app/version` returns `{"version":"1.3.0"}` and the
   backoffice footer displays it. `docker-compose.prod.yml` deploys it with `APP_IMAGE_TAG=1.3.0`.

Other builds never hard-code a version: the [reckon](https://github.com/ajoberstar/reckon) Gradle plugin infers the next
minor snapshot of the latest tag (last release `1.2.3` gives `1.3.0-SNAPSHOT`, or `0.1.0-SNAPSHOT` without any tag).
Reading tags needs the full git history (`fetch-depth: 0` in CI). The Docker build context has no `.git`, so
`APP_VERSION` must be passed explicitly (otherwise the image is `0.1.0-SNAPSHOT`, as on preprod).

### Package visibility

The GHCR package `ghcr.io/dywy-cloud/dywy` is kept **private** (package settings on GitHub, linked to the repository).
The production VM pulls it with a read-only token:

1. Create a token with only the `read:packages` scope (fine-grained tokens cannot read GHCR: use a classic one on a
   dedicated machine account).
2. Store it as a Forgejo secret (for example `GHCR_READ_TOKEN`); never commit it.
3. On the VM, log in before `docker compose pull`: `echo "$GHCR_READ_TOKEN" | docker login ghcr.io -u <user> --password-stdin`.

If the package is made public instead, no token or login is needed.

## Production reverse proxy (HTTPS)

`docker-compose.prod.yml` runs Caddy (`proxy` service, config in `docker/caddy/Caddyfile`) as the only service
publishing ports 80 and 443. It gets and renews Let's Encrypt certificates automatically, redirects HTTP to HTTPS and
proxies to `app:8080` on the internal network. `app` and `db` publish nothing.

- Required variables: `DOMAIN` (`app.dywy.cloud` in production, DNS pointing at the gateway's public IP, which
  forwards TCP 80 and 443 to the VM) and `ACME_EMAIL`.
- Certificates live in the `caddy-data` volume: keep it, or Let's Encrypt rate limits may be hit.
- `SERVER_FORWARD_HEADERS_STRATEGY=framework` is fixed in the compose file because Caddy overwrites `X-Forwarded-*`.
- Remember to set `APP_GUEST_ACCESS_BASE_URL` and `APP_CORS_ALLOWED_ORIGINS` to `https://app.dywy.cloud`,
  `APP_GUEST_AREA_URL` to `https://app.dywy.cloud/guest-access/secured-area` and
  `APP_AUTH_SUCCESS_REDIRECT_URL` to `https://app.dywy.cloud/backoffice`, and the Google OAuth redirect URI to
  `https://app.dywy.cloud/login/oauth2/code/google`.

## Health endpoints

The backend exposes Spring Boot Actuator `health` only, on a dedicated management port (`MANAGEMENT_SERVER_PORT`,
default `8081`) that is never published by Docker:

- `/actuator/health/liveness`: the process is running.
- `/actuator/health/readiness`: the app is ready and the database is reachable (`UP` or `DOWN`, no details).

`docker-compose.prod.yml` uses the readiness endpoint for the `app` healthcheck. Render keeps
`healthCheckPath: /backoffice/index.html` on the main port, because it only probes the public port.
