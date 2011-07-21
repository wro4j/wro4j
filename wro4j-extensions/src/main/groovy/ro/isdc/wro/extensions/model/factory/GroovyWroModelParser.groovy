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
package ro.isdc.wro.extensions.model.factory

import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

/**
 * Parse a Groovy DSL String into a {@link WroModel}.
 *
 * @author Filirom1
 * @created 19 Jul 2011
 */
class GroovyWroModelParser {

  /** Parse a groovy DSL into a {@link WroModel} */
  static WroModel parse(Script dslScript) {
    if (!dslScript) throw new RuntimeException("DSL is invalid : $dslScript")
    try {
      dslScript.metaClass.mixin(WroModelDelegate)
      dslScript.run()
      return (WroModel) dslScript.getProperty("wroModel");
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse DSL : $dslScript")
    }
  }

  /** Parse a groovy DSL into a {@link WroModel} */
  static WroModel parse(String dsl) {
    if (!dsl) throw new RuntimeException("DSL is invalid : $dsl")
    try {
      Script dslScript = new GroovyShell().parse(dsl)
      parse(dslScript);
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse DSL : $dsl")
    }
  }
}

class WroModelDelegate {
  WroModel wroModel = new WroModel()

  void groups(Closure cl) {
    cl.delegate = new GroupDelegate()
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    wroModel = new WroModel(groups: (Collection<Group>) cl.getProperty("groups"))
  }

}

class GroupDelegate {
  List<Group> groups = new ArrayList<Group>()

  def methodMissing(String name, args) {
    def cl = args[0]
    cl.delegate = new ResourceDelegate()
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    groups.add(new Group(name: name, resources: cl.resources))
  }
}

class ResourceDelegate {
  List<Resource> resources = new ArrayList<Resource>()

  void css(Map params = [:], String name) {
    def resource = Resource.create(name, ResourceType.CSS)
    if (params.get("minimize") == false) resource.minimize = false
    resources.add(resource)
  }

  void js(Map params = [:], String name) {
    def resource = Resource.create(name, ResourceType.JS)
    if (params.get("minimize") == false) resource.minimize = false
    resources.add(resource)
  }
}

