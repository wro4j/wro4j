/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.manager.factory.standalone;

import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * An implementation of {@link WroManagerFactory} aware about the run context.<br/>
 * TODO: find a way to not require {@link StandaloneContextAwareManagerFactory} for build time processing, but use any
 * instance of {@link WroManagerFactory}.
 *
 * @author Alex Objelean
 * @deprecated use {@link StandaloneContextAware}
 */
@Deprecated
public interface StandaloneContextAwareManagerFactory extends StandaloneContextAware {
}
