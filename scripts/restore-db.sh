#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"

: "${FORGEJO_URL:?required}"
: "${FORGEJO_OWNER:?required}"
: "${FORGEJO_TOKEN:?required}"
: "${BACKUP_PASSPHRASE:?required}"

target="${1:?usage: restore-db.sh <target-database> [package-version]}"
version="${2:-latest}"

production_db="$(db_compose exec -T database sh -c 'printf %s "$POSTGRES_DB"')"
if [ "$target" = "$production_db" ]; then
  echo "Refusing to restore into the production database '$production_db'" >&2
  exit 1
fi

if [ "$version" = "latest" ]; then
  version="$(curl --fail --silent --show-error \
    --header "Authorization: token $FORGEJO_TOKEN" \
    "$FORGEJO_URL/api/v1/packages/$FORGEJO_OWNER?type=generic&q=$PACKAGE_NAME&limit=100" \
    | jq -r --arg name "$PACKAGE_NAME" '.[] | select(.name == $name) | .version' | sort | tail -n 1)"
  [ -n "$version" ] || { echo "No backup package found" >&2; exit 1; }
fi

workdir="$(mktemp -d)"
trap 'rm -rf "$workdir"' EXIT
file="dywy-$version.dump.enc"

SECONDS=0
curl --fail --silent --show-error \
  --header "Authorization: token $FORGEJO_TOKEN" \
  --output "$workdir/$file" "$PACKAGE_API/$version/$file"
openssl enc -d -aes-256-cbc -pbkdf2 -pass env:BACKUP_PASSPHRASE -in "$workdir/$file" -out "$workdir/dywy.dump"

db_compose exec -T database sh -c 'createdb -U "$POSTGRES_USER" "$0"' "$target"
db_compose exec -T database sh -c 'pg_restore -U "$POSTGRES_USER" -d "$0" --no-owner --exit-on-error' "$target" < "$workdir/dywy.dump"

echo "Restored $file into '$target' in ${SECONDS}s"
