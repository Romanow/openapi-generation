[![CI](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit)](https://github.com/pre-commit/pre-commit)
[![Servers](https://img.shields.io/docker/pulls/romanowalex/servers?logo=docker)](https://hub.docker.com/r/romanowalex/servers)
[![License](https://img.shields.io/github/license/Romanow/openapi-generation)](https://github.com/Romanow/openapi-generation/blob/main/LICENSE)

# Contract First или страх и ненависть в королевстве CodeGen

GitHub: [romanow/openapi-generation](https://github.com/Romanow/openapi-generation).

[Описание API](openapi/servers.yml).

## Локальный запуск

Используем [docker-compose.yml](docker-compose.yml)

```shell
$ ./gradlew clean build
$ docker compose build
$ docker compose up -d --wait
```
