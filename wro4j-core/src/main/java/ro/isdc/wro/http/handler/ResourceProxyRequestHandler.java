package ro.isdc.wro.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.support.ContentTypeResolver;
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
    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    verifyAccess(resourceUri, response);
    serveProxyResourceUri(resourceUri, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final HttpServletRequest request) {
    return StringUtils.contains(request.getRequestURI(), PATH_RESOURCES);
  }

  private void serveProxyResourceUri(final String resourceUri, final HttpServletResponse response)
      throws IOException {
    LOG.debug("[OK] serving proxy resource: {}", resourceUri);
    final OutputStream outputStream = response.getOutputStream();
    response.setContentType(ContentTypeResolver.get(resourceUri, context.getConfig().getEncoding()));

    // set expiry headers
    getHeadersConfigurer().setHeaders(response);

    response.setStatus(HttpServletResponse.SC_OK);
    InputStream is = null;
    try {
      is = new AutoCloseInputStream(locatorFactory.locate(resourceUri));
      final int length = IOUtils.copy(is, outputStream);
      // servlet engine may ignore this if content body is flushed to client
      response.setContentLength(length);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(outputStream);
    }
  }

  private void verifyAccess(final String resourceUri, final HttpServletResponse response) {
    if (!authManager.isAuthorized(resourceUri)) {
      LOG.debug("[FAIL] Unauthorized proxy resource: {}", resourceUri);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new UnauthorizedRequestException("Unauthorized resource request detected: " + resourceUri);
    }
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
}