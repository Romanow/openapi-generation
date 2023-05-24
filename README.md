# Contract First или страх и ненависть в королевстве CodeGen

[![Build project](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/openapi-generation/actions/workflows/build.yml)

## Аннотация

Часто для решения одной бизнес задачи требуется коммуникация и совместная разработка нескольких команд. В микросервисной
архитектуре задача упрощается, ведь сервисы имеют разную кодовую базу и взаимодействуют друг с другом по API. А значит
требуется лишь договориться об общем API в начале разработки. Но так ли все просто и как автоматизировать этот процесс
рассмотрим в нашем докладе.

## План доклада

1. Постановка задачи: коммуникация между командами разработки при совместной работе над одной задачей. [5m]
2. Что такое контракт? OpenAPI. [2m]
3. Contract First vs. Code First. Плюсы и минусы подхода. [5m]
4. Генерируем код по контракту на клиенте и сервере, разбираем что получилось. [10m]
5. Рассматриваем что умеет проект [OpenAPI Generator](https://openapi-generator.tech/). [5m]
6. Используем свои шаблоны генерации кода по OpenAPI: [15]
    * структура шаблона;
    * убираем лишние файлы (`.openapi-generator-ignore` и global и additional properties);
    * модифицируем шаблон под себя;
    * отладка шаблона.
7. Вместо выводов: как поддержать баланс между чувством прекрасного и сгенерированным кодом? [5m]

## Доклад

### Коммуникация между командами разработки при совместной работе над одной задачей

В мире микросервисов практически любая задача требует взаимодействия нескольких сервисов, а значит для ее выполнения
требуется параллельно вести разработку в нескольких командах. Для этого нужна договоренность между командами, по
какому API (Application Programming Interface) ону будут взаимодействовать. Ведь если _Сервис А_ вызывает _Сервис B_, то
команда, ответственная за _Сервис A_ не может ждать, пока команда, ответственная за _Сервис B_ возьмет задачу в работу и
что-то напишет. Следовательно, нам надо заранее (до начала разработки) описать некоторый контракт, который будет
реализовываться в _Сервисе B_, а _Сервис А_ может начать его использовать на заглушках (например, с помощью WireMock или
Postman Mock Server).

Как контролировать корректность данных, которые будут в заглушках, мы не будем рассматривать, скажу лишь, что стоит
смотреть в сторону [контрактных тестов](https://github.com/Romanow/scc-contracts)
([Использование Spring Cloud Contract как альтернатива для интеграционных тестов](https://www.youtube.com/watch?v=iavb9QiD60Y)).

### Что такое контракт? OpenAPI

Контракт – это соглашение о том, как будет выглядеть наше API и какие парамеры оно будет принимать.

В рамках доклада будем рассматривать синхронную коммуникацию с помощью REST, следовательно, мы строим наше API на базе
протокола HTTP. Самым распространенным способом описания контракта для REST сервисов
является [OpenAPI](https://spec.openapis.org/oas/latest.html).

Спецификация OpenAPI определяет стандарт независимого от языка описания API, который позволяет людям и машинам понимать
возможности службы без доступа к исходному коду, документации или путем перехвата сетевого трафика. По сути, OpenAPI —
это описание методов API, для которых описываются заголовки, входные и выходные параметры.

### Contract First vs. Code First. Плюсы и минусы подхода

Итак, мы описали и согласовали контракт между командами, теперь можем приступать к реализации.

Но как поддерживать консистентность контракта с тем, что реально реализовано в коде? Можно при каждом релизе сравнивать
код с контрактом, но все равно в каких-то мелочах они могут разойтись, а случае ошибки может быть сложно установить ее
причину. Например: метод ищет сущность по ID (`/api/v1/users/1`), в ответ может вернуться `404 Not Found`, если сущность
не найдена. Но `404 Not Found` может вернуться и в случае, если такого метода больше нет в коде. И для разбора в этой
проблеме потребуется время.

Единственный _надежный_ способ поддерживать в консистентном состоянии два источника правды – это получать одно из
другого. Рассмотрим два подхода:

* Code First – мы сначала пишем код, помечаем методы специальными аннотациями `@Tag`, `@Opearation` и т.п., а по этим
  аннотациям автоматически генерируется документация. В итоге получаем OpenAPI, полностью соответствующий коду. Но в
  этом подходе есть 3 проблемы:
    * в коде не все можно описать (доп. параметры, валидаторы и т.п.), следовательно, мы можем получить невалидный или
      неполный OpenAPI;
    * требуется время для написания кода, следовательно, другие команды вынуждены нас ждать;
    * есть проблемы при генерации методов со сходной сигнатурой (`/api/v1/users` и `/api/v1/users?login={1}`) – они
      сливаются в один метод.
* Contract First – у нас есть согласованный со всеми сторонами контакт, по нему мы генерируем код. Сгенерированный код
  рабочий, но проблема в том, что он выглядит очень плохо и работать с ним зачастую неудобно. Плюс в команде есть Code
  Style, соглашения о названиях классов и т.п., а значит нам хочется, чтобы _весь_ код в проекте удовлетворял этим
  критериям.

### Рассматриваем что умеет проект [OpenAPI Generator](https://openapi-generator.tech/)

Погрузимся глубже в Contract First и посмотрим что можно улучшить. Для генерации кода возьмем самое распространенное
решение – проект [OpenAPI Generator](https://openapi-generator.tech/) и посмотрим что оно умеет:

* генерация клиентского и серверного кода для всем популярных языков и фреймворков (Java, Kotlin, Go, C#, JavaScript
  и [т.п.](https://openapi-generator.tech/docs/generators));
* валидация OpenAPI;
* возможность **кастомизации** шаблонов.

```shell
# установка OpenAPI Generator
$ brew install openapi-generator

```

### Генерируем код по контракту на клиенте и сервере, разбираем что получилось

Рассматривать будем пример OpenAPI [servers.yml](openapi/servers.yml), сгенерируем клиентский код с помощью
openapi-generator:

```shell
$ openapi-generator generate \
    -g kotlin \
    --api-package ru.romanow.openapi.client.rest \
    --model-package ru.romanow.openapi.client.models \
    --additional-properties=dateLibrary=java8,serializationLibrary=jackson,enumPropertyNaming=UPPERCASE \
    -o client/build/generated \
    -i openapi/servers.yml
```

В результате получаем целый проект:

![Generated client](images/Generated%20client%20start.png)

Посмотрим на
файл [client/build/generated/src/main/kotlin/ru/romanow/openapi/client/model/ServerResponse](client/build/generated/src/main/kotlin/ru/romanow/openapi/client/models/ServerResponse.kt):

```kotlin
/**
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */

@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package ru.romanow.openapi.client.models

import ru.romanow.openapi.client.models.StateInfo

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 *
 * @param id
 * @param purpose
 * @param latency
 * @param bandwidth
 * @param state
 */


data class ServerResponse(

    @field:JsonProperty("id")
    val id: kotlin.Int,

    @field:JsonProperty("purpose")
    val purpose: ServerResponse.Purpose,

    @field:JsonProperty("latency")
    val latency: kotlin.Int,

    @field:JsonProperty("bandwidth")
    val bandwidth: kotlin.Int,

    @field:JsonProperty("state")
    val state: StateInfo

) {

    /**
     *
     *
     * Values: FRONTEND,BACKEND,DATABASE
     */
    enum class Purpose(val value: kotlin.String) {
        @JsonProperty(value = "FRONTEND")
        FRONTEND("FRONTEND"),

        @JsonProperty(value = "BACKEND")
        BACKEND("BACKEND"),

        @JsonProperty(value = "DATABASE")
        DATABASE("DATABASE");
    }
}
```

Код получился неплохой, но все равно много лишнего, например аннотация `@JsonProperty` или enum как inner class.

Чтобы не держать огромную команду запуска перенесем основные параметры в [config.yml](openapi/client/config.yml).

#### Лишние файлы

Полученный шаблон кода оформлен как проект, т.е. его можно собрать и использовать как модуль или опубликовать в
репозиторий. Но наша задача сгенерировать код моделей, а все остальное не нужно

В сгенерированном коде мы получаем большое количество лишних файлов:

* `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/`;
* `README.md`, `docs/`
* файлы в
  пакете [/org/openapitools/client/infrastructure/](client/build/generated/src/main/kotlin/org/openapitools/client/infrastructure)

Опишем эти исключения в файле [.openapi-generator-ignore](openapi/client/.openapi-generator-ignore).

```shell
$ openapi-generator generate \
  -g kotlin \
  --config openapi/client/config.yml \
  --ignore-file-override openapi/client/.openapi-generator-ignore \
  -o client/build/generated \
  -i openapi/servers.yml

```

На этот раз мы убрали все лишние файлы:

![Generated client](images/Generated%20client%20with%20ignore%20file.png)

### Используем свои шаблоны генерации кода по OpenAPI

#### Структура шаблона

Мы избавились от лишних файлов, теперь займемся кастомизацией шаблона. В качестве движка шаблонизации
используется [mustache](https://mustache.github.io/).

В OpenAPI generator можно переопределить часть шаблона генерации, при этом остальные файлы оставить без изменений.

Для начала надо выгрузить шаблон:

```shell
$ openapi-generator author template -g kotlin --library jvm-ktor -o openapi/client/templates
```

Заходим в [/openapi/client/templates](/openapi/client/templates) и видим большое количество шаблонов:

![OpenAPI codegen templates](images/OpenAPI%20codegen%20templates.png)

Файлов много, но по сути есть 5 входных типов файлов, а остальные просто являются частью других шаблонов:

* [API](openapi/client/templates/libraries/jvm-ktor/api.mustache) – клиент или сервер;
* [APIDocs](openapi/client/templates/api_doc.mustache) – markdown описание API;
* [Model](openapi/client/templates/model.mustache) – модели;
* [ModelDocs](openapi/client/templates/model_doc.mustache) – markdown описание моделей;
* SupportingFiles – дополнительные файл.

Нам нужна кастомизация моделей, поэтому удаляем лишнее и оставляем только три файла:

* [common model template](openapi/client/templates/model.mustache) – общий шаблон модели;
* [data class](openapi/client/templates/data_class.mustache) – `data class`;
* [enum](openapi/client/templates/enum_class.mustache) – `enum`.

Откатим изменения в [/openapi/client/templates](/openapi/client/templates):

```shell
$ rm -r openapi/client/templates
$ git checkout master openapi/client/templates

```

Для примера рассмотрим шаблон [`data class`](openapi/client/templates/data_class.mustache):

```
/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.5.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
data class {{classname}} (
{{#vars}}
    var {{name}}: {{#isArray}}{{#isList}}{{#uniqueItems}}Set{{/uniqueItems}}{{^uniqueItems}}List{{/uniqueItems}}{{/isList}}{{^isList}}Array{{/isList}}<{{^items.isEnum}}{{^items.isPrimitiveType}}{{/items.isPrimitiveType}}{{{items.dataType}}}{{/items.isEnum}}{{#items.isEnum}}{{{nameInCamelCase}}}{{/items.isEnum}}>{{/isArray}}{{^isEnum}}{{^isArray}}{{{dataType}}}{{/isArray}}{{/isEnum}}{{#isEnum}}{{^isArray}}{{{nameInCamelCase}}}{{/isArray}}{{/isEnum}}? = null{{^-last}},{{/-last}}
{{/vars}}
)
{{#hasEnums}}{{#vars}}{{#isEnum}}
enum class {{{nameInCamelCase}}} {
    {{#allowableValues}}{{#enumVars}}{{&name}}{{^-last}}, {{/-last}}{{/enumVars}}{{/allowableValues}}
}
{{/isEnum}}{{/vars}}{{/hasEnums}}
```

* `{{...}}` – обращение к значению переменной;
* `{{#...}} .... {{\...}}` – итерация в цикле или проверка, что такая переменная существует.

OpenAPI generator разбирает OpenAPI и собираем объект, который передает в шаблонизатор:

```json
{
  "importPath": "ru.romanow.openapi.client.models.CreateServerRequest",
  "model": {
    "name": "CreateServerRequest",
    "classname": "CreateServerRequest",
    "isPrimitiveType": false,
    "vars": [
      {
        "openApiType": "string",
        "dataType": "kotlin.String",
        "name": "purpose",
        "baseType": "kotlin.String",
        "required": true,
        "deprecated": false,
        "isPrimitiveType": true,
        "isContainer": false,
        "isString": true,
        "isNumeric": false,
        "isInteger": false,
        "isShort": false,
        "isLong": false,
        "isUnboundedInteger": false,
        "isNumber": false,
        "isFloat": false,
        "isDouble": false,
        "isDecimal": false,
        "isByteArray": false,
        "isBinary": false,
        "isFile": false,
        "isBoolean": false,
        "isDate": false,
        "isDateTime": false,
        "isUuid": false,
        "isEmail": false,
        "isPassword": false,
        "isNull": false,
        "isVoid": false,
        "isFreeFormObject": false,
        "isAnyType": false,
        "isArray": false,
        "isMap": false,
        "isEnum": false,
        "isInnerEnum": false,
        "isEnumRef": false,
        "isReadOnly": false,
        "isWriteOnly": false,
        "isNullable": false,
        "vars": [],
        "requiredVars": [],
        "hasValidation": false,
        "isInherited": false,
        "nameInCamelCase": "Purpose",
        "nameInSnakeCase": "PURPOSE",
        "datatype": "kotlin.String",
        "hasItems": false,
        "isEnumOrRef": false
      }
    ]
  }
}
```

В [config.yml](openapi/client/config.yml) указываем измененный шаблон и при запуске OpenAPI Generator передаем папку с
шаблонами:

```yaml
files:
  model.mustache:
    templateType: Model
    destinationFilename: .kt
```

```shell
$ openapi-generator generate \
  -g kotlin \
  --config openapi/client/config.yml \
  --ignore-file-override openapi/client/.openapi-generator-ignore \
  --template-dir openapi/client/templates \
  -o client/build/generated \
  -i openapi/servers.yml
```

Если нам не требуется модифицировать шаблон (например
[ApiController](server/build/generated/src/main/kotlin/ru/romanow/openapi/server/web/ApiController.kt)
в [server](/openapi/server/config.yml)), то мы просто его не меняем и он создается на основе базовых шаблонов. Если
требуется убрать сгенерированные файлы, то просто описываем их
в [\.openapi-generator-ignore](openapi/client/.openapi-generator-ignore).

#### Вместо выводов: как поддержать баланс между чувством прекрасного и сгенерированным кодом?

1. Если разработка идет в одной команде, то возможно Code First вам подойдет, т.к. для него не требуется никаких
   особенных настроек.
2. Если все-таки вы работаете с Contract First, то нужно смотреть в сторону codegen: без этого реализация быстро
   разойдется с контрактом.
3. OpenAPI generator позволяет кастомизировать шаблоны, следовательно, вы можете сгенерировать код, удовлетворяющий Code
   Style и вашему чувству прекрасного.

## Пример

```shell
$ docker compose up -d
$ ./gradlew clean build
$ ./gradlew :server:bootRun
$ java -jar client/build/libs/client.jar
```