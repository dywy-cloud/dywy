# Database backup and restore

Nightly logical dump of the production PostgreSQL (`pg_dump -Fc`), encrypted (AES-256, `openssl`) and stored as a
version of the **generic package `dywy-db-backup`** in the Forgejo package registry. A Forgejo Actions cron
(`.forgejo/workflows/backup.yml`, 02:30 UTC, also runnable by hand with `workflow_dispatch`) runs on the Forgejo runner
installed on the production VM (the one that also runs the deploy workflow, with the Docker socket mounted). It runs
`pg_dump` in the `database` container with `docker exec`, encrypts the stream and uploads it. No SSH is involved. Point-in-time recovery (WAL archiving) is out of scope.

| File                                | Role                                                                        |
|-------------------------------------|-----------------------------------------------------------------------------|
| `.forgejo/workflows/backup.yml`     | Cron trigger, `docker exec pg_dump`, encrypt, upload, retention, failure alert|
| `scripts/restore-db.sh`             | Download a package version, decrypt, `pg_restore` into a target database    |
| `scripts/common.sh`                 | Shared settings and the `docker compose` wrapper                            |

Retention: the 30 newest package versions (`BACKUP_KEEP` in the workflow). Nothing is written to the VM disk outside the job workspace.
Package versions are named `YYYYMMDDTHHMMSSZ`, file `dywy-<version>.dump.enc`.

## One-time setup

1. Runner: it must run on the production VM and mount `/var/run/docker.sock` into job containers (as the deploy
   workflow already needs). The job image (`ubuntu-latest-forge`) needs `bash`, the `docker` CLI, `curl`, `jq` and
   `openssl`. Restrict this runner to trusted repositories: socket access is root on the VM.
2. Restore: copy `scripts/` to `/opt/dywy/scripts` on the VM (compose file at `/opt/dywy/docker-compose.prod.yml`, env
   file at `/opt/dywy/.env`); it needs `curl`, `jq` and `openssl`.
3. Forgejo: create an access token for a dedicated user with the `write:package` scope (it also needs read access to
   list versions); the user must be the owner or an org member allowed to write packages. Never commit it.
4. Forgejo: add the repository secrets:

   | Secret                  | Content                                                                         |
   |-------------------------|---------------------------------------------------------------------------------|
   | `BACKUP_PACKAGE_TOKEN`  | Token from step 3                                                               |
   | `BACKUP_PASSPHRASE`     | Encryption passphrase (`openssl rand -hex 32`); also keep it in a password manager: without it the backups cannot be read |
   | `ALERT_WEBHOOK_URL`     | Receives `{"text": "..."}` on failure (Slack and Mattermost compatible)         |

5. Forgejo: make sure Actions are enabled for the repository and a runner with the label `ubuntu-latest-forge` is registered
   (change `runs-on` otherwise). The package registry must be enabled on the instance.
6. Test it: run the workflow manually (Actions, *Database backup*, *Run workflow*), then check the package appears under
   the owner's *Packages*. Test the alert by temporarily stopping the `database` container and check the webhook
   receives the message.

## Restore

On the VM:

```bash
export DYWY_DIR=/opt/dywy DYWY_ENV_FILE=/opt/dywy/.env
export FORGEJO_URL=https://<forgejo> FORGEJO_OWNER=<owner> FORGEJO_TOKEN=... BACKUP_PASSPHRASE=...
/opt/dywy/scripts/restore-db.sh <target-database> [package-version]
```

The script downloads the version (default: the newest), decrypts it, creates `<target-database>` and runs `pg_restore`
into it. It refuses to target the production database and prints the duration.

To recover production, restore into a scratch database, check the data, then stop the app and swap the databases (or
dump the scratch database back) according to the incident.

## Restore rehearsal (before go-live)

1. Run `restore-db.sh dywy_restore_test`.
2. Check it: `docker compose exec database psql -U <user> -d dywy_restore_test -c '\dt'` and compare a few row counts with
   production.
3. Record the duration printed by the script and the date below, then drop the scratch database
   (`docker compose exec database dropdb -U <user> dywy_restore_test`).

| Date | Package version | Dump size | Duration |
|------|-----------------|-----------|----------|
| TBD  |                 |           |          |


