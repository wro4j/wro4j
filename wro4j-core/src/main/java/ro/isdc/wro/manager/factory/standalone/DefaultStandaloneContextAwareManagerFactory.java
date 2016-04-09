package ro.isdc.wro.manager.factory.standalone;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupExtractorDecorator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.StandaloneServletContextUriLocator;
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
  private StandaloneContext standaloneContext;

  public void initialize(final StandaloneContext standaloneContext) {
    notNull(standaloneContext);
    this.standaloneContext = standaloneContext;
    //Override the ignoreMissingResources flag only when explicitly set
    if (standaloneContext.getIgnoreMissingResourcesAsString() != null) {
      Context.get().getConfig().setIgnoreMissingResources(Boolean.parseBoolean(standaloneContext.getIgnoreMissingResourcesAsString()));
    }

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
      protected InputStream getModelResourceAsStream()
        throws IOException {
        return new FileInputStream(standaloneContext.getWroFile());
      }
    };
  }

  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcessorsFactory();
  }

  @Override
  protected ServletContextUriLocator newServletContextUriLocator() {
    final StandaloneServletContextUriLocator locator = new StandaloneServletContextUriLocator();
    locator.initialize(standaloneContext);
    return locator;
  }

  protected final StandaloneContext getStandaloneContext() {
    return standaloneContext;
  }
}