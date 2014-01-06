/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. This was inspired by TestFingerprintCreatorNamingStrategy.
 */

package ro.isdc.wro.model.resource.support.naming;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test class for {@link DefaultHashEncoderNamingStrategy}
 * 
 * @author Alex Objelean
 * @created 15 Aug 2012
 */
public class TestDefaultHashEncoderNamingStrategy {
  private NamingStrategy namingStrategy;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    namingStrategy = new DefaultHashEncoderNamingStrategy();
    WroTestUtils.createInjector().inject(namingStrategy);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullResourceName()
      throws Exception {
    namingStrategy.rename(null, WroUtil.EMPTY_STREAM);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullStream()
      throws Exception {
    namingStrategy.rename("fileName.js", null);
  }
  
  @Test
  public void shouldRenameResourceWithEmptyContent()
      throws Exception {
    final String result = namingStrategy.rename("fileName", WroUtil.EMPTY_STREAM);
    assertEquals("fileName-da39a3ee5e6b4b0d3255bfef95601890afd80709", result);
  }
  
  @Test
  public void shouldRenameResourceWithSomeContent()
      throws Exception {
    final String result = namingStrategy.rename("anotherFile.js", new ByteArrayInputStream("someContent".getBytes()));
    assertEquals("anotherFile-99ef8ae827896f2af4032d5dab9298ec86309abf.js", result);
  }
  
  @Test
  public void shouldRenameResourceContainedInAFolder()
      throws Exception {
    final String result = namingStrategy.rename("folder1/folder2/resource.css",
        new ByteArrayInputStream("someContent".getBytes()));
    assertEquals("folder1/folder2/resource-99ef8ae827896f2af4032d5dab9298ec86309abf.css", result);
  }
}
