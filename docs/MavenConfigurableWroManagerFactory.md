# Introduction
The ConfigurableWroManagerFactory is a very useful implementation of WroManagerFactory which allows user to easily configure processors to be used during maven execution.

## How to use it 
Update the pom.xml with the following configuration:

```xml
<plugin>
    <groupId>ro.isdc.wro4j</groupId>
    <artifactId>wro4j-maven-plugin</artifactId>
    <version>${wro4j.version}</version>
    <configuration>
      <wroManagerFactory>ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory</wroManagerFactory>
    </configuration>
</plugin>		
```

When ConfigurableWroManagerFactory is used, wro4j will search for a configuration file located at **/WEB-INF/wro.properties** location (same as for runtime solution).

Update wro.properties and add processors of your choice and their order as a CSV:
```
#List of preProcessors
preProcessors=lessCss,coffeeScript,cssImport,semicolonAppender
#List of postProcessors
postProcessors=cssMin,jsMin
`
The list of available processors can be found here: [AvailableProcessors](AvailableProcessors)

The wro.properties also can contains additional configuration options. You can find more about them on [Configuration Options](ConfigurationOptions) page.