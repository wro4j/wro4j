Wro4j Grails Plugin
====================

This version is a working SNAPSHOT. You will have to install the plugin using the ZIP archive full path:

    wget http://xxx/grails-wro4j-1.3.9-SNAPSHOT.zip
    grails install-plugin grails-wro4j-1.3.9-SNAPSHOT.zip


Web Resource Optimizer for Grails
----------------------------------


In order to get started with wro4j, you have to follow only 3 simple steps.


Step 1: Install plugin wro4j

    grails install-plugin wro4j



Step 2: Create grails-app/conf/Wro.groovy

    groups{
      all{
        css(minimize: false, "/css/*.css")
        js "/js/*.js"
      }
    }



Step 3: Use optimized resource

    <html>
      <head>
        <title>Web Page using wro4j</title>
        <wro:css group="all"/>
        <wro:js group="all"/>
      </head>
      <body>

      </body>
    </html>



Step 4: (Optional) Configure Wro in Config.groovy :

    /**
     * Boolean flag for enable/disable resource gzipping.
     */
    wro.gzipResources = true
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
    /**
     * the name of MBean to be used by JMX to configure wro4j.
     */
    wro.mbeanName = null
    /**
     * The parameter used to specify headers to put into the response, used mainly for caching.
     */
    wro.header = null

    environments {
      production {
        wro.debug = false
      }
      development {
        wro.debug = true
      }
      test {
        wro.debug = true
      }
    }



FAQ 
---

**I want to use wro.xml to define my model.**

In Config.groovy, just set the Wro4J default ManagerFactory like this :

    wro.managerFactoryClassName = null


