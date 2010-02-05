/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.config;

/**
 * Holds configuration instance. Accessing this object is safe even outside of a request cycle.
 *
 * @author Alex Objelean
 */
public class ConfigurationContext {
	/**
	 * Configuration instance.
	 */
	private WroConfiguration config;
  /**
   * Singleton instance.
   */
  private static ConfigurationContext INSTANCE;


  /**
   * Cannot instantiate this class. It is singleton.
   */
	private ConfigurationContext() {
	  //set default settings.
	  config = new WroConfiguration();
	}

	/**
	 * @return the settings
	 */
	public WroConfiguration getApplicationSettings() {
		return config;
	}


	/**
	 * @param settings the settings to set
	 */
	public void setConfig(final WroConfiguration settings) {
		this.config = settings;
	}

	/**
	 * @return an instance of {@link ConfigurationContext} singleton object.
	 */
	public static ConfigurationContext get() {
		if (INSTANCE == null) {
		  INSTANCE = new ConfigurationContext();
		}
		return INSTANCE;
	}
}
