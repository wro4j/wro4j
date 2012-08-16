package ro.isdc.wro.http.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.ContentTypeResolver;
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
  
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  @Inject
  private WroConfiguration config;
  
  @Inject
  private ResourceAuthorizationManager authManager;

  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    verifyAccess(resourceUri, response);
    serverProxyResourceUri(resourceUri, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final HttpServletRequest request) {
    return StringUtils.contains(request.getRequestURI(), PATH_RESOURCES);
  }

  private void serverProxyResourceUri(final String resourceUri, final HttpServletResponse response)
      throws IOException {
    LOG.debug("[OK] serving proxy resource: {}", resourceUri);
    final OutputStream outputStream = response.getOutputStream();
    
    response.setContentType(ContentTypeResolver.get(resourceUri, config.getEncoding()));
    int length = IOUtils.copy(new AutoCloseInputStream(uriLocatorFactory.locate(resourceUri)), outputStream);
    response.setContentLength(length);
    response.setStatus(HttpServletResponse.SC_OK);
    
    IOUtils.closeQuietly(outputStream);
  }

  /**
   * TODO: use new AuthorizedResourcesHolder to check acccess to resourceUri Verifies that the user has access or not to
   * the requested resource
   */
  private void verifyAccess(final String resourceUri, final HttpServletResponse response) {
    if (!authManager.isAuthorized(resourceUri)) {
      LOG.debug("[FAIL] Unauthorized proxy resource: {}", resourceUri);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new UnauthorizedRequestException("Unauthorized resource request detected: " + resourceUri);
    }
  }
}