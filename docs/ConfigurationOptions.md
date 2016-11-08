---
title: Configurations Options
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: November 8, 2016
summary: ""
---


The default way of setting them is by creating wro.properties file in WEB-INF folder (next to wro.xml).
The way wro4j is configured is customizable, but this is out of the scope of this page.

The wro.properties file is loaded during the filter initialization and is reused for all requests during application lifetime. 

# Table of Available Configuration Options

The following parameters are available:

| Property Name | Default Value | Description |
| --- | --- | --- |
| debug | true | boolean flag (former known as configuration), with possible values: true (DEVELOPMENT) or false (PRODUCTION). Find out more about differences at the bottom of this page |
| minimizeEnabled | true | Flag for turning minimization on/off. |
| gzipResources | true | accepted values are: true or false (case insensitive). When this flag is enabled response will be gziped. |
| resourceWatcherUpdatePeriod | 0 | integer value for specifying how often (in seconds) the resource changes should be checked. When this value is 0, the cache is never refreshed. When a resource change is detected, the cached group containing changed resource will be invalidated. This is useful during development, when resources are changed often. (since 1.4.8) |
| resourceWatcherAsync | false | A boolean which enables/disables asynchronous resource watcher. The true value does make sense when resourceWatcherUpdatePeriod is greater than 0. (since 1.7.3) |
| cacheUpdatePeriod | 0 | integer value for specifying how often (in seconds) the cache should be refreshed. When this value is 0, the cache is never refreshed.|
| modelUpdatePeriod | 0 |  integer value for specifying how often (in seconds) the model (wro.xml) should be refreshed. When this value is 0, the model is never refreshed. |
| header | computed by wro4j | allow explicit configuration of headers (for controlling expiration date, etc). The implementation was inspired from [http://juliusdev.blogspot.com/2008/06/tomcat-add-expires-header.html here]. The headers can be defined using this format: ```<HEADER_NAME1>: <VALUE1> | <HEADER_NAME2>: <VALUE2>``` Example: ```Expires: Thu, 15 Apr 2020 20:00:00 GMT | cache-control: public``` |
| _disableCache_ | false  | DEPRECATED and removed since 1.7.6. Used only in DEVELOPMENT mode and allows you to disable the cache, this way any request will force the processing of the model and resources. |
| parallelPreprocessing | false | A flag for enabling parallel execution of pre processors which may improve overall performance, especially when there are slow preProcessors |
| connectionTimeout | 2000 | Timeout (milliseconds) of the url connection for external resources. This is used to ensure that locator doesn't spend too much time on slow end-point.(since 1.4.5) |
| _managerFactoryClassName_ | N/A | Fully qualified class name of the {@link WroManagerFactory} implementation. When this value is not specified a default instance is used (BaseWroManagerFactory). |
| encoding | UTF-8 | Encoding to use when reading and writing bytes from/to stream |
| ignoreMissingResources | true | When this flag is disabled (false), any missing resource will cause an exception. This is useful to easy identify invalid resources. |
| ignoreEmptyGroup | true | When a group is empty and this flag is false, the processing will fail. This is useful for runtime solution to allow filter chaining when there is nothing to process for a given request. (since 1.4.5) |
| ignoreFailingProcessor | false | Available since 1.4.7. When this flag is true, any failure during processing will leave the content unchanged. |
| cacheGzippedContent | false | When this flag is enabled, the raw processed content will be gzipped only the first time and all subsequent requests will use the cached gzipped content. Otherwise, the gzip operation will be performed for each request. This flag allow to control the memory vs processing power trade-off. (since 1.4.4) | 
| jmxEnabled | true | a flag used for turning on/off JMX.|
| mbeanName | ```wro4j-<contextPath>``` | The name of MBean object (how it is displayed in JMX console). If _contextPath_ is empty, the name is ```wro4j-ROOT``` |

### Configuration options for ConfigurableWroManagerFactory 

| Property Name | Default Value | Description |
| --- | --- | --- |
| preProcessors | N/A | A comma separated values describing pre processor aliases to be used during processing |
| postProcessors | N/A | A comma separated values describing post processor aliases to be used during processing |
| uriLocators | servletContext,uri,classpath | A comma separated values describing locators aliases to be used during processing |
| namingStrategy | noOp | The alias of the NamingStrategy used to rename bundles (for build time solution only) - available since 1.4.7 |
| hashStrategy | MD5 | The alias of the HashStrategy used to compute ETags & cache keys - available since 1.4.7 |



### What are the differences between DEVELOPMENT & DEPLOYMENT? 
  There are a couple of differences between those two modes:
  
  * In DEVELOPMENT - you can add `?minimize=false` in request url in order to turn off minimization on static resources, while in DEPLOYMENT you can't. 
  * In DEVELOPMENT mode any runtime exception is logged and thrown further, while in DEPLOYMENT it is logged and the response is redirected to 404. 
  * In DEVELOPMENT mode you can reset the in-memory cache. See the [FAQ](FAQ.md#how-to-update-the-cache-at-runtime) for details.
