/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.FallbackAwareXmlModelFactory;
import ro.isdc.wro.model.factory.WroModelFactory;

/**
 * An example of how resources can be managed without restarting the server.
 *
 * @author Alex Objelean
 */
public class ExternalModelConfigurableWroManagerFactory
  extends ExtensionsConfigurableWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory newModelFactory() {
    return new FallbackAwareXmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        return new FileInputStream("D:\\temp\\____wro\\wro.xml");
      }
    };
  }
}
