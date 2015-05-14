//   Copyright 2015
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

package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.net.URL;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.NodeLiterateCoffeeScriptProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor based on lessc shell which uses node.
 *
 * @author Alex Objelean
 */
public class TestNodeLiterateCoffeeScriptProcessor {
  private static boolean isSupported = false;
  @BeforeClass
  public static void beforeClass() {
    //initialize this field only once.
    isSupported = new NodeLiterateCoffeeScriptProcessor().isSupported();
  }

  /**
   * Checks if the test can be run by inspecting {@link NodeLiterateCoffeeScriptProcessor#isSupported()}
   */
  @Before
  public void beforeMethod() {
     Assume.assumeTrue(isSupported);
  }

  @Test
  public void shouldCompileAllFromFolder()
      throws Exception {
    final ResourcePreProcessor processor = new NodeLiterateCoffeeScriptProcessor();
    final URL url = getClass().getResource("coffeeScript/literate");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedNode");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "coffee.md", processor);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new NodeLiterateCoffeeScriptProcessor(), ResourceType.JS);
  }
}
