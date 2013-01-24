package ro.isdc.wro.manager.factory.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupExtractorDecorator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.factory.DefaultProcesorsFactory;
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
      protected InputStream getModelResourceAsStream()
        throws IOException {
        return new FileInputStream(standaloneContext.getWroFile());
      }
    };
  }

  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcesorsFactory();
  }

  @Override
  protected ServletContextUriLocator newServletContextUriLocator() {
    return new ServletContextUriLocator() {
      @Override
      public InputStream locate(final String uri)
        throws IOException {
        //TODO this is duplicated code (from super) -> find a way to reuse it.
        if (getWildcardStreamLocator().hasWildcard(uri)) {
          final String fullPath = FilenameUtils.getFullPath(uri);
          final String realPath = standaloneContext.getContextFolder().getPath() + fullPath;
          return getWildcardStreamLocator().locateStream(uri, new File(realPath));
        }

        final String uriWithoutPrefix = uri.replaceFirst(PREFIX, "");
        final File file = new File(standaloneContext.getContextFolder(), uriWithoutPrefix);
        LOG.debug("Opening file: " + file.getPath());
        return new FileInputStream(file);
      }
    };
  }
}