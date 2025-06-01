# SpringFox Support

---

It will be necessary to copy the file `springdoc.properties` contained in the directory `/src/main/resources`
of this project, to your application's `/src/main/resources` directory, changing the values
of its properties according to the necessity.
Only copy this properties file, in case there is a change in the properties.

# Enable GraphQL Voyager

---

**GraphQL Voyager** becomes accessible at root `/voyager`, if the `spring.graphql.voyager.enabled` property is `true`.

Available Spring Boot configuration parameters (either `application.yml` or `application.properties`):

```yaml
spring:
  graphql:
    voyager:
      enabled: false
      basePath: /
      mapping: /voyager
      endpoint: /graphql
      cdn:
        enabled: false
        version: latest
      pageTitle: Voyager
      displayOptions:
        skipRelay: true
        skipDeprecated: true
        rootType: Query
        sortByAlphabet: false
        showLeafFields: true
        hideRoot: false
      hideDocs: false
      hideSettings: false
```

## GraphQL Voyager Basic settings

The properties `mapping` and `endpoint` will default to `/voyager` and `/graphql`, respectively.
Note that these values may not be empty.

The properties `enabled` defaults to `false`, and therefor **GraphQL Voyager** will be available if the
dependency is added to a Spring Boot Web Application project.

The property `pageTitle` defaults to `Voyager`.

All other properties default to the same as documented on the
official [GraphQL Voyager Properties](https://github.com/graphql-kit/graphql-voyager#properties)

## GraphQL Voyager CDN

The currently bundled version is `2.1.0`, which is - as of writing this - the latest release
of **GraphQL Voyager**. The CDN option uses `jsDelivr` CDN, if enabled.
By default, it will load the latest available release. Available CDN versions can be found on the project's
[jsDelivr page](https://www.jsdelivr.com/package/npm/graphql-voyager). The CDN option is disabled by default.

## Customizing GraphQL Voyager

Further **GraphQL Voyager** `displayOptions`, `hideDocs` and `hideSettings` customizations can be
configured, as documented in the official
[GraphQL Voyager Properties](https://github.com/graphql-kit/graphql-voyager#properties).

# Supported GraphQL-Java Libraries

---

## Extended scalars

[Extended scalars](https://github.com/graphql-java/graphql-java-extended-scalars) can be enabled
by using the `spring.graphql.extended-scalars` configuration property, e.g.:

```yaml
spring:
  graphql:
    extended-scalars: BigDecimal, Date
```

The available scalars are the following: `BigDecimal`, `BigInteger`, `Byte`, `Char`, `Date`,
`DateTime`, `JSON`, `LocalTime`, `Locale`, `Long`, `NegativeFloat`, `NegativeInt`,
`NonNegativeFloat`, `NonNegativeInt`, `NonPositiveFloat`,`NonPositiveInt`, `Object`, `PositiveFloat`,
`PositiveInt`, `Short`, `Time`, `UUID`, `Url`.

The scalars must also be declared in the GraphQL Schema:

```graphql
scalar BigDecimal
scalar Date
```

## Aliased scalars

This component also
supports [aliased scalars](https://github.com/graphql-java/graphql-java-extended-scalars#alias-scalars).
You can define aliases for any standard or extended scalar, as shown in the example below. Note that
the original extended scalar (`BigDecimal`) will *not* be available. In this case, you need to use
`spring.graphql.extended-scalars` property to declare it.

```yaml
spring:
  graphql:
    aliased-scalars:
      BigDecimal: Number, Decimal
      String: Text
```

The aliased scalars must also be declared in the GraphQL Schema:

```graphql
scalar Number
scalar Decimal
scalar Text
```

**Note**: *Custom scalar beans cannot be aliased this way. If you need to alias them, you have to
manually declare the aliased scalar bean.*
