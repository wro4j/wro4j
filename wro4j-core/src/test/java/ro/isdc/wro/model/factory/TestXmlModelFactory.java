/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestXmlModelFactory.
 * 
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestXmlModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestXmlModelFactory.class);
  private WroModelFactory factory;
  
  @Before
  public void setUp() {
    final Context context = Context.standaloneContext();
    Context.set(context);
    context.getConfig().setCacheUpdatePeriod(0);
    context.getConfig().setModelUpdatePeriod(0);
  }
  
  @After
  public void tearDown() {
    factory.destroy();
  }
  
  @Test(expected = RecursiveGroupDefinitionException.class)
  public void recursiveGroupThrowsException() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("recursive.xml");
      }
    };
    factory.create();
  }
  
  @Test
  public void testWithUpdatePeriodSet() {
    Context.get().getConfig().setCacheUpdatePeriod(1);
    Context.get().getConfig().setModelUpdatePeriod(1);
    testSuccessfulCreation();
  }
  
  // TODO use two concurrent calls
  @Test
  public void testTwoConcurrentCreationCalls() {
    testSuccessfulCreation();
    factory.create();
  }
  
  @Test
  public void testSuccessfulCreation() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("wro1.xml");
      }
    };
    // the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.create();
    LOG.debug("model: " + model);
  }
  
  @Test
  public void testMinimizeAttributePresence() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("wro-minimizeAttribute.xml");
      }
    };
    // the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.create();
    final Group group = model.getGroupByName(model.getGroupNames().get(0));
    final List<Resource> resourceList = group.getResources();
    LOG.debug("resources: " + resourceList);
    assertEquals(false, resourceList.get(0).isMinimize());
    assertEquals(true, resourceList.get(1).isMinimize());
    assertEquals(true, resourceList.get(2).isMinimize());
    LOG.debug("model: " + model);
  }
  
  @Test
  public void testValidImports() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/validImports.xml");
      }
    };
    WroTestUtils.init(factory);
    // the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.create();
    assertEquals(2, model.getGroupNames().size());
    LOG.debug("model: " + model);
  }
  
  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testRecursiveImports() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/recursive.xml");
      }
    };
    WroTestUtils.init(factory);
    factory.create();
  }
  
  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testDeepRecursiveImports() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/deepRecursive.xml");
      }
    };
    WroTestUtils.init(factory);
    factory.create();
  }
  
  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testCircularImports() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/circular1.xml");
      }
    };
    WroTestUtils.init(factory);
    factory.create();
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testInvalidImports() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/invalidImports.xml");
      }
    };
    WroTestUtils.init(factory);
    factory.create();
  }
  
  @Test
  public void shouldCreateEmptyModelWhenValidationDisabledAndXmlIsNotValid() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return new ByteArrayInputStream("<xml></xml>".getBytes());
      }
    }.setValidateXml(false);
    WroTestUtils.init(factory);
    // will create an empty model
    assertEquals(new WroModel(), factory.create());
  }
  
  @Test(expected = SAXParseException.class)
  public void testWildcardImports()
      throws Throwable {
    try {
      factory = new XmlModelFactory() {
        @Override
        protected InputStream getModelResourceAsStream() {
          // get a class relative test resource
          return TestXmlModelFactory.class.getResourceAsStream("testimport/wildcard.xml");
        }
      };
      WroTestUtils.init(factory);
      factory.create();
    } catch (final WroRuntimeException e) {
      throw e.getCause();
    }
  }
  
  @Test
  public void shouldBeThreadSafe() throws Exception {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream("testimport/validImports.xml");
      }
    }; 
    WroTestUtils.init(factory);
    final WroModel expected = factory.create();
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        Assert.assertEquals(expected, factory.create());
        return null;
      }
    }), 10);
  }
}
