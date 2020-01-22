Instruções de Uso
=================
 
Suporte ao Autenticação/Autorização JWT 
---------------------------------------
 
Será necessário criar o arquivo `jwt.properties` dentro do diretório `/src/main/resources` da sua aplicação, 
alterando os valores de suas propriedades de acordo com a necessidade, conforme exemplo abaixo:

##### jwt.properties

```
jwt.private-key=
jwt.public-key=
```

Geração das chaves pública e privada para o JWT
-----------------------------------------------

```
ssh-keygen -t rsa -b 1024 -f jwtRS256.key

openssl rsa -in jwtRS256.key -pubout -outform PEM -out jwtRS256.key.pub
cat jwtRS256.key.pub

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in jwtRS256.key -out jwtRS256.pkcs8.key
cat jwtRS256.pkcs8.key
```

Geração do identificador e da chave de acesso à API
---------------------------------------------------

**API Id:** UUID que será utilizado para identificar a aplicação cliente.
(https://www.uuidgenerator.net/)

**API Secret:** Chave randômica de 512-bit utilizada para assinar as requisões e verificar a assinatura.
(https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx)
