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
import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException

/**
 * Parse a Groovy DSL String into a {@link WroModel}.
 *
 * @author Romain Philibert
 * @created 19 Jul 2011
 * @since 1.4.0
 */
class GroovyModelParser {

  /** Parse a groovy DSL into a {@link WroModel} */
  static WroModel parse(Script dslScript) {
    if (!dslScript) throw new WroRuntimeException("DSL is invalid : $dslScript")
    try {
      dslScript.metaClass.mixin(WroModelDelegate)
      dslScript.run()
      return (WroModel) dslScript.getProperty("wroModel");
    } catch (GroovyRuntimeException e) {
      throw new WroRuntimeException("Unable to parse DSL : $dslScript", e)
    }
  }

  /** Parse a groovy DSL into a {@link WroModel} */
  static WroModel parse(String dsl) {
    if (!dsl) throw new WroRuntimeException("DSL is invalid : $dsl")
    Script dslScript = new GroovyShell().parse(dsl)
    parse(dslScript);
  }
}

class WroModelDelegate {
  WroModel wroModel = new WroModel()

  void groups(Closure cl) {
    def groupDelegate = new GroupDelegate()
    cl.delegate = groupDelegate
    cl()
    wroModel = new WroModel(groups: (Collection<Group>) cl.resolveGroupResources())
  }

}

class GroupDelegate {
  Map<String, Closure> closures = [:]
  Map<String, HashMap> params = [:]

  def methodMissing(String name, args) {
    if (closures.containsKey(name)) throw new WroRuntimeException("This group is already defined : $name")
    int clIndex = 0
      if (args.length == 2 && args[0] instanceof Map) {
          clIndex++
          params.put(name,args[0])
      }
    closures.put(name, args[clIndex])
  }

  List<Group> resolveGroupResources() {
    List<Group> groups = new ArrayList<Group>()
    closures.each { name, cl ->
      cl.delegate = new ResourceDelegate(groupDelegate: this)
      cl.resolveStrategy = Closure.DELEGATE_ONLY
      cl()

      boolean abstractGroup = !params.containsKey(name) ? false : !params.get(name).containsKey("abstract") ? false : params.get(name).get("abstract")
      if (!abstractGroup) groups.add(new Group(name: name, resources: cl.resources))
    }
    groups
  }
}

class ResourceDelegate {
  GroupDelegate groupDelegate
  List<Resource> resources = new ArrayList<Resource>()
  /** List of groups which are currently processing and are partially parsed. This list is useful in order to catch
   * infinite recurse group reference.       */
  private List<String> processingGroups = []

  void css(Map params = [:], String name) {
    def resource = Resource.create(name, ResourceType.CSS)
    if (params.minimize == false) resource.minimize = false
    resources.add(resource)
  }

  void js(Map params = [:], String name) {
    def resource = Resource.create(name, ResourceType.JS)
    if (params.minimize == false) resource.minimize = false
    resources.add(resource)
  }

  void groupRef(String name) {
    if (processingGroups.contains(name)) {
      throw new RecursiveGroupDefinitionException("Infinite Recursion detected for the group: " + name
          + ". Recursion path: " + processingGroups)
    }
    processingGroups.add(name)
    def cl = groupDelegate.closures.get(name)
    if (!cl) { throw new WroRuntimeException("Reference to an unknown group : $name") }
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_ONLY
    cl?.call()
  }

  def methodMissing(String name, args) {
    this.groupRef(name);
  }
}

