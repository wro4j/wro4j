# Introduction
This page describes how wro.xml can be interpolated dynamically with some values in the runtime.

# Details 
Sometimes it is useful to create wro.xml dynamically, like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<groups xmlns="http://www.isdc.ro/wro"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.isdc.ro/wro wro.xsd">

  <group name="all">
    <js>/static/${theme}/main.js}</js>
    <css>${externalUrl}/static/main.css</css>
  </group>
</groups>
```

Where ${theme} and ${externalUrl} are placeholders to be replaced at the runtime with some values. 

In order to do this, wro4j exposes a factory method you can override to build the wro.xml model dynamically. Example: 

```java
public class MyWroManagerFactory
  extends ConfigurableWroManagerFactory {
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new FallbackAwareXmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        return //compute the stream dynamically;
      }
    };
  }
}
```

The implementation of **getConfigResourceAsStream()** method can call some sort of business logic responsible for dynamic wro.xml creation, not necessarily located inside the *WEB-INF* folder. An example of implementation could use a velocity template and VelocityEngine class [provided by spring](http://static.springsource.org/spring/docs/2.0.x/reference/mail.html) or any other technologies which you think suits best for building runtime templates.