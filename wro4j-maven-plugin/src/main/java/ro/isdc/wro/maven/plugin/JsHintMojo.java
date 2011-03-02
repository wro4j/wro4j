/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.plugin.MojoExecutionException;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
import ro.isdc.wro.extensions.processor.js.JsHintPreProcessor;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro.xml.
 *
 * @goal jshint
 * @phase process-resources
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 */
public class JsHintMojo extends AbstractWro4jMojo {
  /**
   * Comma separated jsHint options. This field is optional. If no value is provided, no options will be used..
   *
   * @parameter expression="${options}"
   * @optional
   */
  private String options;
  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute()
    throws Exception {
    final Collection<String> groupsAsList = getTargetGroupsAsList();
    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
        processGroup(groupWithExtension);
      }
    }
  }

  /**
   * @param group
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
    getManagerFactory().getInstance().process();

    getLog().info("Success processing group: " + group);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected StandaloneContextAwareManagerFactory newWroManagerFactory()
    throws MojoExecutionException {
    return new DefaultStandaloneContextAwareManagerFactory() {
      @Override
      protected void configureProcessors(final GroupsProcessor groupsProcessor) {
        final ResourcePreProcessor processor = new JsHintPreProcessor() {
          @Override
          protected void onJsHintException(final JsHintException e, final Resource resource) throws Exception {
            getLog().error("Errors found while processing resource: " + resource + " Errors are: " + e.getErrors());
            throw new MojoExecutionException("Errors found when validating resource: " + resource);
          };
        }.setOptions(getOptions());
        groupsProcessor.addPreProcessor(processor);
      }
    };
  }


  /**
   * @return an array of options.
   */
  private String[] getOptions() {
    return options == null ? new String[] {} : options.split(",");
  }

  /**
   * @param options the options to set
   */
  public void setOptions(final String options) {
    this.options = options;
  }
}
