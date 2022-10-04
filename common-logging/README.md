Instructions
------------

Add the following dependency to your application's `pom.xml` file. In this way, all
log information will be recorded following the same formatting pattern. To use with tests only! 

```
<dependencies>
    ...
    <dependency>
        <groupId>io.herd.common</groupId>
        <artifactId>common-logging</artifactId>
        <version>{LATEST AVAILABLE VERSION}</version>
    </dependency>
    ...
</dependencies>
```

In addition to the console (standard output), such information will be recorded in a Log file,
according to the following rule:

 1) `${LOG_FILE}`, system variable that defines the full path (including the name) to the Log file.

 2) If the variable above is not defined, the following rule will be applied: `${LOG_PATH}/app.log`,
    where `${LOG_PATH}` is the system variable that defines where the Log file **app.log** will be stored.

 3) If the variable above is not defined, the following rule will be applied: `${LOG_TEMP}/app.log`,
    where `${LOG_TEMP}` is the system variable that defines the temporary directory where the Log file 
    **app.log** will be stored.

 4) If the variable above is not defined, the following rule will be applied: `${java.io.tmpdir}/app.log`,
    where `${java.io.tmpdir}` is the system variable that defines the temporary directory for Java applications.

 5) If the variable above is not defined, the following rule will be applied: `/tmp/app.log`.