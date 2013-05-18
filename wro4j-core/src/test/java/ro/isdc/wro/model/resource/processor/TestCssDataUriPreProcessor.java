/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CssDataUriPreProcessor} class.
 *
 * @author Alex Objelean
 * @created Created on Mat 09, 2010
 */
public class TestCssDataUriPreProcessor {
  private final String PROXY_RESOURCE_PATH = "classpath:ro/isdc/wro/model/resource/processor/dataUri/proxyImage/";
  private ResourcePreProcessor processor;

  protected DataUriGenerator createMockDataUriGenerator() {
    try {
      final DataUriGenerator uriGenerator = Mockito.mock(DataUriGenerator.class);
      Mockito.when(uriGenerator.generateDataURI(Mockito.any(InputStream.class), Mockito.anyString())).thenReturn(
          "data:image/png;base64,iVBORw0KG");
      return uriGenerator;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot create DataUriGenerator mock", e);
    }
  }

  @Before
  public void init()
      throws Exception {
    Context.set(Context.standaloneContext());
    processor = new CssDataUriPreProcessor() {
      @Override
      protected DataUriGenerator getDataUriGenerator() {
        return createMockDataUriGenerator();
      }
    };
    initProcessor(processor);
  }

  final void initProcessor(final ResourcePreProcessor processor) {
    final BaseWroManagerFactory factory = new BaseWroManagerFactory();
    factory.setUriLocatorFactory(createLocatorFactory());
    factory.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(processor));
    final Injector injector = InjectorBuilder.create(factory).build();
    injector.inject(processor);
  }

  /**
   * @return a locator factory which handles absolute url locations and failed servletContext relative url's by serving
   *         proxy resources from classpath. This is useful to make test pass without internet connection.
   */
  private UriLocatorFactory createLocatorFactory() {
    final UriLocatorFactory locatorFactory = new SimpleUriLocatorFactory().addLocator(
        new ServletContextUriLocator() {
          @Override
          public InputStream locate(final String uri)
              throws IOException {
            try {
              return super.locate(uri);
            } catch (final Exception e) {
              return new ClasspathUriLocator().locate(PROXY_RESOURCE_PATH + "test1.png");
            }
          }
        }).addLocator(new UrlUriLocator() {
      @Override
      public InputStream locate(final String uri)
          throws IOException {
        // avoid external connections
        if (uri.startsWith("http")) {
          return new ClasspathUriLocator().locate(PROXY_RESOURCE_PATH + "test2.png");
        }
        return super.locate(uri);
      }
    });
    return locatorFactory;
  }

  @Test
  public void shouldTransformResourcesFromFolder()
      throws Exception {
    final URL url = getClass().getResource("dataUri");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldTransformLargeResources()
      throws Exception {
    processor = new CssDataUriPreProcessor();
    initProcessor(processor);

    final URL url = getClass().getResource("dataUri");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedLarge");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportOnlyCssResources() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
