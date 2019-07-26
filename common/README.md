Instruções de Uso
=================

Caso sua aplicação redefina o arquivo `application.properties`, será necessário copiar as propriedades do 
arquivo `application.properties` contido no diretório `/src/main/resources` deste projeto, para dentro 
do arquivo `application.properties` redefinido pela sua aplicação, alterando os valores de suas propriedades 
de acordo com a necessidade.

Criar uma classe para inicialização da sua aplicação, fazendo com que a mesma seja anotada 
com **@CustomSpringBootApplication**:

##### Aplicação Non-Web

```
@CustomSpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Para aplicações Non-Web precisamos inicializar a aplicação desta forma.
        // Evitando assim que o Container Web seja carregado desnecessariamente.
        // Por padrão, todas as aplicações possuem dependência com o Javax Servlet.
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
    }
}
```

##### Aplicação Web (com suporte à internacionalização)

```
@CustomSpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

Suporte ao Swagger
------------------

Será necessário copiar o arquivo `swagger.properties` contido no diretório `/src/main/resources` deste projeto, 
para dentro do diretório `/src/main/resources` da sua aplicação, alterando os valores de suas propriedades 
de acordo com a necessidade. Somente copiar um arquivo de propriedades, caso haja alguma alteração nas 
propriedades do mesmo.

Suporte à Internacionalização
-----------------------------

Por padrão, os arquivos: `i18n/exceptions`, `i18n/http-exceptions`, `i18n/default-validation-messages` e 
`i18n/validation-messages` já são carregados dentro do contexto da aplicação quando presentes no _Classpath_. 
Não esquecer que estes arquivos possuem seus respectivos sufixos de localização. 
Ex.: `i18n/exceptions_en_US.properties` ou `i18n/exceptions_pt_BR.properties`.

> **Nota**:
> * Redefinir APENAS os arquivos `i18n/exceptions` e `i18n/validation-messages` a fim de adicionar suporte à 
internacionalização em sua aplicação, conforme exemplo abaixo:

##### i18n/exceptions_en_US.properties

```
track_not_found_exception=Track [Id:{0}] not found!
```

##### i18n/validation-messages_en_US.properties

```
track.trackname.notnull=Trackname can not be null.
track.author.notnull=Author can not be null.
track.duration.notnull=Duration can not be null.
track.duration.minvalue=Duration can not be less than {value} seconds.
```

> **Nota**:
> * CASO NÃO DESEJE UTILIZAR OS NOMES PADRÕES DE ARQUIVOS DE INTERNACIONALIZAÇÃO, será necessário copiar o 
arquivo `i18n.properties` contido no diretório `/src/main/resources` deste projeto, para dentro do 
diretório `/src/main/resources` da sua aplicação, configurando a propriedade `i18n.files` com os nomes 
de arquivos de internacionalização separados por vírgula a serem carregados dentro do contexto da aplicação. 
Usar este recurso apenas para propriedades internacionalizadas. Para os demais casos, usar o recurso 
padrão do Spring, conforme exemplo abaixo:

```
@Component
@PropertySource("classpath:custom.properties")
@ConfigurationProperties(prefix = "app")
public class LoginProperties {

    private String prop1;
    private String prop2;

}
```

##### custom.properties

```
app.prop1=value1
app.prop2=value2
```

Suporte ao uso de Templates Thymeleaf
-------------------------------------

Caso seja usado um _Template_ para texto simples, criar dentro da pasta `/src/main/resources` de sua aplicação, 
um arquivo seguindo o padrão: `templates/text/<nome-do-arquivo>.txt`.

Caso seja usado um _Template_ para HTML, criar dentro da pasta `/src/main/resources` de sua aplicação, 
um arquivo seguindo o padrão: `templates/html/<nome-do-arquivo>.html`.

Na classe responsável por obter o texto convertido utilizando os _Templates Thymeleaf_, adicionar uma
dependência com a classe `TemplateEngine`. E utilizar o código abaixo como referência:

```
...
Context ctx = new Context();
ctx.setVariable("message", message);
//String content = templateEngine.process("text/<nome-do-arquivo>", ctx); // Text
//String content = templateEngine.process("html/<nome-do-arquivo>", ctx); // HTML
String content = templateEngine.process("[(#{greeting(${message})})]", ctx); // Inline String
...
```

##### templates/text/<nome-do-arquivo>.txt

```
[(#{greeting(${message})})]
```

##### templates/html/<nome-do-arquivo>.html

```
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
    <head></head>
    <body>
        <!--/*@thymesVar id="message" type="java.lang.String"*/-->
        <span th:text="#{greeting(${message})}"></span>
    </body>
</html>
```

Por fim, caso seja utilizado internacionalização, conforme exemplo acima, criar dentro da pasta 
`/src/main/resources` de sua aplicação, um arquivo seguindo o padrão: `i18n/templates.properties` 
contendo os textos internacionalizados. Não esquecer de adicionar os sufixos de acordo com a 
localidade, ex: `i18n/templates_en_US.properties`.

##### i18n/templates.properties

```
greeting=Hello! {0}
```