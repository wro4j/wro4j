// Copyright 2015
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ro.isdc.wro.extensions.processor.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoLiterateCoffeeScriptProcessor} but will prefer using {@link NodeLiterateCoffeeScriptProcessor}
 * if it is supported and will fallback to rhino based processor.<br/>
 *
 * @author Thilo Planz
 * @since 1.7.9
 */
@SupportedResourceType(ResourceType.JS)
public class LiterateCoffeeScriptProcessor
    extends AbstractNodeWithFallbackProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(LiterateCoffeeScriptProcessor.class);
  public static final String ALIAS = "literateCoffeeScript";

  @Override
  protected ResourcePreProcessor createNodeProcessor() {
    LOG.debug("creating NodeLiterateCoffeeScript processor");
    return new NodeLiterateCoffeeScriptProcessor();
  }

  @Override
  protected ResourcePreProcessor createFallbackProcessor() {
    LOG.debug("Node CoffeeScript is not supported. Using fallback Rhino processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoLiterateCoffeeScriptProcessor();
      }
    });
  }
}
