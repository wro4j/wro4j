/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Creates a {@link WroModel} object. The concrete implementation must synchronize the instantiation of the model.
 *
 * @author Alex Objelean
 */
public interface WroModelFactory extends ObjectFactory<WroModel> {
  /**
   * Called to indicate that the factory is being taken out of service.
   */
  void destroy();
}
