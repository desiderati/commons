Instruções de Uso
=================

Caso sua aplicação redefina o arquivo `application.properties`, será necessário copiar as propriedades do 
arquivo `application.properties` contido no diretório `/src/main/resources` deste projeto, para dentro 
do arquivo `application.properties` redefinido pela sua aplicação, alterando os valores de suas propriedades 
de acordo com a necessidade.

Suporte à Filas JMS
-------------------

Será necessário copiar o arquivo `jms.properties` contido no diretório `/src/main/resources` deste projeto, 
para dentro do diretório `/src/main/resources` da sua aplicação, alterando os valores de suas propriedades 
de acordo com a necessidade. Somente copiar um arquivo de propriedades, caso haja alguma alteração nas 
propriedades do mesmo.