# Introduction 
By default, wro4j provides 3 locators: ClasspathUriLocator, UrlUriLocator, ServletContextUriLocator. It is very unlikely that you would require a custom locator, because existing one covers almost all use-cases you may have. 


# Implement Custom Locator
But, in case you still need a custom one, just implement UriLocator interface:

```java
public interface UriLocator {
  InputStream locate(final String uri) throws IOException;
  boolean accept(final String uri);
}
```

# Configure Custom Locator 
There are more options to configure custom locator with wro4j. 

## Using Custom managerFactory 
The most low level approach is to create a custom WroManagerFactory which implements the UriLocatorFactory interface. 

```java
public class CustomWroManagerFactory extends BaseWroManagerFactory {
  protected UriLocatorFactory newUriLocatorFactory() {
    return new SimpleUriLocatorFactory().addUriLocator(new ServletContextUriLocator()).addUriLocator(new ClasspathUriLocator()).addUriLocator(
      new UrlUriLocator()).addUriLocator(new CustomUriLocator());
  }
}
```

The last thing to do, is to configure *CustomWroManager* to be used by [wroFilter](Installation) or [maven plugin](MavenPlugin)

## Using ConfigurableWroManagerFactory 
ConfigurableWroManagerFactory allows a simplified configuration with aliases. The idea is that each locator has an associated alias and the locators can be configured in wro.properties (the location and name of this file is configurable) like this:

```xml
uriLocators=servletContext,uri,classpath
```

It is possible to associated new (or even same) aliases with new custom locator implementations. 

### Using LocatorProvider 
This option is available since 1.4.7.
One option to provide custom associations of locator alias and corresponding implementation is to create a class which implements ```ro.isdc.wro.model.resource.locator.support.LocatorProvider``` interface. For instance the default aliases are provided like this:

```java
public class DefaultLocatorProvider
    implements LocatorProvider {
  public Map<String, UriLocator> provideLocators() {
    final Map<String, UriLocator> map = new HashMap<String, UriLocator>();
    map.put(ClasspathUriLocator.ALIAS, new ClasspathUriLocator());
    map.put(ServletContextUriLocator.ALIAS, new ServletContextUriLocator());
    map.put(ServletContextUriLocator.ALIAS_DISPATCHER_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.DISPATCHER_FIRST));
    map.put(ServletContextUriLocator.ALIAS_SERVLET_CONTEXT_FIRST,
        new ServletContextUriLocator().setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_FIRST));
    map.put(UrlUriLocator.ALIAS, new UrlUriLocator());
    return map;
  }
}
```

Next step is to register that provider to make it visible by ConfigurableWroManagerFactory. To register it, add a file at the following location in project classpath:
```
META-INF/services/ro.isdc.wro.model.resource.locator.support.LocatorProvider
```

Update the content of this file with a single line containing the fully qualified name of the your custom provider, example:

```
com.mycompany.locator.CustomLocatorProvider
```

If you have more than one implementation of and want to register all of them, add each class name on the new line:

```
com.mycompany.locator.CustomLocatorProvider1
com.mycompany.locator.CustomLocatorProvider2
```

# Using ConfigurableProvider 
This option is available since 1.4.7.
```ro.isdc.wro.util.provider.ConfigurableProvider``` is an interface which extends ```ro.isdc.wro.model.resource.locator.support.LocatorProvider``` and is useful when you want to provide not only locators but also other components like: processors, namingStrategy or hashStrategy.

The principle is the same, the only change is the provider implementation:

```java
public class MyConfigurableProvider
    extends ConfigurableProviderSupport {
  @Override
  public Map<String, UriLocator> provideLocators() {
    return //provide a map with custom locators
  }
}
```

Notice that the ```DefaultConfigurableProvider``` extends ```ConfigurableProviderSupport``` which provides default implementation for all interfaces the ```ro.isdc.wro.util.provider.ConfigurableProvider``` extends.

The next step is to register that implementation as a service at the following location in the classpath:

```
META-INF/services/ro.isdc.wro.util.provider.ConfigurableProvider
```

with the fully qualified name of the custom provider class:
```
com.mycompany.locator.MyConfigurableProvider
```

If you have more than one implementation of and want to register all of them, add each class name on the new line:

```
com.mycompany.locator.MyConfigurableProvider1
com.mycompany.locator.MyConfigurableProvider2
```