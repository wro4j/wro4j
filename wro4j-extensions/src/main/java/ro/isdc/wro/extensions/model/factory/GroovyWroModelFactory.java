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
package ro.isdc.wro.extensions.model.factory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Creates {@link ro.isdc.wro.model.WroModel} from a groovy DSL.
 *
 * @author Filirom1
 * @created 19 Jul 2011
 */
public class GroovyWroModelFactory
    implements WroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(GroovyWroModelFactory.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    try {
      final WroModel model = GroovyWroModelParser.parse(getWroModelScript());
      LOG.debug("groovy model: ", model);
      if (model == null) {
        throw new WroRuntimeException("Invalid content provided, cannot build model!");
      }
      return model;
    } catch (final Exception e) {
      throw new WroRuntimeException("Invalid model found!", e);
    }
  }

  /**
   * Override this method, in order to provide different json model location.
   *
   * @return stream of the json representation of the model.
   * @throws java.io.IOException if the stream couldn't be read.
   */
  protected Script getWroModelScript() throws IOException {
    return new GroovyShell().parse(getClass().getResourceAsStream("Wro.groovy"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
  }

}
