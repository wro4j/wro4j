/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.config.factory;

import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * Factory responsible for {@link WroConfiguration} object creation.
 *
 * @author Alex Objelean
 * @created 10 May 2011
 * @since 1.3.7
 */
public interface WroConfigurationFactory {
  WroConfiguration create();
}
