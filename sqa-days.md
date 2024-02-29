# OpenAPI на страже вашего спокойствия

## Аннотация

Чтобы end-2-end тесты были максимально полезными, их нужно начинать писать параллельно с разработкой самой задачи. Но
как это сделать прозрачно, ведь API может поменяться как во время разработки, так и в процессе доработок по другим
задачам. Рассмотрим подход Contract First на базе OpenAPI как средство поддержания актуальности e2e тестов.

## План

1. \[**слайды**]\[**3m**] Постановка задачи (что мы решаем?): как быстро начать автоматизацию задачи и следить за изменениями в
   автоматизированном режиме?
2. \[**слайды**]\[**2m**] Что такое контракт? OpenAPI и что он умеет.
3. \[**слайды**]\[**4m**] Берем сервис `servers` с CRUD операциями (на базе [servers.yml](openapi/servers.yml)) с методами :
    * **CREATE**:
        * Добавление нового сервера: `POST /api/v1/servers`;
    * **READ**:
        * Получение сервера по Id: `GET /api/v1/servers/{id}`;
        * Получение списка серверов: `GET /api/v1/servers/`;
    * **UPDATE**:
        * Полное обновление информации о сервере по Id: `PUT /api/v1/servers/{id}`;
        * Частичное обновление информации о сервере по Id: `PATCH /api/v1/servers/{id}`;
    * **DELETE**:
        * `DELETE /api/v1/servers/{id}`.
4. \[**слайды**]\[**2m**] Рассматриваем что умеет проект [OpenAPI Generator](https://openapi-generator.tech/)
5. \[**live coding**]\[**5m**] Берем OpenAPI Generator и по контракту генерируем `RestAssured` клиента.
6. \[**слайды** + **live coding**]\[**5m**] Смотрим что получилось, убираем лишние файлы.
7. \[**слайды** + **live coding**]\[**10m**] Используем свои шаблоны генерации кода по OpenAPI.
    * Структура шаблона.
    * Модифицируем шаблон.
8. А что делать, если реализация еще недоступна:
    * \[**live coding**]\[**5m**] Загружаем OpenAPI в Postman, на базе блока `examples` генерируем Mock Server и делаем вызовы.
      Stubs статические,
      следовательно, мы не можем полноценно выполнить тестирование. Следовательно, нам нужно автоматизировать наши
      сценарии в коде.
    * \[**live coding**]\[**2m**] Генерируем WireMock на базе OpenAPI.
9. \[**слайды**]\[**2m**] Вместо выводов: как максимально быстро реагировать в тестах на изменения в коде?

## Подготовка

Для выполнения мастер-класса нужно:

1. Java 17: `brew install openjdk@17`.
2. OpenAPI generator: `npm install @openapitools/openapi-generator-cli -g`.
3. Postman: `curl -o- "https://dl-cli.pstmn.io/install/osx_arm64.sh" | sh`.

Код проекта: [openapi-generation](https://github.com/Romanow/openapi-generation).

```shell
$ git clone git@github.com:Romanow/openapi-generation.git
$ ./gradlew clean build

```

Сервис `servers` развернут по адресу https://servers.romanow-alex.ru](https://servers.romanow-alex.ru).

Либо можно запустить локально:

```shell
$ docker compose up -d
$ ./gradlew bootRun -p server

```

## Мастер класс

### \[слайды]\[3m] Вступление и постановка задачи

### \[слайды]\[2m] [Что такое контракт? OpenAPI и что он умеет](README.md#что-такое-контракт-openapi)

### \[слайды]\[4m] Описание проекта `Servers`

https://servers.romanow-alex.ru](https://servers.romanow-alex.ru).

### \[слайды]\[2m] [Рассматриваем что умеет проект [OpenAPI Generator](https://openapi-generator.tech/)](README.md#рассматриваем-что-умеет-проект-openapi-generator)

### \[live coding]\[5m] Берем OpenAPI Generator и по контракту генерируем `RestAssured` клиента

```shell
$ openapi-generator-cli generate \
    -g java \
    --api-package ru.romanow.openapi.client.rest \
    --model-package ru.romanow.openapi.client.models \
    --additional-properties=library=rest-assured,dateLibrary=java8,serializationLibrary=jackson,enumPropertyNaming=UPPERCASE \
    -o test/build/generated \
    -i openapi/servers.yml
```

### \[слайды + live coding]\[5m] [Смотрим что получилось, убираем лишние файлы](README.md#лишние-файлы)

### \[слайды + live coding]\[10m] [Используем свои шаблоны генерации кода по OpenAPI](README.md#используем-свои-шаблоны-генерации-кода-по-openapi)

### \[live coding]\[5m] Работаем с OpenAPI средствами Postman

Загружаем в Postman: `APIs` -> `import` (Naming requests: URL, Parameter generation: example, Folder organization:
Tags) -> `Import`. Переименовать коллекцию в _Servers_.

Создаем Mock Server: `Mock servers` -> `Create mock server` -> `Select existing collection` (
_Servers_) -> `Mock Server Name`: `servers` -> `Create Mock Server`.

Создаем новый env с `baseUrl` mock сервера, выполняем запрос.

### \[live coding]\[2m] Генерируем WireMock на базе OpenAPI

```shell
$ openapi-generator-cli generate \
    -g java-wiremock \
    --api-package ru.romanow.openapi.client.rest \
    --model-package ru.romanow.openapi.client.models \
    --additional-properties=dateLibrary=java8,serializationLibrary=jackson,enumPropertyNaming=UPPERCASE \
    -o wiremock/build/generated \
    -i openapi/servers.yml

```

### \[слайды]\[2m] Как максимально быстро реагировать в тестах на изменения в коде