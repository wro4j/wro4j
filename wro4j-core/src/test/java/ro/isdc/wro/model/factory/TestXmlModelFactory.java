/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
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
    final WroModel model = loadModelFromLocation("wro-minimizeAttribute.xml");
    final Group group = model.getGroupByName(new WroModelInspector(model).getGroupNames().get(0));
    final List<Resource> resourceList = group.getResources();
    LOG.debug("resources: " + resourceList);
    assertEquals(false, resourceList.get(0).isMinimize());
    assertEquals(true, resourceList.get(1).isMinimize());
    assertEquals(true, resourceList.get(2).isMinimize());
    LOG.debug("model: " + model);
  }

  @Test
  public void testValidImports() {
    final WroModel model = loadModelFromLocation("testimport/validImports.xml");
    assertEquals(2, new WroModelInspector(model).getGroupNames().size());
    LOG.debug("model: " + model);
  }

  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testRecursiveImports() {
    loadModelFromLocation("testimport/recursive.xml");
  }

  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testDeepRecursiveImports() {
    loadModelFromLocation("testimport/deepRecursive.xml");
  }

  @Test(expected = RecursiveGroupDefinitionException.class)
  public void testCircularImports() {
    loadModelFromLocation("testimport/circular1.xml");
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidImports() {
    loadModelFromLocation("testimport/invalidImports.xml");
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
      loadModelFromLocation("testimport/wildcard.xml");
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
        assertEquals(expected, factory.create());
        return null;
      }
    }), 10);
  }

  @Test
  public void shouldCreateEmptyModelWhenAllGroupsAreAbstract() {
    final WroModel model = loadModelFromLocation("shouldCreateEmptyModelWhenAllGroupsAreAbstract.xml");
    assertTrue(model.getGroups().isEmpty());
  }

  @Test
  public void shouldCreateNonEmptyModelWhenSomeGroupsAreAbstract() {
    final WroModel model = loadModelFromLocation("shouldCreateNonEmptyModelWhenSomeGroupsAreAbstract.xml");
    assertEquals(2, model.getGroups().size());
  }

  @Test
  public void shouldContainOnlyNonAbstractGroups() {
    final WroModel model = loadModelFromLocation("shouldContainOnlyNonAbstractGroups.xml");
    assertEquals(1, model.getGroups().size());
    final Group group = model.getGroups().iterator().next();
    assertEquals("nonAbstract", group.getName());
    assertEquals(5, group.getResources().size());
  }

  @Test(expected = WroRuntimeException.class)
  public void shouldDetectInvalidGroupReference() {
    final WroModel model = loadModelFromLocation("shouldDetectInvalidGroupReference.xml");
    assertTrue(model.getGroups().isEmpty());
  }

  @Test
  public void shouldDetectGroupReferenceFromImportedModel() {
    final WroModel model = loadModelFromLocation("shouldDetectGroupReferenceFromImportedModel.xml");
    assertEquals(2, model.getGroups().size());
  }

  @Test
  public void shouldLoadEmptyModel() {
    final WroModel model = loadModelFromLocation("emptyModel.xml");
    assertEquals(0, model.getGroups().size());
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotCreateFromXmlWithInvalidNamespace() {
    loadModelFromLocation("invalidNamespace.xml");
  }


  private WroModel loadModelFromLocation(final String location) {
    final WroModelFactory factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        // get a class relative test resource
        return TestXmlModelFactory.class.getResourceAsStream(location);
      }
    };
    WroTestUtils.init(factory);
    return factory.create();
  }
}
