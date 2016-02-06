---
title: Data URI
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: "The base64 data uri encoding support was added since version 1.2.7. 
The data URI scheme is a URI scheme that provides a way to include data in line in web pages as if they were external resources. More details on wikipedia: http://en.wikipedia.org/wiki/Data_URI_scheme"
---

_Important_ - The CssDataUriPreProcessor and its variants should be added as preProcessors and before the CssUrlRewritingProcessor. Otherwise, the processor won't be able to find correctly image resources and will apply no changes at all.


The base64 data uri feature is handled by a pre processor called: CssDataUriPreProcessor. It does make sense to use it as a pre processor, because the url of the image referred by a css should be known at the time it is transformed into a base64 encoded value. The CssDataUriPreProcessor works as following:

  * Parse the content of the css
  * For each background image localize the image resource
  * If the image is not found by the processor, then it is not transformed  
  * If the image used by css is a valid one (can be located by the processor), it is encoded into base64 string
  * The encoding is performed only if the size of the image is less than 32KB. This is performed for supporting IE8 browser. Images larger than that amount are not transformed.
  
Because base64 data uri isn't supported by all browsers (only IE8+, FF, Opera, Chrome and other modern browsers support base64 data uri), this feature isn't used by default. 


# Integration
There are several options to integrate base64 data uri support with wro4j:

## Option1: Using ConfigurableWroManagerFactory
This is the simplest option. This solution works when you use wro4j runtime solution (with a filter). 


wro.properties configuration:
```xml 
managerFactoryClassName=ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory
preProcessors=cssDataUri
```

This configuration will use !CssDataUriPreProcessor and it will be the only processor to be used by wro4j. 

## Option2: Creating a custom WroManagerFactory
Creating custom !WroManagerFactory can be very handy when you want to use only the processors you are interested in or custom processor implementations.  

```java
public class MyCustomWroManagerFactory
    extends ServletContextAwareWroManagerFactory {
    @Override
    protected ProcessorsFactory newProcessorsFactory() {
      final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
      factory.addPreProcessor(new CssDataUriPreProcessor());      
    }
  }
```

This implementation creates a processors factory which uses only cssDataUriPreProcessor. Probably you'll want to add other processors as well (like !CssMin or !JsMin).

Don't forget to update the web.xml and to inform wroFilter to use your new wroManagerFactory:

```xml
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>com.mycompany.MyCustomWroManagerFactory</param-value>
    </init-param>
```