package ro.isdc.wro.manager.factory.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupExtractorDecorator;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.model.resource.util.NamingStrategy;
import ro.isdc.wro.model.resource.util.NoOpNamingStrategy;

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
   * Rename the file name based on its original name and content.
   */
  private NamingStrategy namingStrategy;
  /**
   * {@inheritDoc}
   */
  public void initialize(final StandaloneContext standaloneContext) {
    this.standaloneContext = standaloneContext;
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
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        return new FileInputStream(standaloneContext.getWroFile());
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
    super.configureGroupsProcessor(groupsProcessor);
    configureProcessors(groupsProcessor);
    //This is important in order to make plugin aware about ignoreMissingResources option.
    groupsProcessor.setIgnoreMissingResources(standaloneContext.isIgnoreMissingResources());
  }

  /**
   * Configure the pre and post processors. Override this to specify your own processors.
   *
   * @param groupsProcessor
   */
  protected void configureProcessors(final GroupsProcessor groupsProcessor) {
    groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    groupsProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupsProcessor.addPreProcessor(new SemicolonAppenderPreProcessor());
    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
    groupsProcessor.addPostProcessor(new JSMinProcessor());
    groupsProcessor.addPostProcessor(new JawrCssMinifierProcessor());
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

        LOG.debug("locating uri: " + uri);
        final String uriWithoutPrefix = uri.replaceFirst(PREFIX, "");
        final File file = new File(standaloneContext.getContextFolder(), uriWithoutPrefix);
        LOG.debug("Opening file: " + file.getPath());
        return new FileInputStream(file);
      }
    };
  }


  /**
   * This method will never return null. If no NamingStrategy is set, a NoOp implementation will return.
   *
   * @return a not null {@link NamingStrategy}. If no {@link NamingStrategy} is set, a NoOp implementation will return.
   */
  public final NamingStrategy getNamingStrategy() {
    if (namingStrategy == null) {
      namingStrategy = new NoOpNamingStrategy();
    }
    return this.namingStrategy;
  }

  /**
   * @param namingStrategy the namingStrategy to set
   */
  public final void setNamingStrategy(final NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }
}