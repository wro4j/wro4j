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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;

/**
 * @author Alex Objelean
 * @created 6 Aug 2011
 */
public class TestSmartWroModelFactory {
  private SmartWroModelFactory factory;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test(expected = WroRuntimeException.class)
  public void noFactoryProvided() throws Exception {
    final List<WroModelFactory> list = Collections.emptyList();
    factory = new SmartWroModelFactory().setFactoryList(list);
    factory.create();
  }

  @Test
  public void onMockFactoryProvided() throws Exception {
    final WroModelFactory mockFactory = Mockito.mock(WroModelFactory.class);
    final List<WroModelFactory> list = Arrays.asList(mockFactory);
    factory = new SmartWroModelFactory().setFactoryList(list);
    Assert.assertNull(factory.create());
  }

  @Test
  public void onMockFactoryProvided2() throws Exception {
    final WroModelFactory mockFactory = Mockito.mock(WroModelFactory.class);
    Mockito.when(mockFactory.create()).thenReturn(new WroModel());
    final List<WroModelFactory> list = Arrays.asList(mockFactory);
    factory = new SmartWroModelFactory().setFactoryList(list);
    Assert.assertNotNull(factory.create());
  }

  @Test(expected=WroRuntimeException.class)
  public void testDefaultInstance() throws Exception {
    factory = new SmartWroModelFactory();
    factory.create();
  }

  @Test
  public void shouldCreateValidModelWhenWroFileIsSet() throws Exception {
    factory = new SmartWroModelFactory();
    final File wroFile = new File(getClass().getResource("wro.xml").toURI());
    factory.setWroFile(wroFile);
    Assert.assertNotNull(factory.create());
  }

  @Test
  public void shouldCreateValidModelWhenAutoDetectIsTrue() throws Exception {
    factory = new SmartWroModelFactory();
    final File wroFile = new File(getClass().getResource("subfolder/wro.json").toURI());
    factory.setWroFile(wroFile).setAutoDetectWroFile(true);
    Assert.assertNotNull(factory.create());
  }

  @Test(expected=WroRuntimeException.class)
  public void testWithInvalidWroFileSet() throws Exception {
    final File wroFile = new File("/path/to/invalid/wro.xml");
    factory = new SmartWroModelFactory().setWroFile(wroFile);
    Assert.assertNotNull(factory.create());
  }
}
