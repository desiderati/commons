JWT Authentication/Authorization Support
----------------------------------------

It will be necessary to create the file `jwt.properties` inside the `/src/main/resources` directory of your application,
changing the values of their properties as needed, as shown below:

```
jwt.private-key =
jwt.public-key =
```

Generation of public and private keys to use with JWT
-----------------------------------------------------

```
ssh-keygen -t rsa -b 4096 -m PEM -f jwtRS512.key

openssl rsa -in jwtRS512.key -pubout -outform PEM -out jwtRS512.key.pub
cat jwtRS512.key.pub

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in jwtRS512.key -out jwtRS512.pkcs8.key
cat jwtRS512.pkcs8.key
```

Generating the API access key and identifier
--------------------------------------------

**API ID:** UUID that will be used to identify the client application.
(https://www.uuidgenerator.net/)

**API Secret:** 512-bit random key used to sign requests and verify signature.
(https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx)
