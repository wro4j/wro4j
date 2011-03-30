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

import ro.isdc.wro.model.resource.Resource;
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
public class TestDuplicateAwareCssDataUriPreProcessor {
  private ResourcePreProcessor processor;


  @Before
  public void init() {
    processor = new DuplicatesAwareCssDataUriPreProcessor();
    WroTestUtils.initProcessor(processor);
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
