Instructions
------------

Create a class to start your application and annotate it with **@CustomSpringBootApplication**:

#### Non-Web Application

```java
@CustomSpringBootApplication
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
    }
}
```

#### Web application

```java
@CustomSpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

Internationalization Support
-----------------------------

By default, the files: `i18n/exceptions`, `i18n/http-exceptions`, `i18n/default-validation-messages` and
`i18n/validation-messages` are already loaded within the application context when presented in classpath.
Remember that these files have their respective location suffixes.
E.g.: `i18n/exceptions_en_US.properties` or `i18n/exceptions_pt_BR.properties`.

> **Note**:
> ONLY reconfigure the `i18n/exceptions` and `i18n/validation-messages` files to add support for
> internationalization in your application, as shown below:

#### i18n/exceptions_en_US.properties

```properties
track_not_found_exception=Track [Id:{0}] not found!
```

#### i18n/validation-messages_en_US.properties

```properties
track.trackname.notnull=Trackname can not be null.
track.author.notnull=Author can not be null.
track.duration.notnull=Duration can not be null.
track.duration.minvalue=Duration can not be less than {value} seconds.
```

> **Note**:
> IF YOU DO NOT WISH TO USE THE INTERNATIONALIZATION FILES STANDARD NAMES,
> it will be necessary to copy the file `i18n.properties` contained in the directory `/src/main/resources`
> of this project, to your application's `/src/main/resources` directory, setting the` i18n.files` property
> with the internationalization filenames separated by commas to be loaded within the application context.
> Use this feature only for internationalized properties. For other cases, use the default Spring behavior,
> as shown below:

```java
@Component
@PropertySource("classpath:custom.properties")
@ConfigurationProperties("app")
public class LoginProperties {

    private String prop1;
    private String prop2;

}
```

#### custom.properties

```properties
app.prop1=value1
app.prop2=value2
```

Thymeleaf Templates Support
---------------------------

If a plain text template is used, create a file inside the `/src/main/resources` folder of your application,
with the following name: `templates/text/{filename}.txt`.

If an HTML template is used, create a file inside the `/src/main/resources` folder of your application,
with the following name: `templates/html/{filename}.html`.

In the class responsible for obtaining the content generated by the Thymeleaf Framework, add a dependency
with the `TemplateEngine` class. Use the code below as a reference:

```
...
Context ctx = new Context();
ctx.setVariable("message", message);
//String content = templateEngine.process("text/{filename}", ctx); // Text
//String content = templateEngine.process("html/{filename}", ctx); // HTML
String content = templateEngine.process("[(#{greeting(${message})})]", ctx); // Inline String
...
```

#### templates/text/{filename}.txt

```
[(#{greeting(${message})})]
```

#### templates/html/{filename}.html

```html
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
    <head></head>
    <body>
        <!--/*@thymesVar id="message" type="java.lang.String"*/-->
        <span th:text="#{greeting(${message})}"></span>
    </body>
</html>
```

Finally, if internationalization is used, as in the example above, create inside the folder
`/src/main/resources` of your application, a file with name: `i18n/templates.properties`
containing the internationalized strings. Do not forget to add the suffixes according to the
location, eg.: `i18n/templates_en_US.properties`.

#### i18n/templates.properties

```properties
greeting=Hello! {0}
```
