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

import ro.isdc.wro.extensions.model.factory.GroovyWroModelFactory
import javax.servlet.ServletContext
import ro.isdc.wro.manager.factory.BaseWroManagerFactory
import ro.isdc.wro.model.factory.WroModelFactory

/**
 * Load Wro.groovy the Wro Model DSL
 *
 * @author Filirom1
 */
class GrailsWroManagerFactory extends BaseWroManagerFactory {
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new GroovyWroModelFactory() {

      Script getWroModelScript() {
        Class c = new GroovyClassLoader(this.getClass().getClassLoader()).loadClass("Wro")
        (Script) c.newInstance();
      }

    };
  }
}