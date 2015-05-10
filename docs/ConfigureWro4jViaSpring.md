# Introduction
Using Spring to configure wro4j makes it easy to have different debug and cache settings based on the environment in which you run, as it moves the configuration from your web.xml to your Spring application.xml – which allows you to use the !PropertyPlaceholderConfigurer to obtain the desired runtime binding of configuration values.

# Details
A new implementation of ```ro.isdc.wro.http.WroFilter``` (called ```ro.isdc.wro.http.ConfigurableWroFilter```) is available since 1.3.x version. It allows setting all of the properties you may need to configure. 

## Update web.xml
In order to configure it with spring, you have to use *!DelegatingFilterProxy* in the web.xml:
```xml 
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  <init-param>
    <param-name>targetBeanName</param-name>
    <param-value>wroFilter</param-value>
  </init-param>
  <init-param>
    <param-name>targetFilterLifecycle</param-name>
    <param-value>true</param-value>
  </init-param>
</filter>
```

Your target bean name must match that which is configured in your application.xml below.

## Configure applicationContext.xml
Now, you have to configure the bean, using property placeholders so that you can have different values for each environment that you run in:

```xml 
<bean id="wroFilter" class="ro.isdc.wro.http.ConfigurableWroFilter">
  <property name="debug" value="${application.wrofilter.debug}"/>
  <property name="cacheUpdatePeriod" value="${application.wrofilter.cache-update-period}"/>
  <property name="modelUpdatePeriod" value="${application.wrofilter.model-update-period}"/>
</bean>
```

Starting with 1.3.7 release, it is possible to externalize all wro4j configuration properties to a properties file. This is considered a preferred method. Specifying each property one by one is deprecated and will be removed in next major release (1.4.0).
 Now, instead of specifying each property one by one, it is enough to configure the *!ConfigurableWroFilter* this way:

```xml 
<bean id="wroFilter" class="ro.isdc.wro.http.ConfigurableWroFilter">
  <property name="properties" ref="wroProperties"/>
</bean>

<bean id="wroProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean">  
    <property name="location" value="classpath:wro.properties" />  
</bean>  
```

The wro.properties is a resource file which holds all configurations. It can look like this:

```python
#If true, it is DEVELOPMENT mode, by default this value is true
debug=true
# Default is true
gzipEnabled=true
jmxEnabled=true
# MBean name to be used if JMX is enabled
mbeanName=wro
# Default is 0
cacheUpdatePeriod=0
# Default is 0
modelUpdatePeriod=0
# Default is false.
disableCache=false
# Default is UTF-8
encoding=UTF-8
```
If any of these properties are not specified, the default value will be used.