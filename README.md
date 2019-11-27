# Changelog

Para maiores informações veja o projeto: [Commons](common/README.md)

Todas as mudanças deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
e este projeto adere a [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

### [Unreleased]

##### Added
- Melhorias na classe **ExceptionHandlingController**: tratamento de novas exceções.
- Adicionada a licença MIT.
- Criação das classes **CustomRepositoryRestConfiguration**.
- Suporte Spring Data Rest.
- Suporte ao Java 11.
- Criação das classes **ThrowingRunnable**.
- Criação da anotação para testes na camada de serviço.
- Implementação do recurso de _Multi Tenancy_.
- Criação das classes **ThrowingConsumer** e **ThrowingFunction**.
- Criação do módulo **common-parent-static**.
- Adição das páginas padronizadas de erro. (**40X.html** e **50X.html**)

##### Changed
- Correção do endereço do Bitbucket.
- Correções na configuração do POM para uso do Docker.
- Melhorias na forma com que tratamos os parâmetros nulos em classes do tipo repositório.
- Melhorias na classe **ExceptionHandlingController**: correções relacionadas com a forma com que tratamos as exceções. Separação entre exceções para _Controllers_ e _Services_.
- Migração para o Spring 5 e SpringBoot 2. Incluindo as dependências.
- Melhorias na configuração das aplicações (Auto Configuration).
- Melhorias no POM.xml.
- Melhorias no componente de notificação. Agora será possível tratar mais de um _Broadcast_ por _Resource_. E vice-versa!

### [1.1.0] - 2018-07-05

##### Added
- Melhorias nas mensagens de _Log_.

##### Changed
- Os _Beans_ do tipo **ModelMapper** ficaram com o escopo de _Prototype_.
- Correção de _Bug_ que ocorria durante o tratamento de exceção do tipo **ApiException**.

### [1.0.0] - 2018-06-27

##### Added
- Criação do Google Calendar Service.
- Criação da classe **MultipartRequest** para facilitar o envio de dados via HTTP.
- Criação da validação para campos do tipo CPF/CNPJ.
- Criação da funcionalidade de tratamento de exceções genéricas. (**ExceptionHandlingController**)
- Criação de repositórios JPA para tratamento de parâmetros de métodos nulos (_Null-Safe Parameters_), incluindo o MongoDB.
- Criação da classe **AbstractRepository** para auxílio à criação de repositório de dados personalizado.
- Criação da classe **AbstractPersistableIdentity** para auxílio à criação de entidades JPA.
- Criação da funcionalidade para definição de estratégia de nomes para uso com banco de dados. (_Naming Strategy_)
- Criação de classes para auxílio à testes automatizados.
- Adicionado suporte à internacionalização.
- Adicionado suporte ao _Framework_ de _Templates_: Thymeleaf.
- Adicionado suporte ao _Framework_ Swagger.
- Adicionado suporte à configuração padronizada de aplicações via Spring Boot.
- Adicionado suporte ao _Framework_ Liquibase.
- Adicionado suporte ao _Framework_ Atmosphere.
- Adicionado suporte à gerenciamento de filas.
- Adicionado suporte à configuração padronizada de arquivos de _Log_.
- Adicionado suporte à leitores QR Code.
- Adicionado suporte à segurança via JWT.