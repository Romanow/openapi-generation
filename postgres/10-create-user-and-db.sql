-- file: 10-create-user-and-db.sql
CREATE USER program WITH PASSWORD 'test';

CREATE DATABASE openapi;
GRANT ALL PRIVILEGES ON DATABASE openapi TO program;