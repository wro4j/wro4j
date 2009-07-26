/**
 *
 */
package ro.isdc.wro.http;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.util.Configuration;

/**
 * Holds the properties related to a request cycle.
 * @author Alex Objelean
 */
public class Context {
  /**
   * Thread local holding CURRENT context.
   */
  private static final ThreadLocal<Context> CURRENT = new ThreadLocal<Context>();

  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode
   * is used.
   */
  private static final String PARAM_CONFIGURATION = "configuration";

  /**
   * Gzip resources param option.
   */
  private static final String PARAM_GZIP_RESOURCES = "gzipResources";

  /**
   * Request.
   */
  private final HttpServletRequest request;
  /**
   * Response.
   */
  private final HttpServletResponse response;
  /**
   * ServletContext.
   */
  private final ServletContext servletContext;
  /**
   * FilterConfig.
   */
  private final FilterConfig filterConfig;
  /**
   * @return {@link Context} associated with CURRENT request cycle.
   */
  public static Context get() {
    final Context context = CURRENT.get();
    if (context == null) {
      throw new WroRuntimeException("No context associated with CURRENT request cycle!");
    }
    return context;
  }

  /**
   * Associate a context with the CURRENT request cycle.
   *
   * @param context {@link Context} to set.
   */
  public static void set(final Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be NULL!");
    }
    CURRENT.set(context);
  }

  /**
   * Remove context from the local thread.
   */
  public static void unset() {
    CURRENT.set(null);
  }

  /**
   * Constructor.
   */
  public Context(final HttpServletRequest request, final HttpServletResponse response, final FilterConfig filterConfig) {
    this.request = request;
    this.response = response;
    this.servletContext = filterConfig.getServletContext();
    this.filterConfig = filterConfig;
  }

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return this.request;
  }

  /**
   * @return the response
   */
  public HttpServletResponse getResponse() {
    return this.response;
  }

  /**
   * @return the servletContext
   */
  public ServletContext getServletContext() {
    return this.servletContext;
  }

  /**
   * @return the filterConfig
   */
  public FilterConfig getFilterConfig() {
    return this.filterConfig;
  }

  /**
   * @return true if debug parameter is present (this means that DEBUG or DEVELOPMENT mode is used).
   */
  public final boolean isDevelopmentMode() {
    //TODO read initParam from filter config?
    final String debugParam = getRequest().getParameter("debug");
    if (debugParam != null) {
      return Boolean.parseBoolean(debugParam);
    }
    //TODO deprecate config using filter config?
    final String configParam = this.filterConfig.getInitParameter(PARAM_CONFIGURATION);
    //TODO get rid of Configuration enum & simplify this logic
    final Configuration config = configParam == null
      ? Configuration.DEVELOPMENT
      : Configuration.valueOf(configParam.toUpperCase());
    return config.isDevelopment();
  }

  /**
   * Decision method for gziping resources.
   *
   * @return true if requested resources should be gziped.
   */
  public final boolean isGzipEnabled() {
    boolean gzipResources = true;
    final HttpServletRequest request = Context.get().getRequest();
    final String toGzipAsString = request.getParameter("gzip");
    if (toGzipAsString != null) {
      gzipResources = Boolean.valueOf(toGzipAsString);
    } else {
      final String gzipParam = this.filterConfig.getInitParameter(PARAM_GZIP_RESOURCES);
      gzipResources = gzipParam == null ? true : Boolean.valueOf(gzipParam);
    }
    return acceptsEncoding(request, "gzip") && gzipResources;
  }

  /**
   * TODO: move to WroUtil?
   * Checks if request accepts the named encoding.
   */
  private boolean acceptsEncoding(final HttpServletRequest request,
      final String name) {
    final boolean accepts = headerContains(request, "Accept-Encoding", name);
    return accepts;
  }

  /**
   * TODO: move to WroUtil?
   * Checks if request contains the header value.
   */
  private boolean headerContains(final HttpServletRequest request,
      final String header, final String value) {
    final Enumeration<String> accepted = request.getHeaders(header);
    while (accepted.hasMoreElements()) {
      final String headerValue = accepted.nextElement();
      if (headerValue.indexOf(value) != -1) {
        return true;
      }
    }
    return false;
  }

}
