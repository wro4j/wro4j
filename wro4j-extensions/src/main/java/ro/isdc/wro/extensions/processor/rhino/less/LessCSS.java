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

import org.apache.commons.io.IOUtils;

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

      getContext().evaluateString(getScriptableObject(), lessjs, "less.js", 1, null);
      getContext().evaluateString(getScriptableObject(), runjs, "run.js", 1, null);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    }
  }


  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String less(final String data) {
    final String lessitjs = "lessIt(\"" + removeNewLines(data) + "\");";
    final String result = getContext().evaluateString(getScriptableObject(), lessitjs, "lessitjs.js", 1, null).toString();
    return result;
  }
}
