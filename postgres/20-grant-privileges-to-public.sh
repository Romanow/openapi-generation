#!/usr/bin/env bash

set -e

psql -U postgres -d openapi -c "GRANT ALL PRIVILEGES ON SCHEMA public TO program;"
