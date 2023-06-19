package ro.isdc.wro.http.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;


/**
 * Allows configuration of a list of processors to be applied on the
 *
 * @author Alex Objelean
 * @since 1.4.5
 */
public abstract class AbstractProcessorsFilter
    implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessorsFilter.class);
  private FilterConfig filterConfig;

  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
      throws ServletException {
    this.filterConfig = config;
    doInit(config);
  }

  /**
   * Allows custom initialization.
   */
  protected void doInit(final FilterConfig config) {
  }

  /**
   * {@inheritDoc}
   */
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    try {
      // add request, response & servletContext to thread local
      Context.set(Context.webContext(request, response, filterConfig));
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      final HttpServletResponse wrappedResponse = new RedirectedStreamServletResponseWrapper(os, response);
      chain.doFilter(request, wrappedResponse);
      final Reader reader = new StringReader(new String(os.toByteArray(), Context.get().getConfig().getEncoding()));

      final StringWriter writer = new StringWriter();
      final String requestUri = request.getRequestURI().replaceFirst(request.getContextPath(), "");
      doProcess(requestUri, reader, writer);
      // it is important to update the contentLength to new value, otherwise the transfer can be closed without all
      // bytes being read. Some browsers (chrome) complains with the following message: ERR_CONNECTION_CLOSED
      final int contentLength = writer.getBuffer().length();
      response.setContentLength(contentLength);
      // Content length can be 0 when the 30x (not modified) status code is returned.
      if (contentLength > 0) {
        IOUtils.write(writer.toString(), response.getOutputStream(), Charset.defaultCharset());
      }
    } catch (final RuntimeException e) {
      onRuntimeException(e, response, chain);
    } finally {
      Context.unset();
    }
  }

  /**
   * Applies configured processor on the intercepted stream.
   */
  private void doProcess(final String requestUri, final Reader reader, final Writer writer)
      throws IOException {
    Reader input = reader;
    Writer output = null;
    LOG.debug("processing resource: {}", requestUri);
    try {
      final StopWatch stopWatch = new StopWatch();
      final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
      final List<ResourcePreProcessor> processors = getProcessorsList();
      if (processors == null || processors.isEmpty()) {
        IOUtils.copy(reader, writer);
      } else {
        for (final ResourcePreProcessor processor : processors) {
          stopWatch.start("Using " + processor.getClass().getSimpleName());
          // inject all required properties
          injector.inject(processor);

          output = new StringWriter();
          LOG.debug("Using {} processor", processor);
          processor.process(createResource(requestUri), input, output);

          input = new StringReader(output.toString());
          stopWatch.stop();
        }
        LOG.debug(stopWatch.prettyPrint());
        if (output != null) {
          writer.write(output.toString());
        }
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * @param requestUri
   *          the uri of the requested resource.
   * @return the {@link Resource} to pass as argument to the processor.
   */
  protected Resource createResource(final String requestUri) {
    return Resource.create(requestUri);
  }

  /**
   * Invoked when a {@link RuntimeException} is thrown. Allows custom exception handling. The default implementation
   * redirects to 404 for a specific {@link WroRuntimeException} exception when in DEPLOYMENT mode.
   *
   * @param e
   *          {@link RuntimeException} thrown during request processing.
   */
  protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response,
      final FilterChain chain) {
    LOG.debug("RuntimeException occured", e);
    try {
      LOG.debug("Cannot process. Proceeding with chain execution.");
      chain.doFilter(Context.get().getRequest(), response);
    } catch (final Exception ex) {
      // should never happen
      LOG.error("Error while chaining the request.");
    }
  }

  /**
   * @return a list of processors to apply for this filter.
   */
  protected abstract List<ResourcePreProcessor> getProcessorsList();

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }
}
