package ro.isdc.wro.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
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
import jakarta.servlet.http.HttpServletResponseWrapper;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.http.support.RedirectedStreamServletResponseWrapper;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * A filter responsible for gzipping all content served through this filter.
 *
 * @author Alex Objelean
 * @since 1.7.0
 */
public class GzipFilter
    implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(GzipFilter.class);

  /**
   * {@inheritDoc}
   */
  public void init(final FilterConfig filterConfig)
      throws ServletException {
  }

  /**
   * {@inheritDoc}
   */
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (isGzipAllowed(request)) {
      doGzipResponse(request, response, chain);
    } else {
      LOG.debug("Gzip not allowed. Proceeding with chain.");
      chain.doFilter(request, response);
    }
  }

  /**
   * Performs actual gzip of the filtered content.
   */
  private void doGzipResponse(final HttpServletRequest req, final HttpServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    LOG.debug("Applying gzip on resource: {}", req.getRequestURI());
    response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final CountingOutputStream countingStream = new CountingOutputStream(new GZIPOutputStream(new BufferedOutputStream(
        baos)));
    // final GZIPOutputStream gzout = new GZIPOutputStream(new BufferedOutputStream(baos));
    // Perform gzip operation in-memory before sending response
    final HttpServletResponseWrapper wrappedResponse = new RedirectedStreamServletResponseWrapper(countingStream,
        response);
    chain.doFilter(req, wrappedResponse);
    // close underlying stream
    countingStream.close();
    response.setContentLength(countingStream.getCount());
    // avoid NO CONTENT error thrown by jetty when gzipping empty response
    if (countingStream.getCount() > 0) {
      IOUtils.write(baos.toByteArray(), response.getOutputStream());
    }
  }

  /**
   * Checks if the request supports gzip and is not a include request (these cannot be gzipped)
   */
  private boolean isGzipAllowed(final HttpServletRequest request) {
    return !DispatcherStreamLocator.isIncludedRequest(request) && WroUtil.isGzipSupported(request);
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }

}
