Commons Herd.io
---------------

[![Build Status](https://github.com/desiderati/commons/workflows/Build%20Status/badge.svg)](https://github.com/desiderati/commons/actions?query=workflow%3A%22Build+Status%22)
[![GitHub Stars](https://img.shields.io/github/stars/desiderati/commons.svg?label=GitHub%20Stars)](https://github.com/desiderati/commons/)
[![LICENSE](https://img.shields.io/badge/License-MIT-lightgrey.svg)](https://github.com/desiderati/commons/blob/master/LICENSE) 

For more information, see the projects:
 
* [common](common/README.md)
* [common-google](common-google/README.md)
* [common-jms](common-jms/README.md)
* [common-logging](common-logging/README.md)
* [common-logging-test](common-logging-test/README.md)
* [common-security](common-security/README.md)

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

Changelog
---------

All project changes will be documented in this file.

#### [2.2.10] - 2020-04-08
- Better auto configuration setup. Aiming to execute test slicing technique available on Spring Boot correctly.
- Created the classes **ServiceJpaTest** and **MockitoLoader**.
- Support to DBUnit.

#### [2.2.9] - 2020-04-08
- Fixed a bug with the Hikari and Liquibase.
- Some Undertown tweaks.
- Better Api Base Path sanitization.

#### [2.2.8] - 2020-04-02
- Undertown access log configuration.

#### [2.2.7] - 2020-04-02
- Fully translated to English.

#### [2.2.6] - 2020-04-01
- Fixed a bug related to **ComponentScan** annotation while using it with **WebMvcTest** annotation.

#### [2.2.5] - 2020-04-01
- Fixed a bug related to **ComponentScan** annotation.

#### [2.2.4] - 2020-04-01
- Fixed a problem while generating the file **spring-configuration-metadata.json**. 
  Custom properties weren't being recognized.

#### [2.2.3] - 2020-03-31
- **WebSecurityConfigurerAdapterAutoConfiguration** will only be configured if there's no other bean 
  of the same type inside the application context.

#### [2.2.2] - 2020-03-31
- **ResponseExceptionDTOHttpMessageConverter** must always return an "application/json". 
  It will ensure when the controller throws an exception, even if the response content-type is different 
  than "application/json", the response entity will be configured to return an "application/json" content type. 

#### [2.2.1] - 2020-03-30
- Support to Apollo GraphQL.
- Added a custom **ResponseExceptionDTOHttpMessageConverter**.
- Updated Commons Compress: 1.8 -> 1.19.

#### [2.2.0] - 2020-03-25
- Migration to Java 11.
- Better Spring Boot auto configuration. Removed **EnableAutoConfiguration** annotations. 
- Better rethrowing functionality. See **ThrowingConsumer** for further information.
- Some language translations.
- Minor code review.
- Better _Multi Tenancy_ support.

#### [2.1.18] - 2020-03-23
- Fixed a Bug related to allowing calls to the public API.

#### [2.1.17] - 2020-03-21
- Added support to MapStruct.
- Now it is possible to use a different profile when starting the application to access the Swagger API.

#### [2.1.16] - 2020-03-20
- Fixed a bug related when an exception was thrown before the **ExceptionHandlingFilter**, generating 
  this way a response message not readable by the **ExceptionHandlingController**. Now if the response body 
  could not be deserialized into a **ResponseExceptionDTO**, it will be deserialized using a **Map<String, Object>**.
- Now it is possible to disable the default authentication.

#### [2.1.15] - 2020-03-19
- ~~Fixed a bug related to the authentication. We must set authentication as **true** after calling 
  the authentication services (JWT and Sign Request).~~

#### [2.1.14] - 2020-03-11
- Added support to mail messages.
- Added support to Apache Commons Validator and Apache Commons Compress.
- Added support to symmetric encryption while handling JWT Tokens.
- Fixed a bug related to how the default behavior was managed by the customized **WebSecurityAutoConfiguration**.

#### [2.1.13] - 2020-03-04
- Added support to CORS configuration through **application.properties** file.

#### [2.1.12] - 2020-02-21
- Now there is no need to add the Commons Herd.io version in children projects.

#### [2.1.11] - 2020-02-21
- Added the class **MockSignRequestAuthorizedClient**.
- Fixed some minor bugs regarding the **JwtAuthorizationService** class instantiation.

#### [2.1.10] - 2020-02-20
- Changed the **ExceptionHandlingController** class to add support to **HttpStatusCodeException**.
- Better logger information while printing exceptions handled by the Swagger Client.
- Fixed a Bug while reading the **swagger-client.properties** file. Prefix was not specified.
- Now the class **SignRequestAuthorizationService** is enabled by default, as it can be used to sign requests 
  when used by a Swagger client.
- Now it is possible to add additional properties to the **SignRequestAuthorizedClient**.

#### [2.1.9] - 2020-02-11
- Added the **SecurityExceptionHandlingController** class.
- Added support to authorities and credentials on JWT Authentication/Authorization Component.
- Now it is possible to configure the expiration period while generating the JWT Token.  

#### [2.1.8] - 2020-01-31
- Added the **AwsExtendedThrowableProxyConverter** class, which is responsible for grouping the stack trace 
  in just one message while using the AWS Cloud Watch.

#### [2.1.7] - 2020-01-30
- Fixed some **Sonarlint** issues. Better **Hikari** configuration.
- Better name for property: **commons-herd.io.version**.

#### [2.1.6] - 2020-01-27
- Added support to the use of profiles with **LogBack**.

#### [2.1.5] - 2020-01-27
- Disabled default log configuration. This was an erroneous deploy!

#### [2.1.4] - 2020-01-25
- Better Swagger Thin Server configuration.

#### [2.1.3] - 2020-01-24
- Added empty **application.properties** file.

#### [2.1.2] - 2020-01-22
- Fixed a bug related to refreshing properties and local validation. 

#### [2.1.1] - 2020-01-22
- Added support to default properties.
- Added support to refreshable properties.
- Added dependency with Spring Cloud Starter.
- Minor changes.
- Improved the Swagger client properties configuration. Now there is no need of extending 
  the **SwaggerClientProperties** class.
- Changed the **DefaultPhysicalNamingStrategy** class. Now there is no need of extending it, 
  just to configure the property 'app.database.table-prefix'. 
- Better messages while handling authorization and authentication.

#### [2.1.0] - 2020-01-16
- Added support to Sign Request Authorization.
- Added support to JWT Authorization.

#### [2.0.2] - 2020-01-14
- Now the base path configuration is also applied to Spring Data Rest Repositories.
  Fixed Bug: [The configuration of the default path ("/api") is not working while using Spring Data Rest](https://github.com/desiderati/commons/issues/2)
- New Spring Boot Version 2.2.2.
- Some translation and minor fixes.
- Better Swagger support. Now it will be available by default.
  Fixed Bug: [Swagger auto configuration not working with Spring Data Rest](https://github.com/desiderati/commons/issues/4)
- Support to new exceptions: **ResourceNotFoundException** and **HttpRequestMethodNotSupportedException**.
- Added support to Spring Actuator for all Spring Web applications.

#### [2.0.1] - 2020-01-11
- Now it is possible to configure the **EntityScan** and **JpaRepositotyScan**.

#### [2.0.0] - 2020-01-10
- Better files generation available for Swagger Clients. Now it is possible to download the JSON file 
  directly from Thin Server.
- CRC16 calculation feature.
- Improvements in the class **ExceptionHandlingController**: handling of new exceptions.
- MIT license added.
- Creation of **CustomRepositoryRestConfiguration** class.
- Spring Data Rest support.
- Support for Java 11.
- Creation of **ThrowingRunnable** class.
- Creating the annotation for tests in the service layer.
- Implementation of the _Multi Tenancy_ feature.
- Creation of **ThrowingConsumer**, **ThrowingSupplier** and **ThrowingFunction** classes.
- Creation of **common-parent-static** module.
- Addition of standard error pages. (**40X.html** and **50X.html**)
- Improvements in the way we treat null parameters in **Repository** classes.
- Improvements in the **ExceptionHandlingController** class: corrections related to the way we handle exceptions. 
  Separation between exceptions for controllers and services.
- Migration to Spring 5 and SpringBoot 2. Including dependencies.
- Application configuration improvements (Auto Configuration).
- Improvements in POM.xml.
- Improvements in the notification component. It will now be possible to handle more than one broadcast 
  per resource. And vice versa!

#### [1.1.0] - 2018-07-05
- Improvements to log messages.
- The **ModelMapper** beans configured with the prototype scope.
- Correction of a bug that occurred when handling **ApiException**.

#### [1.0.0] - 2018-06-27
- Creation of Google Calendar Service.
- Creation of **MultipartRequest** class.
- Creation of CPF/CNPJ field validations.
- Creation of the generic exception handling functionality. (**ExceptionHandlingController**)
- Creation of JPA repositories to handle null method parameters (_Null-Safe Parameters_), including MongoDB.
- Creation of **AbstractRepository** class to assist in the creation of personalized data repositories.
- Creation of **AbstractPersistableIdentity** class to assist in the creation of JPA entities.
- Creation of the functionality responsible for defining the name strategy to use with databases. (_Naming Strategy_)
- Creation of classes to use with automated tests.
- Added support for internationalization.
- Added support for templates: Thymeleaf Framework.
- Added support for Swagger.
- Added support for default application configuration via Spring Boot.
- Added support for Liquibase.
- Added support for Atmosphere.
- Added support for queue management.
- Added support for default log configuration.
- Added support for QR Code readers.
- Added security support via JWT.