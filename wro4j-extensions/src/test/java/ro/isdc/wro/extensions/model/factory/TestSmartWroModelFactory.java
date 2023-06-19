/*
 * Copyright 2011 wro4j Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;


/**
 * @author Alex Objelean
 */
public class TestSmartWroModelFactory {
  private SmartWroModelFactory factory;
  private Injector injector;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = WroRuntimeException.class)
  public void noFactoryProvided()
      throws Exception {
    final List<WroModelFactory> list = Collections.emptyList();
    factory = new SmartWroModelFactory().setFactoryList(list);
    injector.inject(factory);
    factory.create();
  }

  @Test
  public void onMockFactoryProvided()
      throws Exception {
    final WroModelFactory mockFactory = Mockito.mock(WroModelFactory.class);
    final List<WroModelFactory> list = Arrays.asList(mockFactory);
    factory = new SmartWroModelFactory().setFactoryList(list);
    injector.inject(factory);
    Assert.assertNull(factory.create());
  }

  @Test
  public void onMockFactoryProvided2()
      throws Exception {
    final WroModelFactory mockFactory = Mockito.mock(WroModelFactory.class);
    Mockito.when(mockFactory.create()).thenReturn(new WroModel());
    final List<WroModelFactory> list = Arrays.asList(mockFactory);
    factory = new SmartWroModelFactory().setFactoryList(list);
    injector.inject(factory);
    Assert.assertNotNull(factory.create());
  }

  @Test(expected = WroRuntimeException.class)
  public void testDefaultInstance()
      throws Exception {
    factory = new SmartWroModelFactory();
    injector.inject(factory);
    factory.create();
  }

  @Test
  public void shouldCreateValidModelWhenWroFileIsSet()
      throws Exception {
    factory = new SmartWroModelFactory();
    final File wroFile = new File(getClass().getResource("wro.xml").toURI());
    factory.setWroFile(wroFile);
    injector.inject(factory);
    Assert.assertNotNull(factory.create());
  }

  @Test
  public void shouldCreateValidModelWhenAutoDetectIsTrue()
      throws Exception {
    factory = new SmartWroModelFactory();
    final File wroFile = new File(getClass().getResource("subfolder/wro.json").toURI());
    factory.setWroFile(wroFile).setAutoDetectWroFile(true);
    injector.inject(factory);
    Assert.assertNotNull(factory.create());
  }

  @Test(expected = WroRuntimeException.class)
  public void testWithInvalidWroFileSet()
      throws Exception {
    final File wroFile = new File("/path/to/invalid/wro.xml");
    factory = new SmartWroModelFactory().setWroFile(wroFile);
    injector.inject(factory);
    Assert.assertNotNull(factory.create());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotCreateModelWhenNullListOfFactoriesProvided()
      throws Exception {
    factory = new SmartWroModelFactory() {
      @Override
      protected List<WroModelFactory> newWroModelFactoryFactoryList() {
        return null;
      }
    };
    injector.inject(factory);
    factory.create();
  }

  @Test
  public void shouldCreateModelEvenWhenFirstAttemptFails()
      throws Exception {
    factory = createTestSmartModelFactory();
    Assert.assertNotNull(factory.create());
  }

  @Test(expected = WroRuntimeException.class)
  public void shouldFailCreatingModelEvenWhenFirstAttemptFailsAndAutoDetectIsDisabled()
      throws Exception {
    factory = createTestSmartModelFactory();
    factory.setAutoDetectWroFile(false);
    factory.create();
  }

  /**
   * Creates a {@link SmartWroModelFactory} which is provided with a list of two {@link WroModelFactory}'s. The first
   * one is failing, the second one is working.
   */
  private SmartWroModelFactory createTestSmartModelFactory() {
    final WroModelFactory failingModelFactory = new WroModelFactory() {
      @Override
      public WroModel create() {
        throw new WroRuntimeException("Cannot create model", new IOException("invalid model stream"));
      }

      @Override
      public void destroy() {
      }
    };
    final WroModelFactory workingModelFactory = new WroModelFactory() {
      @Override
      public WroModel create() {
        return new WroModel();
      }

      @Override
      public void destroy() {
      }
    };
    final SmartWroModelFactory factory = new SmartWroModelFactory() {
      @Override
      protected List<WroModelFactory> newWroModelFactoryFactoryList() {
        final List<WroModelFactory> list = new ArrayList<WroModelFactory>();
        list.add(failingModelFactory);
        list.add(workingModelFactory);
        return list;
      }
    };
    injector.inject(factory);
    return factory;
  }

  /**
   * Checks that modelFactories provided as a list will have all required fields injected (ex: locatorFactory)
   */
  @Test
  public void shouldInjectInnerModelFactories() {
    final SmartWroModelFactory factory = new SmartWroModelFactory() {
      @Override
      protected List<WroModelFactory> newWroModelFactoryFactoryList() {
        final List<WroModelFactory> list = new ArrayList<WroModelFactory>();
        list.add(new CustomWroModel() {
          @Override
          public WroModel create() {
            Assert.assertNotNull("Should have an injected locator!", uriLocatorFactory);
            return new WroModel();
          }
        });
        return list;
      }
    };
    injector.inject(factory);
    Assert.assertNotNull(factory.create());
  }

  private static class CustomWroModel
      implements WroModelFactory {
    @Inject
    UriLocatorFactory uriLocatorFactory;

    @Override
    public WroModel create() {
      return null;
    }

    @Override
    public void destroy() {
    }
  }
}
