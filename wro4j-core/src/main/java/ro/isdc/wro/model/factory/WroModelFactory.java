/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;


/**
 * Factory responsible for creation of {@link WroModel} object.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface WroModelFactory {
  /**
   * Creates a {@link WroModel} object. The concrete implementation must synchronize the instantiation of the model.
   *
   * @return an instance of {@link WroModel}.
   * @throws WroRuntimeException if model cannot be created.
   */
  WroModel getInstance();

  /**
   * Called to indicate that the factory is being taken out of service.
   */
  void destroy();
}
