/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for css import processor.
 *
 * @author Alex Objelean
 */
public class TestCssImportPreProcessor extends AbstractWroTest {
  @Test
  public void testFromFolder() throws IOException {
    final CssImportPreProcessor processor = new CssImportPreProcessor();
    updateGroupsProcessorDependencies(processor);
    final URL url = getClass().getResource("cssImport");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "css", "out.css", processor);
  }

  /**
   * This method will allow the fields containing @Inject annotations to be assigned.
   */
  private void updateGroupsProcessorDependencies(final ResourcePreProcessor processor) {
    final GroupsProcessor groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        factory.addUriLocator(new ClasspathUriLocator());
        factory.addUriLocator(new UrlUriLocator());
        factory.addUriLocator(new ServletContextUriLocator());
      }
    };
    groupsProcessor.addPreProcessor(processor);
  }
}
