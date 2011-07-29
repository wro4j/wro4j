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

import javax.servlet.ServletContext
import ro.isdc.wro.extensions.model.factory.GroovyWroModelFactory
import ro.isdc.wro.manager.factory.BaseWroManagerFactory
import ro.isdc.wro.model.factory.WroModelFactory
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory

/**
 * The Grails WroManagerFactory.
 *
 * It does 2 things :
 *   load the Wro Model DSL (Wro.groovy)
 *   and load the preProcessors, postProcessors and uriLocators from the Config files (Config.groovy override DefaultWroConfig.groovy)
 *
 * @author Filirom1
 */
class GrailsWroManagerFactory extends BaseWroManagerFactory {

  /** Singleton initialized in the doWithSpring closure (in Wro4JGrailsPluguin)  */
  @Lazy ConfigObject config = WroConfigHandler.config.grailsWroManagerFactory

  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new GroovyWroModelFactory() {

      Script getWroModelScript() {
        Class c = new GroovyClassLoader(this.getClass().getClassLoader()).loadClass("Wro")
        (Script) c.newInstance();
      }

    };
  }

  protected ProcessorsFactory newProcessorsFactory() {
    return new GrailsProcessorsFactory(config);
  }

  protected UriLocatorFactory newUriLocatorFactory() {
    return new GrailsUriLocatorFactory(config);
  }
}

/** Load preProcessors and postProcessors from the config file.  */
final class GrailsProcessorsFactory extends SimpleProcessorsFactory {

  public GrailsProcessorsFactory(ConfigObject config) {
    config.preProcessors.each { addPreProcessor(it) }
    config.postProcessors.each { addPostProcessor(it) }
  }
}

/** Load uriLocators from the config file.      */
final class GrailsUriLocatorFactory extends SimpleUriLocatorFactory {

  public GrailsUriLocatorFactory(ConfigObject config) {
    config.uriLocators.each { addUriLocator(it) }
  }
}
