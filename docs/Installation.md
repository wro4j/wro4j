# Introduction
This page describes how to install wro4j filter to your java web project.

```xml
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>
    ro.isdc.wro.http.WroFilter
  </filter-class>
</filter>
<filter-mapping>
  <filter-name>WebResourceOptimizer</filter-name>
  <url-pattern>/wro/*</url-pattern>
</filter-mapping>
```

This is the minimum configuration. The filter has several init-params. When using he simplest configuration, they are set to default values. 

## wro4j configuration
In order to configure the option, you have the following options: 


### Configure wro4j with a properties file
This configuration option is available starting with 1.3.8 version. Add the *wro.properties* file under *WEB-INF* folder (the location of the properties file and its name can be changed, by extending WroFilter). 

This is an example of *wro.properties* file:
```
cacheUpdatePeriod=0
modelUpdatePeriod=0
debug=true
disableCache=false
gzipResources=true
ignoreMissingResources=false
jmxEnabled=true
managerFactoryClassName=ro.isdc.wro.examples.manager.CustomWroManager
```

If you are using wro.properties file and there is still an init-param providing a different configuration, the later will override the configuration from the properties file. Both type of configuration can coexist. This was designed in order to be backward compatible with earlier versions. 

For a detailed list of available configuration view [ConfigurationOptions](ConfigurationOptions) page.

This is not the only way to configure wro4j. Besides using init-param in web.xml, you can also externalize all configuration properties into a separate properties file. This can greatly simplify the configuration and make it environment specific. For more details, visit this page: [ConfigureWro4jViaSpring](ConfigureWro4jViaSpring)

### Configure wro4j with init-param(s)

An extended version of filter configuration, with all init-params explained is presented below:

```xml
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>
    ro.isdc.wro.http.WroFilter
  </filter-class>
  <init-param>
    <param-name>configuration</param-name>
    <param-value>DEVELOPMENT</param-value>
  </init-param>
  <init-param>
    <param-name>gzipResources</param-name>
    <param-value>FALSE</param-value>
  </init-param>
  <init-param>
    <param-name>cacheUpdatePeriod</param-name>
    <param-value>60</param-value>
  </init-param>
  <init-param>
    <param-name>modelUpdatePeriod</param-name>
    <param-value>600</param-value>
  </init-param>
  <init-param>
    <param-name>jmxEnabled</param-name>
    <param-value>false</param-value>
  </init-param>
  <init-param>
    <param-name>mbeanName</param-name>
    <param-value>MyApplication</param-value>
  </init-param>
  <init-param>  
    <param-name>header</param-name>  
    <param-value>Expires: Thu, 15 Apr 2020 20:00:00 GMT | Last-Modified: Thu, 15 Apr 2010 20:00:00 GMT</param-value>  
  </init-param>
  <init-param>
    <param-name>disableCache</param-name>
    <param-value>true</param-value>
  </init-param>
</filter>
```

Note: You can configure wro4j not only with init-params. It is possible to keep all wro4j related configurations in a property file (since 1.3.7) or in any other location. More details can be found here: [ConfigureWro4jViaSpring](ConfigureWro4jViaSpring)

### Create wro.xml under WEB-INF directory

```xml
<?xml version="1.0" encoding="UTF-8"?>
<groups xmlns="http://www.isdc.ro/wro"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.isdc.ro/wro wro.xsd">

  <group name="g1">
    <js>classpath:1.js</js>
    <css>classpath:1.css</css>
    <group-ref>g2</group-ref>
  </group>

  <group name="g2">
    <js>classpath:2.js</js>
    <group-ref>g3</group-ref>
    <css>classpath:2.css</css>
  </group>

  <group name="g3">
    <css>classpath:3.css</css>
    <js>classpath:3.js</js>
  </group>

</groups>
```

### Include desired groups as js or css resources
```xml
<html>
  <head>
    <title>Web Frameworks Comparison</title>
    <link rel="stylesheet" type="text/css" href="/wro/groupName.css" />
    <script type="text/javascript" src="/wro/groupName.js"></script>
  </head>
  <body>
    //Body
  </body>
</html>
```

## Resource paths

Paths to CSS or !JavaScript resources can be specified in several ways.  wro4j supports resource paths relative to the servlet context, from the classpath, or using a URL (including `file:` URLs.)    For more information, see the ['wro.xml' File Format specification](WroFileFormat).