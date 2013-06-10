/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestGroupsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  private GroupsProcessor victim;
  final String groupName = "group";

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new GroupsProcessor();
    initVictim(new WroConfiguration());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private void initVictim(final WroConfiguration config) {
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(new Group(groupName)));
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(modelFactory);
    initVictim(config, managerFactory);
  }

  private void initVictim(final WroConfiguration config, final WroManagerFactory managerFactory) {
    Context.set(Context.standaloneContext(), config);
    final Injector injector = InjectorBuilder.create(managerFactory).build();
    injector.inject(victim);
  }

  @Test
  public void shouldReturnEmptyStringWhenGroupHasNoResources() {
    final CacheKey key = new CacheKey(groupName, ResourceType.JS, true);
    Assert.assertEquals(StringUtils.EMPTY, victim.process(key));
  }

  /**
   * Same as above, but with ignoreEmptyGroup config updated.
   */
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenGroupHasNoResourcesAndIgnoreEmptyGroupIsFalse() {
    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreEmptyGroup(false);
    initVictim(config);
    final CacheKey key = new CacheKey("group", ResourceType.JS, true);
    victim.process(key);
  }

  @Test
  public void shouldLeaveContentUnchangedWhenAProcessorFails() {
    final CacheKey key = new CacheKey(groupName, ResourceType.JS, true);
    final Group group = new Group(groupName).addResource(Resource.create("1.js")).addResource(Resource.create("2.js"));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(new WroModel().addGroup(group));
    // the locator which returns the name of the resource as its content
    final UriLocatorFactory locatorFactory = new SimpleUriLocatorFactory().addLocator(new UriLocator() {
      public boolean accept(final String uri) {
        return true;
      }
      public InputStream locate(final String uri)
          throws IOException {
        return new ByteArrayInputStream(uri.getBytes());
      }
    });

    final ResourcePreProcessor failingPreProcessor = new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        throw new IOException("BOOM!");
      }
    };
    final ProcessorsFactory processorsFactory = new SimpleProcessorsFactory().addPreProcessor(failingPreProcessor).addPostProcessor(
        new ProcessorDecorator(failingPreProcessor));
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    managerFactory.setProcessorsFactory(processorsFactory);

    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreFailingProcessor(true);
    initVictim(config, managerFactory);

    final String actual = victim.process(key);
    Assert.assertEquals("1.js2.js", actual);
  }

  @Test
  public void shouldApplyOnlyEligibleProcessors() throws Exception {
    final CssMinProcessor cssMinProcessor = Mockito.spy(new CssMinProcessor());
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory();
    managerFactory.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(cssMinProcessor));
    managerFactory.setModelFactory(WroTestUtils.simpleModelFactory(new WroModel().addGroup(new Group("g1").addResource(Resource.create("/script.js")))));
    initVictim(new WroConfiguration(), managerFactory);

    victim.process(new CacheKey("g1", ResourceType.JS, true));
    verify(cssMinProcessor, Mockito.never()).process(Mockito.any(Resource.class), Mockito.any(Reader.class), Mockito.any(Writer.class));
  }


  @Test
  public void shouldApplyEligibleMinimizeAwareProcessors() throws Exception {
    final JSMinProcessor cssMinProcessor = Mockito.spy(new JSMinProcessor());
    final BaseWroManagerFactory managerFactory = new BaseWroManagerFactory();
    managerFactory.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(cssMinProcessor));
    managerFactory.setModelFactory(WroTestUtils.simpleModelFactory(new WroModel().addGroup(new Group("g1").addResource(Resource.create("/script.js")))));
    initVictim(new WroConfiguration(), managerFactory);

    victim.process(new CacheKey("g1", ResourceType.JS, true));
    verify(cssMinProcessor).process(Mockito.any(Resource.class), Mockito.any(Reader.class), Mockito.any(Writer.class));
  }
}
