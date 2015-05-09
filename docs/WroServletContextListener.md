# Introduction 
When using runtime configuration (with filter), it is possible to configure wro4j with a **ServletContextListener**. Besides the alternative configuration, it also has the advantage of adding **WroConfiguration** & **WroManagerFactory** objects as attributes to **ServletContext**. This can be useful when you want to access these objects outside of the filter (example: using a jsp tag or in a servlet).

Starting with release 1.4.6, wro4j provides a listener implementation called: **WroServletContextListener**.

# Details 

## Creating listener 
In order to use a **ServletContextListener** with wro4j add the following to web.xml:

```xml
<listener>
    <listener-class>ro.isdc.wro.http.WroServletContextListener</listener-class>
</listener>
```

The above code declares a listener which does the following:
  * When **ServletContext** is initialized create the **WroConfiguration** and **WroManagerFactory** and add them as attributes to **ServletContext**.
  * When **ServletContext** is destroyed, remove all attributes created during initialization.

It is important to note that adding the same listener twice in web.xml will produce an exception which will prevent your application to start. The reason is to prevent storing the same objects twice as a **ServletContext** attribute.

### Listener Attributes Initialization 
By default !WroConfiguration is initialized by reading the wro.properties file from **/WEB-INF/wro.properties** location. As long as **wro.properties** file contains **managerFactoryClassName** property with a not null value, the provided **WroManagerFactory** will be used, otherwise a default instance will be used (the one containing a predefined set of locators and processors).

It is possible to change the initialization strategy by extending the **WroServletContextListener** and overriding the following methods:

### Change Attributes Initialization Strategy
```java
public class CustomListener extends WroServletContextListener {
  protected WroConfiguration newConfiguration() {
    return //a custom WroConfiguration object
  }
  protected WroManagerFactory newManagerFactory() {
    return //a custom WroManagerFactory object
  }
}
```

Alternatively it is possible to set the configuration and manager factory through setters of listener instance:

```java
//Return a custom WroConfiguration object
WroServletContextListener#setConfiguration(WroConfiguration configuration);
//Return a custom WroManagerFactory object
WroServletContextListener#setManagerFactory(WroManagerFactory managerFactory);
```

### Add WroContextFilter to web.xml
Creation of **WroManager** is tightly coupled to Context. This is the reason, why you need to add an additional filter to web.xml whose responsibility is to initialize and set the Context object to all requests. 

```xml
  <filter>
    <filter-name>WroContextFilter</filter-name>
    <filter-class>ro.isdc.wro.http.WroContextFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>WroContextFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

The **WroContextFilter** will be available in 1.4.7 release. If you need to use it before the release is available, you can [view its source](https://github.com/wro4j/wro4j/blob/1.7.x/wro4j-core/src/main/java/ro/isdc/wro/http/WroContextFilter.java) on github in latest development branch. 

## Accessing Servlet Context Attributes 
The listener responsibility is to add **WroConfiguration** and **WroManagerFactory** objects to **ServletContext**. Accessing these objects is possible using a class named **ServletContextAttributeHelper**. This class hides the details about how these attributes are stored and retrieved. Usage example:
```java
 ServletContextAttributeHelper helper = new ServletContextAttributeHelper(servletContext);
 WroConfiguration config = helper.getWroConfiguration();
 WroManagerFactory managerFactory = helper.getManagerFactory();
```

Another example for accesing WroModel:
```java
 ServletContextAttributeHelper helper = new ServletContextAttributeHelper(servletContext);
 WroManagerFactory managerFactory = helper.getManagerFactory();
 WroModel model = getManagerFactory().create().getModelFactory().create();
```

The above examples shows how you can access the **WroConfiguration** and **WroManagerFactory** programmatically anywhere as long as you have access to **ServletContext** object.

## Multiple Listener Configuration 
By default, it is not possible to have two !WroServletContextListener defined in web.xml. This is a big limitation when you want to have multiple **WroFilter** declarations, each having a separate model & resources or cachingStrategy and locators. 

In order to overcome this limitation the following solution is possible:
  * Each new filter should have an unique identifier. The init parameter called name is used as an identifier. Example:

### Filter Configuration 
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
    <init-param>
        <param-name>name</param-name>
        <param-value>first</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>WebResourceOptimizer</filter-name>
    <url-pattern>/filter1/*</url-pattern>
  </filter-mapping>

  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
    <init-param>
        <param-name>name</param-name>
        <param-value>second</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>WebResourceOptimizer</filter-name>
    <url-pattern>/filter2/*</url-pattern>
  </filter-mapping>
```

The above configuration declares two different filters, each being mapped to a different url. Notice that each filter contains an init-param with an unique value. When no init-param is declared, a it is defaulted to "default".

### ServletContextListener Configuration 
Because servlet-api doesn't allow a kind of init-param for listeners, the only way to declare multiple **WroServletContextListener** is to extend this class and override the **WroServletContextListener.getListenerName()**

This is the only inconvenience, since you have to create a new class for each new listener. Example:

```java
public class FirstListener extends WroServletContextListener {
  public String getListenerName() {
    return "first";
  }
}
public class SecondListener extends WroServletContextListener {
  public String getListenerName() {
    return "second";
  }
}
```

And declare these listeners in web.xml: 

```xml
<listener>
    <listener-class>com.mycompany.listener.FirstListener</listener-class>
</listener>

<listener>
    <listener-class>com.mycompany.listener.SecondListener</listener-class>
</listener>
```

Each listener will be responsible for initializing the **WroConfiguration** and **WroManagerFactory** of the filter having the same unique name used by the listener. Thus, the **FirstListener** will initialize the filter having init-param with name "first", while the **SecondListener** will initialize the second one.

### Accessing Attributes For Specific Listener
Having multiple listeners, means that you have multiple attributes stored in !ServletContext containing wro4j specific attributes. You cannot use the following approach for accessing attributes:

```java
 ServletContextAttributeHelper helper = new ServletContextAttributeHelper(servletContext);
 WroConfiguration config = helper.getWroConfiguration();
 WroManagerFactory managerFactory = helper.getManagerFactory();
```
because it will search for attribute named "default". What about uniquely identified listener names?

It is possible to use **ServletContextAttributeHelper** this way:
```java
 ServletContextAttributeHelper helper = new ServletContextAttributeHelper(servletContext, "first");
 WroConfiguration config = helper.getWroConfiguration();
 WroManagerFactory managerFactory = helper.getManagerFactory();
```
It contains a constructor with two arguments, second being the name of your choice.

Alternatively, you can avoid hard-coding the name if you have access to filterConfig object. Example:
```java
ServletContextAttributeHelper helper = ServletContextAttributeHelper.create(filterConfig);
WroConfiguration config = helper.getWroConfiguration();
WroManagerFactory managerFactory = helper.getManagerFactory();
```
In the above case, the name will be extracted for you from init-param.

## Note 
When using **WroServletContextListener** and you want to use a custom **WroConfiguration** or **WroManagerFactory**, you'll have to extend the **WroServletContextListener** and set those objects on the listener. The reason is that the **WroFilter** will use the object from the listener if one already present and this approach guarantees that the filter and listener works with the same object instances.