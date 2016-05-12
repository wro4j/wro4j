---
summary: This page describes how to use ConfigurableWroManagerFactory and why it is useful.
labels: Phase-Implementation
---

# Introduction
_ConfigurableWroManagerFactory_ is very useful when you want easily add or remove resource pre/post processors. 

_Related pages_ 
[AvailableProcessors](AvailableProcessors.md) - a list of processors provided by wro4j and their alias.

## Configuring with wro4j-1.4.0 
Starting with version 1.4.0, the configuration become even more easier. 

The web.xml configuration should be as simple as:

```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>WebResourceOptimizer</filter-name>
    <url-pattern>/wro/*</url-pattern>
  </filter-mapping>
```

Create a file named *wro.properties* at the following location */WEB-INF/wro.properties*. 

In order to be able to configure pre & post processors, you have to add the following to *wro.properties*:

```python
managerFactoryClassName=ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory
preProcessors=cssUrlRewriting,cssImport,semicolonAppender,cssMin
postProcessors=cssVariables,jsMin
uriLocators=servletContext,uri,classpath
# Configurable options available since 1.4.7 (not mandatory)
hashStrategy=MD5
namingStrategy=hashEncoder-CRC32
```

The *managerFactoryClassName* property instructs wro4j what implementation of WroManagerFactory should be used. 

The *preProcessors* property contains a list of comma separated values, representing alias of processors to be used. 

The *postProcessors* is the same thing, but applies to post processors.

The *uriLocators* - a comma spearated list of locators to use. When this property is not set, the default locators will be used.

The following properties are configurable since 1.4.7 release:
*hashStrategy* - the value contains the alias of the HashStrategy implementation to use (the one responsible for generating ETag values). When this property is not added, a default value is used.

*namingStrategy* - the value contains the alias of the NamingStrategy implementation (this one is used for build time solution for now). When this property is not added, a default value is used.

## Configuring with earlier versions of wro4j 

### Not required since 1.4.7

When *ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory* is used, you can use all processors existing in *wro4j-core* module.

In order to use also processors defined by *wro4j-extensions* module (like LessCss, CoffeeScript or GoogleClosure), update *managerFactoryClassName* to:

```python
managerFactoryClassName=ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory
```

As you can see, all configuration is moved from web.xml to a property file. The idea was to remove all unnecessary noise from web.xml.

### Not required since 1.4.0

When adding the wro4j filter to web.xml, by default you specify only the filter name and filter class, like this:
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
  </filter>
```
This means that a default *manager* (class responsible for specifying what resource pre/post processource should be used when merging resources) is used, more precisely - *ServletContextAwareWroManagerFactory*.  This *manager* is a default implementation which does most of the job for you: configures most important resource pre/post processors & reads the wro.xml from WEB-INF folder.

But what happens if you want to specify easily what pre/post processors you are interested  in? This is where *ConfigurableWroManagerFactory* is very useful. All you have to do, is to update the web.xml configuration this way:
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory</param-value>
    </init-param>
    <init-param>
      <param-name>uriLocators</param-name>
      <param-value>servletContext,classpath,url</param-value>
    </init-param>
    <init-param>
      <param-name>preProcessors</param-name>
      <param-value>cssImport,bomStripper</param-value>
    </init-param>
    <init-param>
      <param-name>postProcessors</param-name>
      <param-value>cssVariables,cssMinJawr,jsMin</param-value>
    </init-param>
  </filter>
```
The above filter configuration, instructs wro4j that instead using default *manager* class, we want to use another one - *ConfigurableWroManagerFactory*. This *manager*, expects (optional) configuration parameters. These are:
  * uriLocators - what kind of locators should wro4j use to locate the resources found in wro.xml
  * preProcessors - what preProcessors should be applied on resources while merging.
  * postProcessors - what postProcessors should be applied on resources after merge is complete. 
The param-value tag should contain a comma separated values. The values from the above example, are the existing uriLocators, pre/post processors. Thus, for instance, if you are not interested in jsMin (minifies javascript resources) or cssMinJawr (minifies css resoruces)  post processor, you simply remove it from param-value. Same applies for uriLocators or pre processors.


## Extending ConfigurableWroManagerFactory 
The ConfigurableWroManagerFactory is designed for extension and allows easily plug in custom uriLocators or pre/post processors. 

Lets say that you have created a new pre processor (MyPreProcessor). Now, you want to add it the same way as we did with cssImport pre processor. All you have to do, is to extend the ConfigurableWroManagerFactory and contribute with your newly created pre processor like this:
```java
public class MyConfigurableWroManagerFactory
  extends ConfigurableWroManagerFactory {
  @Override
  protected void contributePreProcessors(Map<String, ResourcePreProcessor> map) {
    map.put("myPreProcessor", new MyPreProcessor());
  }
}
```

The same way, you can contribute with post processors and uri locators:
```java
public class MyConfigurableWroManagerFactory
  extends ConfigurableWroManagerFactory {
  @Override
  protected void contributePreProcessors(Map<String, ResourcePreProcessor> map) {
    map.put("myPreProcessor", new MyPreProcessor());
  }
  @Override
  protected void contributePostProcessors(Map<String, ResourcePostProcessor> map) {
    map.put("myPostProcessor", new MyPostProcessor());
  }
  @Override
  protected void contributeLocators(Map<String, UriLocator> map) {
    map.put("myLocator", new MyLocator());
  }
}
```

And finally, update the filter configuration in web.xml:
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>com.mycompany.wro.MyConfigurableWroManagerFactory</param-value>
    </init-param>
    <init-param>
      <param-name>uriLocators</param-name>
      <param-value>myLocator</param-value>
    </init-param>
    <init-param>
      <param-name>preProcessors</param-name>
      <param-value>myPreProcessor</param-value>
    </init-param>
    <init-param>
      <param-name>postProcessors</param-name>
      <param-value>myPostProcessor</param-value>
    </init-param>
  </filter>
```

### Available Locators
| Name | Class | 
|--------|---------|
| servletContext | ServletContextUriLocator |
| classpath | ClasspathUriLocator |
| url | UrlUriLocator |


## ExtensionsConfigurableWroManagerFactory 
The ExtensionsConfigurableWroManagerFactory is an extension of ConfigurableWroManagerFactory located in extensions module, contributing with the following processors:

* yuiCssMin - YUICssCompressorProcessor()
* yuiJsMin - YUIJsCompressorProcessor()
* googleClosureSimple - GoogleClosureCompressorProcessor()
* googleClosureAdvanced - GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS)

In order to use it, all you have to do is:

add wro4j-extensions to classpath, manually or by adding wro4j-extensions dependency to pom.xml:
```xml
<dependency>
    <groupId>ro.isdc.wro4j</groupId>
    <artifactId>wro4j-extensions</artifactId>
    <version>${wro4j.version}</version>
</dependency>
```
where *wro4j.version* - is the latest version of wro4j.

configure the filter this way:

```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
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
      <param-value>cssImport,bomStripper</param-value>
    </init-param>
    <init-param>
      <param-name>postProcessors</param-name>
      <param-value>cssVariables,googleClosureSimple,yuiCssMin</param-value>
    </init-param>
  </filter>
```
