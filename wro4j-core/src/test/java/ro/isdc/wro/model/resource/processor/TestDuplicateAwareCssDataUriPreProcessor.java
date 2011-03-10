/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.DuplicatesAwareCssDataUriPreProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test for {@link CssDataUriPreProcessor} class.
 *
 * @author Alex Objelean
 * @created Created on Mat 09, 2010
 */
public class TestDuplicateAwareCssDataUriPreProcessor extends AbstractWroTest {
  private ResourcePreProcessor processor;


  @Before
  public void init() {
    processor = new DuplicatesAwareCssDataUriPreProcessor();
    final GroupsProcessor groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        factory.addUriLocator(new ServletContextUriLocator());
        factory.addUriLocator(new UrlUriLocator());
        factory.addUriLocator(new ClasspathUriLocator());
      }
    };
    groupsProcessor.addPreProcessor(processor);
  }


  /**
   * Check if a large dataUri with more than 32KB does not replace original url.
   */
  @Test
  public void processLargeDataUri()
    throws IOException {
    final URL url = getClass().getResource("duplicateAwareDataUri");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css",
      WroUtil.newResourceProcessor(createMockResource("file:" + testFolder.getPath() + "/test.css"), processor));
  }


  /**
   * @param resourceUri the resource should return.
   * @return mocked {@link Resource} object.
   */
  private Resource createMockResource(final String resourceUri) {
    final Resource resource = Mockito.mock(Resource.class);
    Mockito.when(resource.getUri()).thenReturn(resourceUri);
    return resource;
  }
}
