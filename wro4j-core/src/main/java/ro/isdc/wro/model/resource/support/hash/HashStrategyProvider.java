/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.support.hash;

import java.util.Map;

/**
 * A service provider responsible for providing new implementations of {@link HashStrategy}.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public interface HashStrategyProvider {
  /**
   * @return the {@link HashStrategy} implementations to contribute. The key represents the alias.
   */
  Map<String, HashStrategy> provideHashStrategies();
}
