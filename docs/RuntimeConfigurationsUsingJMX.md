# Introduction
Starting with version 1.2.0, wro4j allows changing of some configuration options using JMX. This could be very helpful in case you want reload the cache hold by wro4j while your application is in production.

This page describes how this can be done.

# Details
Once you have configured wro4j filter & started the application, a new MBean is registered (implementing WroConfigurationMBean interface). This allows changing of the following:

## Properties
  * **debug** - change of the 'debug' mode. The 'debug' mode is useful for development, because it updates from time to time the cache holding merge results. Also, it schedules an update of the model. This means that if the wro.xml is changed, the result will be available in a short period of time. When using application in production, it is very unlikely that you will want to have debug property set to true.
  * **gzipEnabled** - enables/disables gzip in the runtime
  * **cacheUpdatePeriod** - how often the scheduling thread will update the cache holding the result of merged resources. That means that if during the development you change resources (css & js) very often, you will want the change to be visible very often. The expected value is an integer, representing in how many seconds a refresh of the cache should be done. For instance, setting cacheUpdatePeriod = 30, means that every 30 seconds the cache holding the merged resources will be flushed and the next request will update it  with the most recent contents.
  * **modelUpdatePeriod** - same as cacheUpdatePeriod, except that it relates to update of the wro.xml file. 

Once changed, any of the above properties will notify the filter & the change will be available immediately after next request.

## Operations  
  * **reloadCache** - force the reload of the cache.
  * **reloadModel** - force the reload of the model.

# Running jconsole
In order to change the properties exposed by JMX, all you have to do is to run *jconsole*,  connect to the port where your server is running and select the wro4j MBean associated with your application.

## Configure custom MBeanServer
If, for some reason, you want wro4j to use other MBeanServer then default one, you can do this easily, by extending WroFilter. Below is the example:

### Create a custom implementation of WroFilter:
```java
public class MyCustomWroFilter
  extends WroFilter {
  @Override
  protected MBeanServer getMBeanServer() {
    return //..my custom MBeanServer
  }
}
```
### Update filter configuration in web.xml:
```xml
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>
    com.mycompany.MyCustomWroFilter
  </filter-class>
</filter>
```
This is enough to make wro4j to use your custom MBeanServer for JMX