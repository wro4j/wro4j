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
	private static ThreadLocal<ApplicationContext> CURRENT = new ThreadLocal<ApplicationContext>();
	/**
	 * Settings associated with current context.
	 */
	private ApplicationSettings settings;


	/**
	 * @return the settings
	 */
	public ApplicationSettings getSettings() {
		return settings;
	}


	/**
	 * @param settings the settings to set
	 */
	public void setSettings(final ApplicationSettings settings) {
		this.settings = settings;
	}

	public static ApplicationContext get() {
		final ApplicationContext instance = CURRENT.get();
		if (instance == null) {

		}
		return instance;
	}

}
