# Introduction
By default, !LessCss is not included in maven plugin. In order to 
use it as a build time solution you have a couple of solutions described below. 

## Use ConfigurableWroManagerFactory
The ConfigurableWroManagerFactory is a very useful implementation of !WroManagerFactory which allows user to easily configure processors to be used during maven execution.

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

Update wro.properties and add lessCss processor:
```
#Use less css as preProcessor
preProcessors=lessCss
#Use less css as postProcessor
postProcessors=lessCss
```

You can also add other processors along with the lessCss processor separated by comma. Example:
```
#Use less css as preProcessor
preProcessors=cssImport,lessCss
```

The wro.properties also can contains additional configuration options. You can find more about them on [ConfigurationOptions Configuration Options] page.

## Create custom a wro manager factory class
(extend 
DefaultStandaloneContextAwareManagerFactory, @see 
GoogleStandaloneManagerFactory as example) which add the !LessCss 
processor to the chain of existing processors 
```java
public class LessCssSupportStandaloneManagerFactory extends 
DefaultStandaloneContextAwareManagerFactory { 
    @Override
    protected ProcessorsFactory newProcessorsFactory() {         
      return new SimpleProcessorsFactory().addPostProcessor(new LessCssProcessor());  
    }
} 
```

This custom factory called LessCssSupportStandaloneManagerFactory, uses a processorFactory which configures lessCss as a post processor only. You can also add more processors and decide the order in which they are executed. 

### Update the maven configuration plugin in pom.xml 
```xml 
<plugin> 
  <groupId>ro.isdc.wro4j</groupId> 
  <artifactId>wro4j-maven-plugin</artifactId> 
  <version>${wro4j.version}</version> 
  <executions> 
    <execution> 
      <goals> 
        <goal>run</goal> 
      </goals> 
    </execution> 
  </executions> 
  <configuration> 
    <wroManagerFactory>com.mycompany.LessCssSupportStandaloneManagerFactory</wroManagerFactory> 
  </configuration> 
</plugin> 
``` 
This way you can instruct maven plugin what processors to use during 
the build time for compressing/processing resources. 