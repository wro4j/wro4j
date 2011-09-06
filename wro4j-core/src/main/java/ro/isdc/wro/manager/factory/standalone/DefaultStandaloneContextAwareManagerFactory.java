package ro.isdc.wro.manager.factory.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupExtractorDecorator;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.FileSystemResourceLocator;

/**
 * {@link WroManagerFactory} instance used by the maven plugin.
 *
 * @author Alex Objelean
 */
public class DefaultStandaloneContextAwareManagerFactory
  extends StandaloneWroManagerFactory implements StandaloneContextAwareManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultStandaloneContextAwareManagerFactory.class);
  /**
   * Context used by stand-alone process.
   */
  private StandaloneContext standaloneContext;
  /**
   * {@inheritDoc}
   */
  public void initialize(final StandaloneContext standaloneContext) {
    this.standaloneContext = standaloneContext;
    //This is important in order to make plugin aware about ignoreMissingResources option.
    Context.get().getConfig().setIgnoreMissingResources(standaloneContext.isIgnoreMissingResources());
    LOG.debug("initialize: " + standaloneContext);
    LOG.debug("config: " + Context.get().getConfig());
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


  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceLocatorFactory newResourceLocatorFactory() {
    return DefaultResourceLocatorFactory.standaloneFactory(standaloneContext.getContextFolder());
  }
}