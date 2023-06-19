//   Copyright 2015, Thilo Planz
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;


/**
 * Important node: this processor is not cross platform and has some pre-requisites in order to work.
 * Installation instructions:
 *
 * <pre>
 *   npm install -g coffee-script
 * </pre>
 *
 * It is possible to test whether the utility is available using {@link NodeLiterateCoffeeScriptProcessor#isSupported()}
 *
 * @author Thilo Planz
 * @since 1.7.9
 */
@SupportedResourceType(ResourceType.JS)
public class NodeLiterateCoffeeScriptProcessor extends NodeCoffeeScriptProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {

  public static final String ALIAS = "nodeLiterateCoffeeScript";

  private static final String[] LITERATE_MODE_FLAG = new String[]{ "-l" };

  @Override
  protected final String[] buildOptionalArguments() {
    return LITERATE_MODE_FLAG;
  }

}
