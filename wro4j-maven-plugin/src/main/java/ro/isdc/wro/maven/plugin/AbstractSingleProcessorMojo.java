/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.WroManager.Builder;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactoryDecorator;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;


/**
 * Maven plugin which use a single processor.
 *
 * @author Alex Objelean
 */
public abstract class AbstractSingleProcessorMojo extends AbstractWro4jMojo {
  /**
   * Comma separated options. This field is optional. If no value is provided, no options will be used..
   *
   * @parameter expression="${options}"
   * @optional
   */
  private String options;
  /**
   * When true, all the plugin won't stop its execution and will log all found errors.
   *
   * @parameter default-value="false" expression="${failNever}"
   * @optional
   */
  private boolean failNever;

  /**
   * {@inheritDoc}
   */
  @Override
  public final void doExecute()
    throws Exception {
    getLog().info("options: " + options);
    getLog().info("failNever: " + failNever);

    final Collection<String> groupsAsList = getTargetGroupsAsList();
    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
        processGroup(groupWithExtension);
      }
    }
  }

  /**
   * @param group the name of the group to process.
   */
  private void processGroup(final String group) throws Exception {
    getLog().info("processing group: " + group);

    //mock request
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn(group);
    //mock response
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));

    //init context
    final WroConfiguration config = Context.get().getConfig();
    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);
    //perform processing
    getManagerFactory().create().process();

    getLog().debug("Processing group: " + group + " [OK]");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroManagerFactory getManagerFactory() {
    return new WroManagerFactoryDecorator(super.getManagerFactory()) {
      @Override
      protected void onBeforeBuild(final Builder builder) {
        builder.setProcessorsFactory(createSingleProcessorsFactory());
      }
    };
  }

  private ProcessorsFactory createSingleProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    final ResourcePreProcessor processor = createResourceProcessor();
    factory.addPreProcessor(processor);
    return factory;
  }

  protected abstract ResourcePreProcessor createResourceProcessor();

  /**
   * @return raw representation of the option value.
   */
  protected String getOptions() {
    return options;
  }

  /**
   * Used for tests only.
   * @param options the options to set
   */
  void setOptions(final String options) {
    this.options = options;
  }

  /**
   * @param failNever the failFast to set
   * @VisibleForTesting
   */
  void setFailNever(final boolean failNever) {
    this.failNever = failNever;
  }

  /**
   * @return the failNever
   * @VisibleForTesting
   */
  boolean isFailNever() {
    return failNever;
  }
}
