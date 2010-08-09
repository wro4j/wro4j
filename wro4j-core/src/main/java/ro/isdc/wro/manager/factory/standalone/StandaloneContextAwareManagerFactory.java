/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.manager.factory.standalone;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.manager.WroManagerFactory;

/**
 * An implementation of {@link WroManagerFactory} which is aware about the run context.
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
   * @param request {@link HttpServletRequest} associated with current request cycle.
   */
  public void initialize(StandaloneContext standaloneContext, HttpServletRequest request);
}
