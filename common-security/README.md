JWT Authentication/Authorization Support
----------------------------------------

It will be necessary to create the file `jwt.properties` inside the `/src/main/resources` directory of your application,
changing the values ​​of their properties as needed, as shown below:

```
jwt.private-key =
jwt.public-key =
```

Generation of public and private keys to use with JWT
-----------------------------------------------------

```
ssh-keygen -t rsa -b 1024 -f jwtRS256.key

openssl rsa -in jwtRS256.key -pubout -outform PEM -out jwtRS256.key.pub
cat jwtRS256.key.pub

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in jwtRS256.key -out jwtRS256.pkcs8.key
cat jwtRS256.pkcs8.key
```

Generating the API access key and identifier
--------------------------------------------

**API Id:** UUID that will be used to identify the client application.
(https://www.uuidgenerator.net/)

**API Secret:** 512-bit random key used to sign requests and verify signature.
(https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx)