/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;


/**
 * Factory responsible for creation of {@link WroModel} object.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public interface WroModelFactory {
  /**
   * Creates a {@link WroModel} object. The concrete implementation must
   * synchronize the instantiation of the model.
   *
   * @return an instance of {@link WroModel}.
   */
  WroModel getInstance();
}
