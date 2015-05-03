= Introduction =
[CssLint](http://csslint.net/) is a tool to help point out problems with your CSS code. It does basic syntax checking as well as applying a set of rules to the code that look for problematic patterns or signs of inefficiency. The rules are all pluggable, so you can easily write your own or omit ones you don't want.


Useful links: [CssLint](http://csslint.net/), [MavenPlugin](MavenPlugin)

# Details
Starting with version 1.3.8 wro4j provides a way to validate css using maven plugin with a new goal called csslint.

Configuration example:
```xml
<plugins>
  <plugin>
    <groupid>ro.isdc.wro4j</groupid>
    <artifactid>wro4j-maven-plugin</artifactid>
    <version>${wro4j.version}</version>
    <executions>
      <execution>
        <goals>
          <goal>csslint</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <options>box-model</options>
    </configuration>
  </plugin>
</plugins>
```

## Reporting 
The csslint goal is capable of generating reports. This feature is available since version 1.5.0. The report is located at the following location by default: ```/target/wro4j-reports/csslint.xml```

It is possible to control the location of the generated report file with the following configuration:

```xml
<configuration>
  <reportFile>/path/to/custom/location/report.xml</reportFile>
</configuration>
```

Since release 1.6.2 it is possible to control the format of the generated report using *reportFormat* option:

```xml
<configuration>
  <reportFormat>checkstyle-xml</reportFormat>
</configuration>
```

The following formats are available: lint-xml, checkstyle-xml, csslint-xml, jslint-xml. By default **lint-xml** format is used. 
