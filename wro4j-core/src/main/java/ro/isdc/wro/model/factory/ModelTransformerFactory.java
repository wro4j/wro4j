/*
 * Copyright (C) 2011 wro4j.
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.Transformer;

/**
 * Applies transformation using supplied transformers to the decorated model.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public class ModelTransformerFactory
    extends WroModelFactoryDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ModelTransformerFactory.class);
  private List<? extends Transformer<WroModel>> modelTransformers = Collections.emptyList();
  @Inject
  private Injector injector;
  
  /**
   * Decorates a model factory.
   */
  public ModelTransformerFactory(final WroModelFactory decorated) {
    super(decorated);
  }

  /**
   * Set a list of transformers to apply on decorated model factory.
   */
  public ModelTransformerFactory setTransformers(final List<Transformer<WroModel>> modelTransformers) {
    Validate.notNull(modelTransformers);
    this.modelTransformers = modelTransformers;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    WroModel model = super.create();
    LOG.debug("using {} transformers", modelTransformers);
    for (final Transformer<WroModel> transformer : modelTransformers) {
      injector.inject(transformer);
      LOG.debug("using transformer: {}", transformer.getClass());
      try {
        model = transformer.transform(model);
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception during model transformation", e);
      }
    }
    return model;
  }
}
