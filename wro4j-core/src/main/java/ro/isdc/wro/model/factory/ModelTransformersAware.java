package ro.isdc.wro.model.factory;

import java.util.Collection;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.Transformer;


/**
 * Used by classes capable of holding model transformers.
 * 
 * @author Alex Objelean
 * @created 25 Apr 2012
 * @since 1.4.6
 */
public interface ModelTransformersAware {
  /**
   * @return a collection of model transformers.
   */
  Collection<? extends Transformer<WroModel>> getModelTransformers(); 
}
