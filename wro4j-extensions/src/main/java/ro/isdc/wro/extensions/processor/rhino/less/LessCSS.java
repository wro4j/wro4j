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
package ro.isdc.wro.extensions.processor.rhino.less;

import java.io.IOException;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.rhino.AbstractRhinoContext;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * This class is borrowed from Richard Nichols visural project.
 *
 * @author Richard Nichols
 */
public class LessCSS extends AbstractRhinoContext {

  public LessCSS() {
    try {
      final ClasspathUriLocator uriLocator = new ClasspathUriLocator();

      final String packagePath = WroUtil.toPackageAsFolder(getClass());
      final String lessjs = IOUtils.toString(uriLocator.locate("classpath:" + packagePath + "/less.js"));
      final String runjs = IOUtils.toString(uriLocator.locate("classpath:" + packagePath + "/run.js"));

      getScriptEngine().eval(lessjs);
      getScriptEngine().eval(runjs);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Unable to evaluate the script", e);
    }
  }



  private String removeNewLines(final String data) {
    return data.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }

  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String less(final String data) {
    try {
      final String lessitjs = "lessIt(\"" + removeNewLines(data) + "\");";
      final String result = (String)getScriptEngine().eval(lessitjs);
      return result;
    } catch (final ScriptException e) {
      throw new WroRuntimeException("Could not execute the script", e);
    }
  }
}
