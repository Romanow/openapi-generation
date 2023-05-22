# Contract First или страх и ненависть в королевстве CodeGen

[![Build project](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml)

## Аннотация

Часто для решения одной бизнес задачи требуется коммуникация и совместная разработка нескольких команд. В микросервисной
архитектуре задача упрощается, ведь сервисы имеют разную кодовую базу и взаимодействуют друг с другом по API. А значит
требуется лишь договориться об общем API в начале разработки. Но так ли все просто и как автоматизировать этот процесс
рассмотрим в нашем докладе.

## План доклада

1. Постановка задачи: коммуникация между командами разработки при совместной работе над одной задачей. [5m]
2. Как две команды могут параллельно выполнять одну задачу? [3m]
3. Что такое контракт? OpenAPI. [2m]
4. Contract First vs. Code First. Плюсы и минусы подхода. [5m]
5. Генерируем код по контракту на клиенте и сервере, разбираем что получилось. [10m]
6. Рассматриваем что умеет проект [OpenAPI Generator](https://openapi-generator.tech/). [5m]
7. Используем свои шаблоны генерации кода по OpenAPI: [15]
    * структура шаблона;
    * убираем лишние файлы (`.openapi-generator-ignore` и global и additional properties);
    * модифицируем шаблон под себя;
    * отладка шаблона.
8. Вместо выводов: как поддержать баланс между чувством прекрасного и сгенерированным кодом? [5m]

## Доклад

## Пример

```shell
$ openapi-generator list

$ openapi-generator author template -g kotlin --library jvm-ktor -o openapi/client/templates

$ openapi-generator config-help -g kotlin
```