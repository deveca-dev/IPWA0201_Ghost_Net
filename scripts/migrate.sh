#!/bin/sh
set -eu

cd "$(dirname "$0")/.."
if [ "$#" -ne 1 ] || [ ! -f "$1" ]; then
    echo "Aufruf: sh scripts/migrate.sh db/migration/V001__initial_schema.sql" >&2
    exit 1
fi

# Bewusst explizit: Nur die angegebene Migration wird ausgeführt.
docker compose exec -T postgres sh -c \
    'exec psql -X --username="$POSTGRES_USER" --dbname="$POSTGRES_DB" --set=ON_ERROR_STOP=1 --single-transaction --file=-' < "$1"
