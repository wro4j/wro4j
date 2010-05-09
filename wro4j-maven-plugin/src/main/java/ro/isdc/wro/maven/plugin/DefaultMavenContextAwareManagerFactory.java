package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.StandaloneWroManagerFactory;
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

/**
 * {@link WroManagerFactory} instance used by the maven plugin.
 *
 * @author Alex Objelean
 */
public class DefaultMavenContextAwareManagerFactory
  extends StandaloneWroManagerFactory implements MavenContextAwareManagerFactory {
  /**
   * Holds the properties of the run mojo.
   */
  private RunContext runContext;
  /**
   * {@link HttpServletRequest} associated with current request processing cycle.
   */
  private HttpServletRequest request;

  /**
   * Initialize the ManagerFactory with required properties.
   *
   * @param runContext
   * @param request
   */
  public void initialize(final RunContext runContext, final HttpServletRequest request) {
    this.runContext = runContext;
    this.request = request;
  }


  @Override
  protected void onBeforeCreate() {
    Context.set(Context.standaloneContext(request));
  }


  @Override
  protected GroupExtractor newGroupExtractor() {
    return new GroupExtractorDecorator(super.newGroupExtractor()) {
      @Override
      public boolean isMinimized(final HttpServletRequest request) {
        return runContext.isMinimize();
      }
    };
  }


  @Override
  protected WroModelFactory newModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        return new FileInputStream(runContext.getWroFile());
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupsProcessor = super.newGroupsProcessor();
    configureProcessors(groupsProcessor);
    return groupsProcessor;
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
        final String uriWithoutPrefix = uri.replaceFirst(PREFIX, "");
        return new FileInputStream(new File(runContext.getContextFolder(), uriWithoutPrefix));
      }
    };
  }
}