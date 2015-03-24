## Resource Locators ##

Wro4j defines an interface called **UriLocator** which is responsible for accessing different type of low-level resources, located virtually anywhere.
By default, when no explicit locators are specified, wro4j loads all locators available in the classpath registered using spi.

There are a number of UriLocator implementations that come supplied out of the box:
  * **ClasspathUriLocator** - for classpath resources
    * Examples
```
<js>classpath:file.js</js>
<css>classpath:ro/isdc/resources/file.css</css>
```

  * **ServletContextUriLocator** -  interprets relative paths within the web application's root directory. You may also include 'protected' resources located under WEB-INF folder.
    * Examples
```
<css>/static/css/style.css</css>
<css>/WEB-INF/css/style.css</css>
<js>/js/prototype.css</js>
```

  * **UrlUriLocator** - uses java.net.URL class for resource localization
    * Examples
```
<js>http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js</js>
<css>http://www.site.com/static/style.css</css>
<css>file:c:/temp/file.css</css>
<js>file:/var/temp/application.js</js>
```

  * **WebjarUriLocator** - (since 1.7.0) loads [webjars](http://www.webjars.org/) resources using [webjars-locator](https://github.com/webjars/webjars-locator) libarary. This locator is available in wro4j-extensions module.
    * Examples
```
<css>webjar:fontawesome.css</css>
<js>webjar:jquery.js</js>
```


## Configure Locator with ConfigurableWroManagerFactory ##
It is possible to simplify the locators configuration with ConfigurableWroManagerFactory. Just add the uriLocators property to wro.properties:

```
uriLocators=servletContext,uri,classpath
```

Each locator has an associated alias. The above configuration instructs wro4j to use to use 3 locators.

Available aliases are:

| **Alias** | **Description** |
|:----------|:----------------|
| classpath | uses ClasspathUriLocator - for locating resources from classpath |
| uri | uses UrlUriLocator - for resources with absolute url (external or files from disk)|
| servletContext | Uses ServletContextUriLocator for locating resources relative to web application context |
| servletContext.DISPATCHER\_FIRST | Same as **servletContext** (available since 1.4.7) |
| servletContext.SERVLET\_CONTEXT\_FIRST | Similar with **servletContext**, but uses **SERVLET\_CONTEXT\_FIRST** strategy for locating resources (available since 1.4.7) |
| webjar | (since 1.7.0) Uses WebjarUriLocator to locate webjars from the classpath |