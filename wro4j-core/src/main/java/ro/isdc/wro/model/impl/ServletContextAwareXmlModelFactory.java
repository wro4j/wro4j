/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.impl;

import java.io.InputStream;

import ro.isdc.wro.http.ContextHolder;

/**
 * ServletContext aware Model factory implementation. This factory will run
 * properly only when is used inside a web application. The configuration xml
 * file will be read from the following location: <code>WEB-INF/wro.xml</code>
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public final class ServletContextAwareXmlModelFactory extends XmlModelFactory {
  /**
   * This {@inheritDoc}
   */
  @Override
  protected InputStream getConfigResourceAsStream() {
    return ContextHolder.SERVLET_CONTEXT_HOLDER.get().getResourceAsStream(
        "/WEB-INF/" + XML_CONFIG_FILE);
  }
}
