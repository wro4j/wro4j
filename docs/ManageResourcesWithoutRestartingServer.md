# Introduction
One of the things you can do with wro4j, is to manage all your static resources without ever restarting the server. How is this possible? 
This page describes how to configure wro4j in order to be able to do this.

# Details 
Assuming that you want to use the default chain of processors (you have still the freedom to change the processors and their order), the first step would be to extend *ServletContextAwareWroManagerFactory* class:

## Extend ServletContextAwareWroManagerFactory 
```java
package com.mycompany;

public class MyManagerFactory
  extends ServletContextAwareWroManagerFactory {
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new FallbackAwareXmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        return new FileInputStream("D:\\temp\\wro.xml");
      }
    };
  }
}
```

Your custom implementation overrides the _newModelFactory_ method, responsible for creation of the model. The implementation creates an input stream from a file located at the following disk location: d:/temp/wro.xml

That means that instead of being located inside the web application folder (relative to servlet context), the wro.xml is located somewhere on the disk. Actually, its location can be virtually anywhere (absolute url, ftp, db, etc). Eventually, you can create the wro.xml model dynamically using some template with placeholders interpolated in the runtime with values retrieved from some properties file or from somewhere else.

## Configure web.xml
Update the configuration of wro4j in web.xml, by specifying your new _MyManagerFactory_
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>
      ro.isdc.wro.http.WroFilter
    </filter-class>
    <init-param>
      <param-name>configuration</param-name>
      <param-value>DEPLOYMENT</param-value>
    </init-param>
    <init-param>
      <param-name>cacheUpdatePeriod</param-name>
      <param-value>100</param-value>
    </init-param>
    <init-param>
      <param-name>modelUpdatePeriod</param-name>
      <param-value>200</param-value>
    </init-param>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>com.mycompany.MyManagerFactory</param-value>
    </init-param>
  </filter>
```

This configuration will make sure that it will retrieve the model (wro.xml) from the location specified by your MyManagerFactory class.
The *modelUpdatePeriod* init param is configured with 200 - that means that each 200 seconds, the model will be updated with the latest version. That means that you can create new groups, update group contents, add or remove resources from the groups. Alternatively, you can use jmx to force the model update instead of using _modelUpdatePeriod_. For details about how to use jmx, visit this page: [RuntimeConfigurationsUsingJMX](RuntimeConfigurationsUsingJMX)

If accidentally, the model is broken (becomes invalid), the wro4j doesn't crash, but use instead the latest known good version of your model. This is the reason why you are extending *FallbackAwareXmlModelFactory*. 

# Conclusion 
This page describes how wro4j can help you to manage your static resources without ever restarting the server.