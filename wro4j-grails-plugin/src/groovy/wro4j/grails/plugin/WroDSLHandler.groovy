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

/**
 * Helper to have an updated version of Wro.groovy DSL
 *
 * When grails detects that Wro.groovy has changed, the classLoader is not reloaded. This is a problem.
 * So we load Wro.groovy with WroDSLHandler#loadDefaultDSL() the first time from the Class Loader
 * And, each time grails detects that Wro.groovy has changed, we manually update the DSL by calling WroDSLHandler#setDsl(Script)
 *
 * @author Filirom1
 */
class WroDSLHandler {

  static Script dsl

  static synchronized Script getDsl() {
    if (dsl == null) {
      dsl = loadDefaultDSL()
    }
    dsl
  }

  /** Load the DSL from the default class loader      */
  private static loadDefaultDSL() {
    WroDSLHandler.getClassLoader().loadClass("Wro").newInstance()
  }

  static synchronized void setDsl(Script dsl) {
    this.dsl = dsl
  }
}
