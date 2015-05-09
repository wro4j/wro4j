# Introduction
This page describes how you can use wro4j to develop coffeeScript code which is compiled to js on the fly using runtime solution. This should be as simple as following several simple steps:


## Configure web.xml
Add wro4j filter configuration to your web.xml
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

## Create Model
Describe the resources you want to be processed by wro4j. This can be done in multiple ways. The simplest is to create a wro.xml file under WEB-INF folder. Read more about [wro.xml configuration](http://code.google.com/p/wro4j/wiki/WroFileFormat). If you prefer a different DSL, you can try to configure the model [using groovy syntax](http://code.google.com/p/wro4j/wiki/GroovyWroModel). Here is an example of the model using groovy syntax:
```groovy
groups {
  groupName {
    js("/asset/**.coffee")
  }
}
```

## Create configuration file
Create a configuration file. This file is optional and should be located at the same location: under WEB-INF folder:

```
debug=true
disableCache=true
managerFactoryClassName=ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory
preProcessors=coffeeScript
```

This configuration file has a key called preProcessors. This instructs wro4j what pre processors to use. You can provide multiple processors using comma separated values, like: ```cssImport,lessCss,coffeeScript,cssUrlRewriting,semicolonAppender,cssMin```.
Using [coffeeScript](http://code.google.com/p/wro4j/wiki/CoffeeScriptSuport) as a preProcessor, means that each resource described in the model will be processed using this processor before it is merged. If you would like to process the entire merged content, then it is easy to achieve using ```postProcessors``` key in wro.properties configuration file.

## Update html 
The last step is to add to html the link to the resource processed by wro4j:

```xml
<html>
  <head>
    <title>Page Using wro4j</title>
    <script type="text/javascript" src="/wro/groupName.js"></script>
  </head>
  <body>
    <h1>This is simple</h1>
  </body>
</html>
```