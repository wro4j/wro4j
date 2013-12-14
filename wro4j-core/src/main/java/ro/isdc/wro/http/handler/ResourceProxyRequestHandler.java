package ro.isdc.wro.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.support.ContentTypeResolver;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.http.support.ResponseHeadersConfigurer;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


/**
 * Provides access to wro resources via a resource proxy.
 *
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class ResourceProxyRequestHandler
    extends RequestHandlerSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceProxyRequestHandler.class);

  public static final String PARAM_RESOURCE_ID = "id";
  public static final String PATH_RESOURCES = "wroResources";
  /**
   * The alias of this {@link RequestHandler} used for configuration.
   */
  public static final String ALIAS = "resourceProxy";

  @Inject
  private UriLocatorFactory locatorFactory;

  @Inject
  private ReadOnlyContext context;

  @Inject
  private ResourceAuthorizationManager authManager;
  private ResponseHeadersConfigurer headersConfigurer;
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    verifyAccess(request, response);
    serveProxyResourceUri(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final HttpServletRequest request) {
    return StringUtils.contains(request.getRequestURI(), PATH_RESOURCES);
  }

  private void serveProxyResourceUri(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    final String resourceUri = getResourceUri(request);
    LOG.debug("[OK] serving proxy resource: {}", resourceUri);
    final OutputStream outputStream = response.getOutputStream();
    response.setContentType(ContentTypeResolver.get(resourceUri, context.getConfig().getEncoding()));

    if (isResourceChanged(request)) {
      // set expiry headers
      getHeadersConfigurer().setHeaders(response);
      InputStream is = null;
      try {
        is = new AutoCloseInputStream(locatorFactory.locate(resourceUri));
        final int length = IOUtils.copy(is, outputStream);
        // servlet engine may ignore this if content body is flushed to client
        response.setContentLength(length);
        response.setStatus(HttpServletResponse.SC_OK);
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
  }

  /**
   * Used to identify whether the {@link HttpServletResponse#SC_NOT_MODIFIED} or {@link HttpServletResponse#SC_OK}
   * should be returned. Currently a single timestamp is used to detect the change for all resources. This might be no
   * accurate, but at least it allows sending NOT_MODIFIED header much often resulting in less load on the server.
   * <p/>
   * Override this method if a different way detecting change is required.
   *
   * @return true if the requested resource is changed on the server and the latest version should be returned.
   */
  protected boolean isResourceChanged(final HttpServletRequest request) {
    try {
      final long ifModifiedSince = request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString());
      return ifModifiedSince < getHeadersConfigurer().getLastModifiedTimestamp();
    } catch (final Exception e) {
      LOG.warn("Could not extract IF_MODIFIED_SINCE header for request: " + request.getRequestURI() + ". Assuming content is changed. ", e);
      return true;
    }
  }

  private void verifyAccess(final HttpServletRequest request, final HttpServletResponse response) {
    final String resourceUri = getResourceUri(request);
    if (!authManager.isAuthorized(resourceUri)) {
      LOG.debug("[FAIL] Unauthorized proxy resource: {}", resourceUri);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new UnauthorizedRequestException("Unauthorized resource request detected: " + resourceUri);
    }
  }

  private String getResourceUri(final HttpServletRequest request) {
    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    return resourceUri;
  }

  private final ResponseHeadersConfigurer getHeadersConfigurer() {
    if (headersConfigurer == null) {
      headersConfigurer = newResponseHeadersConfigurer();
    }
    return headersConfigurer;
  }

  /**
   * @return the {@link ResponseHeadersConfigurer}.
   */
  protected ResponseHeadersConfigurer newResponseHeadersConfigurer() {
    return ResponseHeadersConfigurer.fromConfig(context.getConfig());
  }

  /**
   * @param request
   *          {@link HttpServletRequest} for a wro request. The assumption is that {@link WroFilter} has a mapping
   *          ending with a <code>*</code> character.
   * @return the path for this handler.
   */
  public static String createHandlerPath(final String requestUri) {
    return createProxyPath(requestUri, "");
  }
  
  public static String createProxyPath(final String requestUri, final String resourceId) {
    Validate.notNull(resourceId);
    return FilenameUtils.getFullPath(requestUri) + getProxyResourcePath() + resourceId;
  }
  
  /**
   * Checks if the provided url is a resource proxy request.
   * @param url
   *          to check.
   * @return true if the provided url is a proxy resource.
   */
  public static boolean isProxyUrl(final String url) {
    Validate.notNull(url);
    return url.contains(getProxyResourcePath());
  }
  
  private static String getProxyResourcePath() {
    return String.format("%s?%s=", PATH_RESOURCES, PARAM_RESOURCE_ID);
  }
}