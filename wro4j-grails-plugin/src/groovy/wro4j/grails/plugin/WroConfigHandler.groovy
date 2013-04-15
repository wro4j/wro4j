/*
* Copyright 2011 Wro4J
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package wro4j.grails.plugin

import grails.util.Environment

import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Helper to load DefaultWroConfig.groovy and merge it with merge Config.groovy
 *
 * @author Filirom1
 */
class WroConfigHandler {
  static GrailsApplication application
  private static ConfigObject config
  private static final String CONFIG_PREFIX = "wro"

  /**
   * Parse and load the configuration.
   * @return the configuration
   */
  static synchronized ConfigObject getConfig() {
    if (config == null) {
      reloadConfig()
    }
    return config
  }

  /**
   * Force a reload of the security configuration.
   */
  static void reloadConfig() {
    def grailsConfig = application.getConfig()
    mergeConfig((ConfigObject) grailsConfig.getProperty(CONFIG_PREFIX), "DefaultWroConfig")
  }

  /**
   * Merge in a secondary config (provided by a plugin as defaults) into the main config.
   * @param currentConfig the current configuration
   * @param className the name of the config class to load
   */
  private static void mergeConfig(final ConfigObject currentConfig, final String className) {
    GroovyClassLoader classLoader = new GroovyClassLoader(WroConfigHandler.getClassLoader())
    ConfigSlurper slurper = new ConfigSlurper(Environment.getCurrent().getName())
    ConfigObject secondaryConfig
    try {
      secondaryConfig = slurper.parse(classLoader.loadClass(className))
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("unlable to merge config " + currentConfig + ", " + className, e)
    }

    config = mergeConfig(currentConfig, (ConfigObject) secondaryConfig.getProperty(CONFIG_PREFIX))
  }

  /**
   * Merge two configs together. The order is important; if <code>secondary</code> is not null then
   * start with that and merge the main config on top of that. This lets the <code>secondary</code>
   * config act as default values but let user-supplied values in the main config override them.
   *
   * @param currentConfig the main config, starting from Config.groovy
   * @param secondary new default values
   * @return the merged configs
   */
  @SuppressWarnings("unchecked")
  private static ConfigObject mergeConfig(final ConfigObject currentConfig, final ConfigObject secondary) {
    ConfigObject config = new ConfigObject()
    if (secondary == null) {
      config.putAll(currentConfig)
    }
    else {
      config.putAll(secondary.merge(currentConfig))
    }
    return config
  }
}
