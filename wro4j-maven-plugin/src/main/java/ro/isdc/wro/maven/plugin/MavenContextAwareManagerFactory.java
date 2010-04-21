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
   * Called by maven plugin for initialization.
   *
   * @param runContext
   * @param request
   */
  public void initialize(RunContext runContext, HttpServletRequest request);
}
