/*
 * Copyright (C) 2010 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.config;

/**
 * Holds configuration for the entire application. Accessing this object is safe even outside of a request cycle.
 *
 * @author Alex Objelean
 */
public class ApplicationContext {
	/**
	 * Settings associated with current context.
	 */
	private ApplicationSettings settings;
  /**
   * Singleton instance.
   */
  private static ApplicationContext INSTANCE;


  /**
   * Cannot instantiate this class. It is singleton.
   */
	private ApplicationContext() {
	  //set default settings.
	  settings = new ApplicationSettings();
	}

	/**
	 * @return the settings
	 */
	public ApplicationSettings getApplicationSettings() {
		return settings;
	}


	/**
	 * @param settings the settings to set
	 */
	public void setSettings(final ApplicationSettings settings) {
		this.settings = settings;
	}

	public static ApplicationContext get() {
		if (INSTANCE == null) {
		  INSTANCE = new ApplicationContext();
		}
		return INSTANCE;
	}

}
