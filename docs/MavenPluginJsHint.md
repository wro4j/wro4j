# Introduction
[JsHint](http://jshint.com) is a tool to detect errors and potential problems in JavaScript code and to enforce your team's coding conventions. It is very flexible so you can easily adjust it to your particular coding guidelines and the environment you expect your code to execute in.

Useful links: [Build Time Javascript Code Analysis](http://web-resource-optimization.blogspot.com/2011/03/build-time-javascript-code-analysis.html), [MavenPlugin](MavenPlugin)

# Details 
Starting with version 1.3.5 wro4j provides a way to validate javascript as a maven plugin with a new goal called jshint.

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
          <goal>jshint</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <options>devel,evil,noarg</options>
    </configuration>
  </plugin>
</plugins>
```

The options should be provided using one of the following format:

#### Simple Option
```
option1,option2,option3
``` 

Example: 
```
devel,evil,noarg
```


#### Key Value Option
```
key1=value1,key2=value2,key3=value3
``` 

Example: 
```
maxerr=100
```

#### Multiple Value Key Option
```
multiValueKey=['v1','v2','v3']
```
Example:
```
predef=['YUI','window','document','OnlineOpinion','xui']
```