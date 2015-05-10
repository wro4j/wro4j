# <img src="http://code.google.com/p/wro4j/logo"> Wro4j Grails Plugin
[![Build Status](https://api.travis-ci.org/wro4j/wro4j-grails-plugin.png)](http://travis-ci.org/wro4j/wro4j-grails-plugin)

# Getting Started

In order to get started with wro4j, you have to follow only 3 simple steps.

## Step 1: Install the wro4j plugin

Install the plugin by adding the following to BuildConfig.groovy:
```groovy
    grails.project.dependency.resolution {
       plugins {
          compile ':wro4j:latest.release'
       }
    }
```

## Step 2: Create grails-app/conf/Wro.groovy

```groovy
    groups{
      all{
        css(minimize: false, "/css/*.css")
        js "/js/*.js"
      }
    }
```    

Wro.groovy can be located anywhere; see the configuration option 'wro.wroPath' below.

## Step 3: Use optimized resources

```html
    <html>
      <head>
        <title>Web Page using wro4j</title>
        <wro:css group="all"/>
        <wro:js group="all"/>
      </head>
      <body>

      </body>
    </html>
```


## Step 4: (Optional) Configure Wro in Config.groovy :

```groovy
    import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor
    import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor
    import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor
    import ro.isdc.wro.model.resource.processor.impl.css.*
    import ro.isdc.wro.model.resource.locator.UrlUriLocator
    import ro.isdc.wro.model.resource.locator.ClasspathUriLocator
    import ro.isdc.wro.model.resource.locator.ServletContextUriLocator

    /**
     * Boolean flag for enable/disable resource gzipping.
     */
    wro.gzipResources = true
    /**
     * integer value for specifying how often (in seconds) the resource changes should be checked. When this value is 0, the cache is never refreshed. When a resource change is detected, the cached group containing changed resource will be invalidated. This is useful during development, when resources are changed often.
     */
    wro.resourceWatcherUpdatePeriod = 0
    /**
     * flag indicating if the minimization is enabled. When this flag is false, the minimization will be
     *         suppressed for all resources.
     */
    wro.minimizeEnabled = true
    /**
     * Parameter allowing resources to be asynchronously processed
     */
    wro.resourceWatcherAsync = false
    /**
     * Parameter allowing to turn jmx on or off.
     */
    wro.jmxEnabled = true
    /**
     * Parameter containing an integer value for specifying how often (in seconds) the cache should be refreshed.
     */
    wro.cacheUpdatePeriod = 0
    /**
     * Parameter containing an integer value for specifying how often (in seconds) the model should be refreshed.
     */
    wro.modelUpdatePeriod = 0
    /**
     * Disable cache configuration option. When true, the processed content won't be cached in DEVELOPMENT mode. In
     * DEPLOYMENT mode changing this flag will have no effect.
     */
    wro.disableCache = false
    /**
     * Instructs wro4j to not throw an exception when a resource is missing.
     */
    wro.ignoreMissingResources = true
    /**
     * Encoding to use when reading and writing bytes from/to stream
     */
    wro.encoding = null
    /**
     * The fully qualified class name of the {@link ro.isdc.wro.manager.WroManagerFactory} implementation.
     */
    wro.managerFactoryClassName = "wro4j.grails.plugin.GrailsWroManagerFactory"

    /** PreProcessor used by wro4j.grails.plugin.GrailsWroManagerFactory  */
    wro.grailsWroManagerFactory.preProcessors = [
        new CssUrlRewritingProcessor(),
        new CssImportPreProcessor(),
        new BomStripperPreProcessor(),
        new SemicolonAppenderPreProcessor(),
        new JSMinProcessor(),
        new JawrCssMinifierProcessor(),
    ]

    /** postProcessor used by wro4j.grails.plugin.GrailsWroManagerFactory  */
    wro.grailsWroManagerFactory.postProcessors = [
        new CssVariablesProcessor(),
    ]

    /** uriLocator used by wro4j.grails.plugin.GrailsWroManagerFactory  */
    wro.grailsWroManagerFactory.uriLocators = [
        new ServletContextUriLocator(),
        new ClasspathUriLocator(),
        new UrlUriLocator(),
    ]

    /**
     * the name of MBean to be used by JMX to configure wro4j.
     */
    wro.mbeanName = null
    /**
     * The parameter used to specify headers to put into the response, used mainly for caching.
     */
    wro.header = null

    /**
     * The path to Wro.groovy. The file name can be located anywhere and named anything.
     */
    wro.wroPath = "file:./grails-app/conf/Wro.groovy"
    

    environments {
      production {
        wro.debug = false
      }
      development {
        wro.debug = true
        // @see
        // http://stackoverflow.com/questions/17935629/is-there-a-way-to-configure-wro4j-to-pick-up-changed-files-without-restart-durin
    	wro.resourceWatcherUpdatePeriod=5
      }
      test {
        wro.debug = true
      }
    }
```


# FAQ

## I have updated the css and js files, and I want to reload the cache

Just call this url : http://localhost:${port}/${appName}/wro/wroApi/reloadCache



## I want to use wro.xml to define my model.

In Config.groovy, just set the Wro4J default ManagerFactory like this :

    wro.managerFactoryClassName = null


## Can I change web-app/conf/Wro.groovy at runtime?

Yes you can. Each time you update the Wro.groovy file, Wro4J is fully reloaded.


## Can I change the wro config in web-app/conf/Config.groovy at runtime?


Yes, of course !!!


## How to build this plugin

run 'grailsw package-plugin'

It will create a plugin archive. Note that the selenium tests require that firefox.exe be on the PATH, so be sure to add it before running anything with runs the tests.

## Releasing a new version of this plugin

1. In Wro4jGrailsPlugin.groovy, set the plugin version to a non-snapshot version number (ex 1.7.1)
2. run 'grailsw publish-plugin --no-scm'
3. Update the version of wro4j in grails-app/conf/BuildConfig.groovy (optional)
4. Update the plugin version in Wro4jGrailsPlugin.groovy to be a snapshot (ex "1.7.2-SNAPSHOT")

The plugin version and the wro4j version should be kept in sync for simplicity's sake. For example, plugin version 1.7.1 should use wro4j 1.7.1.

# Wro Processors

## I want to use CoffeeScript

CoffeeScript is one of the available Resource Processors in Wro4J (others are : Less, Sass, CSSVariables, ...).
See the whole list of resource processors here : <http://code.google.com/p/wro4j/wiki/AvailableProcessors>

In Config.groovy add this :


    import ro.isdc.wro.model.resource.processor.impl.*
    import ro.isdc.wro.model.resource.processor.impl.js.*
    import ro.isdc.wro.model.resource.processor.impl.css.*
    import ro.isdc.wro.extensions.processor.css.*
    import ro.isdc.wro.extensions.processor.js.*

    //your config stuff...

    wro.grailsWroManagerFactory.preProcessors = [
        new CssUrlRewritingProcessor(),
        new CssImportPreProcessor(),
        new BomStripperPreProcessor(),
        new SemicolonAppenderPreProcessor(),
        new JSMinProcessor(),
        new JawrCssMinifierProcessor(),
        new CoffeeScriptProcessor(),  //   <------ CoffeeScript is here
    ]

You see the *new CoffeeScriptProcessor()* line. This is where you add the special processors.

Other lines are the default preProcessors that are used if you don't override the
wro.grailsWroManagerFactory.preProcessors property. You can remove those preProcessors if you don't need them.

Then create your coffeeScript file :

    /web-app/js/script.js.coffee

    $ ->
      $('<h2>Enhanced with Coffee Script</h2>').insertAfter $('h1')

And register coffee script files in your model :

    /grails-app/conf/wro.groovy

    groups {
      all {
        css "/css/*.css"
        js "/js/*.js"
        js "/js/*.js.coffee"
      }
    }

## I want to use LessCss

It is quite similar to CoffeeScript, so look at it first.

Configure your Config.groovy to add this :

    import ro.isdc.wro.model.resource.processor.impl.*
    import ro.isdc.wro.model.resource.processor.impl.js.*
    import ro.isdc.wro.model.resource.processor.impl.css.*
    import ro.isdc.wro.extensions.processor.css.*
    import ro.isdc.wro.extensions.processor.js.*

    //your config stuff...

    wro.grailsWroManagerFactory.preProcessors = [
        new CssUrlRewritingProcessor(),
        new CssImportPreProcessor(),
        new BomStripperPreProcessor(),
        new SemicolonAppenderPreProcessor(),
        new JSMinProcessor(),
        new JawrCssMinifierProcessor(),
        new LessCssProcessor(),  //   <------ Less is here
    ]

Create your less file :

    /web-app/css/style.less

    @my-size = 12px
    h1 {
      font-size: @my-size
    }

And register less files in your model :

    /grails-app/conf/wro.groovy

    groups {
      all {
        css "/css/*.css"
        css "/css/*.less"

        js "/js/*.js"
      }
    }
