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

import ro.isdc.wro.http.ConfigurableWroFilter
import javax.servlet.FilterConfig

/**
 * A WroFilter that can be reloaded with the #reload() method
 *
 * @author Filirom1
 */
class ReloadableWroFilter extends ConfigurableWroFilter {

  private FilterConfig filterConfig;

  /** Reload the wroFilter  */
  void reload() {
    this.destroy()
    this.init(filterConfig)
  }

  /** Store the filterConfig     */
  void doInit(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }
}
