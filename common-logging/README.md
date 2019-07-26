Instruções de Uso
=================

Adicionar a seguinte dependência ao arquivo `pom.xml` da sua aplicação. Desta forma, todo as 
informações de Log serão gravadas seguindo um mesmo padrão de formatação. 

```
<dependencies>
    ...
    <dependency>
        <groupId>br.tech.desiderati.common</groupId>
        <artifactId>common-logging</artifactId>
        <version>{INSERIR ÚLTIMA VERSÃO DISPONÍVEL}</version>
    </dependency>
    ...
</dependencies>
```

Além do console (saída padrão), tais informações serão gravadas em um arquivo de Log, de acordo
com a regra a seguir:

 1) `${LOG_FILE}`, variável de sistema que define o caminho completo (incluindo o nome) para o arquivo de Log.

 2) Caso a variável acima não esteja definida, será utiliza a seguinte regra: `${LOG_PATH}/app.log`, 
    onde `${LOG_PATH}` é a variável de sistema que define onde o arquivo de Log (app.log) será armazenado.

 3) Caso a variável acima não esteja definida, será utiliza a seguinte regra: `${LOG_TEMP}/app.log`, 
    onde `${LOG_TEMP}` é a variável de sistema que define o diretório temporário onde o arquivo de Log (app.log) 
    será armazenado.

 4) Caso a variável acima não esteja definida, será utiliza a seguinte regra: `${java.io.tmpdir}/app.log`, 
    onde `${java.io.tmpdir}` é a variável de sistema que define o diretório temporário para aplicações Java.

 5) Caso a variável acima não esteja definida, será utiliza a seguinte regra: `/tmp/app.log`.