/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.support.hash;

import java.util.Map;

import ro.isdc.wro.model.resource.support.naming.NamingStrategy;

/**
 * A service provider responsible for providing new implementations of {@link NamingStrategy}.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public interface HashBuilderProvider {
  /**
   * @return the {@link HashBuilder} implementations to contribute. The key represents the alias.
   */
  Map<String, HashBuilder> provideHashBuilders();
}
