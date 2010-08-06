/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.maven.plugin;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.manager.WroManagerFactory;

/**
 * An implementation of {@link WroManagerFactory} which is aware about the maven context.
 *
 * @author Alex Objelean
 */
public interface MavenContextAwareManagerFactory
  extends WroManagerFactory {
  /**
   * Called by maven plugin for initialization. It is responsibility of the implementor to take care of
   * {@link RunContext} in order to initialize the internals.
   *
   * @param runContext {@link RunContext} holding properties associated with current context.
   * @param request {@link HttpServletRequest} associated with current request cycle.
   */
  public void initialize(RunContext runContext, HttpServletRequest request);
}
