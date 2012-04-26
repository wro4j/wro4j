package ro.isdc.wro.model.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.WroModel;


/**
 * A factory which uses for creation model passed to constructor, holds it in memory and never destroys it. 
 * 
 * @author Alex Objelean
 */
public class SimpleWroModelFactory
    implements WroModelFactory {
  private WroModel model;
  
  public SimpleWroModelFactory(final WroModel model) {
    Validate.notNull(model);
    this.model = model;
  }
  
  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }
}
