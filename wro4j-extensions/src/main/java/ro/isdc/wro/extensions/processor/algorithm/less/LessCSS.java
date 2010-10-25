/*
 *  Copyright 2010.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package ro.isdc.wro.extensions.processor.algorithm.less;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;


/**
 * This class is inspired from Richard Nichols visural project.
 *
 * @author Alex Objelean
 */
public class LessCSS {
  private static final Logger LOG = LoggerFactory.getLogger(LessCSS.class);

  public LessCSS() {}


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_LESS = "less-1.0.36.js";
      final InputStream lessStream = getClass().getResourceAsStream(SCRIPT_LESS);
      final String SCRIPT_RUN = "run.js";
      final InputStream runStream = getClass().getResourceAsStream(SCRIPT_RUN);

      return RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(lessStream, SCRIPT_LESS).evaluateChain(runStream,
        SCRIPT_RUN);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    }
  }

  /**
   * Replace new line characters with empty spaces.
   * @param data
   * @return
   */
  private static String removeNewLines(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }


  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String less(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("lessify");
    try {
      final String lessitjs = "lessIt(\"" + removeNewLines(data) + "\");";
      final Object result = builder.evaluate(lessitjs, "lessIt");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Could not execute the script", e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
