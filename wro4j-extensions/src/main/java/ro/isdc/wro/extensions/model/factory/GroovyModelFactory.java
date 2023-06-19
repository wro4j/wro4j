/*
 * Copyright 2011 wro4j
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

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.AbstractWroModelFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * <p>Creates {@link ro.isdc.wro.model.WroModel} from a groovy DSL.</p>
 *
 * <p>This class is thread-safe because it doesn't have any state.</p>
 *
 * @author Romain Philibert
 * @since 1.4.0
 */
public class GroovyModelFactory
  extends AbstractWroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(GroovyModelFactory.class);
  /**
   * Default name of the file used to retrieve the model.
   */
  private static final String DEFAULT_FILE_NAME = "wro.groovy";
  /**
   * Alias for this model factory used by provider.
   */
  public static final String ALIAS = "groovy";


  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    final StopWatch stopWatch = new StopWatch("Create Wro Model from Groovy");
    final Script script;
    try {
      stopWatch.start("parseStream");
      script = new GroovyShell().parse(new InputStreamReader(new AutoCloseInputStream(getModelResourceAsStream())));
      LOG.debug("Parsing groovy script to build the model");
      stopWatch.stop();

      stopWatch.start("parseScript");
      final WroModel model = GroovyModelParser.parse(script);
      stopWatch.stop();
      LOG.debug("groovy model: {}", model);
      if (model == null) {
        throw new WroRuntimeException("Invalid content provided, cannot build model!");
      }
      return model;
    } catch (final IOException e) {
      throw new WroRuntimeException("Invalid model found!", e);
    } finally {
      LOG.debug(stopWatch.prettyPrint());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultModelFilename() {
    return DEFAULT_FILE_NAME;
  }
}
