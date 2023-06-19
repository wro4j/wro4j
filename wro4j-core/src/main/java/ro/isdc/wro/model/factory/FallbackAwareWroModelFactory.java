/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;


/**
 * An {@link XmlModelFactory} that handles the situation when the wro model cannot be loaded at some point (resource
 * cannot be located or the model is invalid). It holds the last known good model and reuse it until a new valid
 * instance of model is available.
 *
 * @author Alex Objelean
 */
public class FallbackAwareWroModelFactory extends WroModelFactoryDecorator {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FallbackAwareWroModelFactory.class);
  /**
   * Last valid model instance..
   */
  private WroModel lastValidModel;


  /**
   * @param decorated {@link WroModelFactory} to decorated.
   */
  public FallbackAwareWroModelFactory(final WroModelFactory decorated) {
    super(decorated);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    WroModel newModel = null;
    try {
      newModel = super.create();
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
