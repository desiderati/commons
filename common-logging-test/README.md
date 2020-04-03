Instructions
------------

Add the following dependency to your application's `pom.xml` file. In this way, all
log information will be recorded following the same formatting pattern. To use with tests only!

```
<dependencies>
    ...
    <dependency>
        <groupId>io.herd.common</groupId>
        <artifactId>common-logging-test</artifactId>
        <version>{LATEST AVAILABLE VERSION}</version>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```