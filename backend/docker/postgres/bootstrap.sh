#!/bin/sh
set -eu
if [ "$POSTGRES_READ_USER" = "$PROJECTION_USER" ]; then
    echo 'O usuário de consulta deve ser diferente do usuário de projeção.' >&2
    exit 1
fi
export PGPASSWORD="$PROJECTION_PASSWORD"
psql -h postgres-read -U "$PROJECTION_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 \
  -v read_user="$POSTGRES_READ_USER" -v read_password="$POSTGRES_READ_PASSWORD" <<'SQL'
SELECT format('CREATE ROLE %I LOGIN', :'read_user') WHERE NOT EXISTS
    (SELECT 1 FROM pg_roles WHERE rolname = :'read_user') \gexec
SELECT format('ALTER ROLE %I WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION PASSWORD %L',
    :'read_user', :'read_password') \gexec
SELECT format('GRANT pg_read_all_data TO %I', :'read_user') \gexec
SQL
