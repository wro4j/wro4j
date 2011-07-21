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