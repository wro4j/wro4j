# Introduction
Here you will find several use-cases and implementation examples. 

## Use a Custom Resource Processor
If you have a use-case, where you need to run a single custom processor (called !MyCustomPostProcessor) on all resources found in the model, you have to follow these steps:

### Create a custom WroManagerFactory
This code applies for the wro4j versions >= 1.3.6 
```java
  public class MyCustomWroManagerFactory
    extends DefaultStandaloneContextAwareManagerFactory{
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      SimpleProcessorsFactory factory = (SimpleProcessorsFactory) super.newProcessorsFactory();
      factory.addPostProcessor(new MyCustomPostProcessor());  
    }
  }
```

Note: If you are using 1.2.x branch, then instead of **DefaultStandaloneContextAwareManagerFactory** use  **DefaultMavenContextAwareManagerFactory**.

As you can see, we just override the method responsible for processors configuration. Here you can add any processors you want (custom or existing ones).

### Update pom.xml configuration
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
		<wroManagerFactory>com.mycompany.MyCustomWroManagerFactory</wroManagerFactory>
	</configuration>
</plugin>
```

Alternatively, you can set the wroManagerFactory when explicitly running plugin:

```
  mvn wro4j:run -DwroManagerFactory=com.mycompany.MyCustomWroManagerFactory
```

There are several existing implementations of manager factories you can use for maven plugin are:
* ro.isdc.wro.extensions.manager.standalone.GoogleStandaloneManagerFactory
* ro.isdc.wro.extensions.manager.standalone.YUIStandaloneManagerFactory

## Example
This example will show how you can easily use google closure with advanced optimization mode (the existing factory GoogleStandaloneManagerFactory uses simple optimization mode).

Based on documentation about the customization, you can easily provide a custom processor in the wro4j maven plugin flow. Integrating google closure with advanced optimization is simple as:

### Create custom wro manager factory
```java
  public class GoogleClosureAdvancedWroManagerFactory
    extends DefaultStandaloneContextAwareManagerFactory{
    @Override
    protected ProcessorsFactory newProcessorsFactory() {         
      SimpleProcessorsFactory factory = (SimpleProcessorsFactory) super.newProcessorsFactory();
      factory.addPostProcessor(new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));  
    }
  }
```

### Update wro4j maven plugin configuration 
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
		<wroManagerFactory>com.mycompany.GoogleClosureAdvancedWroManagerFactory</wroManagerFactory>
	</configuration>
</plugin>
```

Alternatively, you can set the wroManagerFactory when explicitly running plugin:

```
  mvn wro4j:run -DwroManagerFactory=com.mycompany.GoogleClosureAdvancedWroManagerFactory
```