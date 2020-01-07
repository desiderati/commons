Instruções de Uso
=================

Adicionar a seguinte dependência ao arquivo `pom.xml` da sua aplicação. Desta forma, todo as 
informações de Log serão gravadas seguindo um mesmo padrão de formatação. Para uso com testes apenas!

```
<dependencies>
    ...
    <dependency>
        <groupId>io.herd.common</groupId>
        <artifactId>common-logging-test</artifactId>
        <version>{INSERIR ÚLTIMA VERSÃO DISPONÍVEL}</version>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```