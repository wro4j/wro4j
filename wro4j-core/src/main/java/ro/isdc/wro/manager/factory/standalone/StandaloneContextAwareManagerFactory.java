/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.manager.factory.standalone;

import ro.isdc.wro.manager.WroManagerFactory;

/**
 * An implementation of {@link WroManagerFactory} aware about the run context.
 *
 * @author Alex Objelean
 */
public interface StandaloneContextAwareManagerFactory
  extends WroManagerFactory {
  /**
   * Called by standalone process for initialization. It is responsibility of the implementor to take care of
   * {@link StandaloneContext} in order to initialize the internals.
   *
   * @param standaloneContext {@link StandaloneContext} holding properties associated with current context.
   */
  public void initialize(StandaloneContext standaloneContext);
}
