package ro.isdc.wro.manager.factory.standalone;

import ro.isdc.wro.manager.factory.InjectableWroManagerFactoryDecorator;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * Created to overcome the difference between {@link WroManagerFactory} and {@link StandaloneContextAwareManagerFactory}
 * regarding the injection ability. This class will be removed once the {@link StandaloneContextAwareManagerFactory}
 * will be removed and another approach will be found to handle the difference.
 * 
 * @author Alex Objelean
 * @created 23 Jun 2012
 * @since 1.4.7
 */
public class InjectableContextAwareManagerFactory extends InjectableWroManagerFactoryDecorator implements StandaloneContextAwareManagerFactory {
  public InjectableContextAwareManagerFactory(final StandaloneContextAwareManagerFactory decorated) {
    super(decorated);
  }

  private StandaloneContextAwareManagerFactory getDecoratedFactory() {
    return (StandaloneContextAwareManagerFactory) getDecoratedObject();
  }
  
  /**
   * {@inheritDoc}
   */
  public void setProcessorsFactory(final ProcessorsFactory processorsFactory) {
    getDecoratedFactory().setProcessorsFactory(processorsFactory);
  }
  
  /**
   * {@inheritDoc}
   */
  public void initialize(StandaloneContext standaloneContext) {
    getDecoratedFactory().initialize(standaloneContext);
  }
}