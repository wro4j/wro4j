/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;

/**
 * An {@link XmlModelFactory} that handles the situation when the wro model cannot be loaded at some point
 * (resource cannot be located or the model is invalid). It holds the last known good model and reuse it until a new valid
 * instance of model is available.
 *
 * @author Alex Objelean
 */
public class FallbackAwareXmlModelFactory
  extends XmlModelFactory {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FallbackAwareXmlModelFactory.class);
  /**
   * Last valid model instance..
   */
  private WroModel lastValidModel;
  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized WroModel getInstance() {
    WroModel newModel = null;
    try {
      newModel = super.getInstance();
    } catch (final WroRuntimeException e) {
      LOG.error("Error while creating the model", e);
    }
    if (newModel == null) {
      LOG.warn("Couldn't load new model, reusing last Valid Model!");
      if (lastValidModel == null) {
        throw new WroRuntimeException("No valid model was found!");
      }
      return lastValidModel;
    }
    lastValidModel = newModel;
    return lastValidModel;
  }
}
