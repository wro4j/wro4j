/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Decorator of {@link IOFileFilter}.
 *
 * @author Alex Objelean
 */
public class IOFileFilterDecorator
    implements IOFileFilter {
  /**
   * Instance to decorate.
   */
  private final IOFileFilter decorated;

  public IOFileFilterDecorator(final IOFileFilter decorated) {
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final File file) {
    return decorated.accept(file);
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final File dir, final String name) {
    return decorated.accept(dir, name);
  }

}
