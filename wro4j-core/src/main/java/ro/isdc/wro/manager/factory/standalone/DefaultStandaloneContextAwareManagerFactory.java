package ro.isdc.wro.manager.factory.standalone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupExtractorDecorator;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.FileSystemResourceLocator;
import ro.isdc.wro.model.resource.processor.factory.DefaultProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;

/**
 * {@link WroManagerFactory} instance used by the maven plugin.
 *
 * @author Alex Objelean
 */
public class DefaultStandaloneContextAwareManagerFactory
  extends StandaloneWroManagerFactory implements StandaloneContextAware {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultStandaloneContextAwareManagerFactory.class);
  /**
   * Context used by stand-alone process.
   */
  StandaloneContext standaloneContext;
  /**
   * {@inheritDoc}
   */
  public void initialize(final StandaloneContext standaloneContext) {
    Validate.notNull(standaloneContext);
    this.standaloneContext = standaloneContext;
    //This is important in order to make plugin aware about ignoreMissingResources option.
    Context.get().getConfig().setIgnoreMissingResources(standaloneContext.isIgnoreMissingResources());
    LOG.debug("initialize: {}", standaloneContext);
    LOG.debug("config: {}", Context.get().getConfig());
  }


  @Override
  protected GroupExtractor newGroupExtractor() {
    return new GroupExtractorDecorator(super.newGroupExtractor()) {
      @Override
      public boolean isMinimized(final HttpServletRequest request) {
        return standaloneContext.isMinimize();
      }
    };
  }


  @Override
  protected WroModelFactory newModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return new FileSystemResourceLocator(standaloneContext.getWroFile());
      }
    };
  }

  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcessorsFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceLocatorFactory newLocatorFactory() {
    return DefaultResourceLocatorFactory.standaloneFactory(standaloneContext);
  }
}