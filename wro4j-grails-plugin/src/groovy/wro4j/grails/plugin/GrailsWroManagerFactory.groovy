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

import ro.isdc.wro.extensions.model.factory.GroovyModelFactory
import ro.isdc.wro.extensions.model.factory.GroovyModelParser
import ro.isdc.wro.manager.factory.BaseWroManagerFactory
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.factory.WroModelFactory
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
  private classLoader = new GroovyClassLoader()

  @Override
  protected WroModelFactory newModelFactory() {
    return new GroovyModelFactory() {
      WroModel create() {
        return GroovyModelParser.parse(WroDSLHandler.dsl)
      }
    }
  }

  protected ProcessorsFactory newProcessorsFactory() {
    return new GrailsProcessorsFactory(config)
  }

  /** WroConfigHandler initialized in the doWithSpring closure (in Wro4JGrailsPluguin)             */
  ConfigObject getConfig() { WroConfigHandler.config.grailsWroManagerFactory }
}

/** Load preProcessors and postProcessors from the config file.             */
final class GrailsProcessorsFactory extends SimpleProcessorsFactory {

  GrailsProcessorsFactory(ConfigObject config) {
    config.preProcessors.each { addPreProcessor(it) }
    config.postProcessors.each { addPostProcessor(it) }
  }
}
