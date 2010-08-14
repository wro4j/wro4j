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
package ro.isdc.wro.extensions.processor.rhino.packer;

import java.io.IOException;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.rhino.AbstractRhinoContext;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * This class is borrowed from Richard Nichols visural project.
 *
 * @author Richard Nichols
 */
public class PackerJs extends AbstractRhinoContext {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PackerJs.class);


  public PackerJs() {
    try {
      final ClasspathUriLocator uriLocator = new ClasspathUriLocator();

      final String packagePath = WroUtil.toPackageAsFolder(getClass());
      final String base2 = IOUtils.toString(uriLocator.locate("classpath:" + packagePath + "/base2.js"));
      final String packer = IOUtils.toString(uriLocator.locate("classpath:" + packagePath + "/packer1.js"));
      getScriptEngine().eval(base2);
      getScriptEngine().eval(packer);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript packer.js", ex);
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }


  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public String pack(final String data)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("pack");
      final String script = multilineEscape(data);
      getScriptEngine().eval("var scriptToPack = \"" + script + "\"");
      LOG.debug("Script to pack evaluated");
      final String packIt = "new Packer().pack(scriptToPack, true, true);";
      LOG.debug("evaluating packer script");
      final String result = getScriptEngine().eval(packIt).toString();
      LOG.debug("packer result: " + result);
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return result;
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }


  private final String multilineEscape(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }
}
