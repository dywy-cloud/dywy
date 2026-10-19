set -euo pipefail

: "${DYWY_DIR:?required}"
: "${DYWY_ENV_FILE:?required}"
PACKAGE_NAME="${PACKAGE_NAME:-dywy-db-backup}"
PACKAGE_API="${FORGEJO_URL:-}/api/packages/${FORGEJO_OWNER:-}/generic/$PACKAGE_NAME"

db_compose() {
  docker compose \
    --project-directory "$DYWY_DIR" \
    --file "$DYWY_DIR/docker-compose.prod.yml" \
    --env-file "$DYWY_ENV_FILE" \
    "$@"
}
