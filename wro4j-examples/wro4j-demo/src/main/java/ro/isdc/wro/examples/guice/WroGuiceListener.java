/**
 * Copyright(c)2011
 */
package ro.isdc.wro.examples.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;


/**
 * @author Alex Objelean
 */
public class WroGuiceListener extends GuiceServletContextListener {
  /**
   * {@inheritDoc}
   */
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new WroExamplesServletModule());
  }
}
