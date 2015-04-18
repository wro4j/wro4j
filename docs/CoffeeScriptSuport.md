# Introduction
CoffeeScript is a little language that compiles into !JavaScript. Underneath all of those embarrassing braces and semicolons, !JavaScript has always had a gorgeous object model at its heart. !CoffeeScript is an attempt to expose the good parts of !JavaScript in a simple way.

More details about coffeeScript can be found here: http://jashkenas.github.com/coffee-script/

# Details
The coffeeScript support is provided by the !CoffeeScriptProcessor. This processor can be used as both: pre & post processor. Its purpose is to read the coffeeScript content and compile it into javascript. The underlying implementation use Rhino compiler for processing, because the original coffeeScript compiler is implemented in javascript. 
The processor works this way: 
  * it tries to apply the coffeeScript compiler on the processed resource
  * if the compiler fails during processing, the result will be unchanged (and a warning will be logged). This will ensure that you can use coffeeScript and javascript resources at the same time, as long as coffeeScript is used as a pre processor. When using it as a post processor will work only if the merged content of resources is a valid coffeeScript. 
  * if the coffeeScript compiler succeed, the compiled javascript code is used as output result.


# Integration
There are several options for integrating coffeeScript into your application with wro4j:

### Option1: Using !ExtensionsConfigurableWroManagerFactory 
This is the simplest option. This solution works when you use wro4j runtime solution (with a filter). All you have to do is to update the web.xml and configure the wro4j filter to use the !ExtensionsConfigurableWroManagerFactory:

```xml 
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>
      ro.isdc.wro.http.WroFilter
    </filter-class>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory</param-value>
    </init-param>
    <init-param>
      <param-name>uriLocators</param-name>
      <param-value>servletContext,classpath,url</param-value>
    </init-param>
    <init-param>
      <param-name>preProcessors</param-name>
      <param-value>coffeeScript</param-value>
    </init-param>
  </filter>
```
This configuration will use !CoffeeScript as a post processor and it will be the only processor to be used by wro4j. You can use !CoffeeScript as a postProcessor as well (just add the postProcessors init-param with coffeeScript as a param value)

### Option 2: Create a custom !WroManagerFactory 
Creating custom !WroManagerFactory can be very handy when you want to use only the processors you are interested in or custom processor implementations.  

```java
public class MyCustomWroManagerFactory
    extends ServletContextAwareWroManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      factory.addPreProcessor(new CoffeScriptProcessor());      
    }
  }
```

This implementation creates a processors factory which uses only coffeeScript as a pre processor. You can add as well other processors you might need.

Don't forget to update the web.xml and to inform wroFilter to use your new !WroManagerFactory:

```xml
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>com.mycompany.MyCustomWroManagerFactory</param-value>
    </init-param>
```

### Option 3: Integration in maven build
Integrating coffeeScript into maven build is similar to Option 2. 

Create a custom implementation of DefaultStandaloneContextAwareManagerFactory:

```java
public class MyCustomWroManagerFactory
    extends DefaultStandaloneContextAwareManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      factory.addPreProcessor(new CoffeScriptProcessor());      
    }
  }
```

This implementation is almost the same as the one used by run-time solution. Notice that you extend DefaultStandaloneContextAwareManagerFactory (this one is required by maven plugin).

The last step is to update pom.xml configuration of the wro4j maven plugin:

```xml
<configuration>
    <wroManagerFactory>com.mycompany.MyCustomWroManagerFactory</wroManagerFactory>
<configuration>
```

# Updating CoffeeScriptProcessor

CoffeeScriptProcessor underlying implementation uses [https://github.com/jashkenas/coffee-script coffee-script] implementation. !CoffeeScript code base evolves independently and the release cycles of this project are not the same as the one of the wro4j. If there is a newer version of coffee-script available which is not yet supported by wro4j, you can easily update it by extending !CoffeeScriptProcessor. Example:

```java
public class ExtendedCoffeeScriptProcessor extends CoffeeScriptProcessor {
  protected CoffeeScript newCoffeeScript() {
    return new CoffeeScript() {
      protected InputStream getCoffeeScriptAsStream() {
        return //.. the stream of the different version of coffee-script
      }
    };
  }
}
```

The above code shows how you can create a custom processor which extends the original !CoffeeScriptProcessor and provides a different version of coffee-script code. This way, you can easily integrate a different version of coffee-script into your application.