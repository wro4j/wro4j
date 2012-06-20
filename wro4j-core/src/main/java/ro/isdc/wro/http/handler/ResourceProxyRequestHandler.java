package ro.isdc.wro.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.support.ContentTypeResolver;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

/**
 * Provides access to wro resources via a resource proxy.
 *
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public class ResourceProxyRequestHandler implements RequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceProxyRequestHandler.class);

  public static final String PARAM_RESOURCE_ID = "id";
  public static final String PATH_RESOURCES = "wroResources";
  
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  //TODO: remove when AuthorizedResourcesHolder exists.
  public static boolean hasAccess = true;

  /**
   * {@inheritDoc}
   */
  public void handle(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {

    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    verifyAccess(resourceUri, response);
    handleVerifiedRequestURI(resourceUri, response);

  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(final HttpServletRequest request) {
    return StringUtils.contains(request.getRequestURI(), PATH_RESOURCES);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return true;
  }

  private void handleVerifiedRequestURI(final String resourceUri, final HttpServletResponse response)
      throws IOException {
    LOG.debug("locating stream for resourceId: {}", resourceUri);
    final InputStream is = uriLocatorFactory.locate(resourceUri);
    final OutputStream outputStream = response.getOutputStream();

    int length = IOUtils.copy(is, outputStream);
    response.setContentLength(length);
    response.setContentType(ContentTypeResolver.get(resourceUri));
    response.setStatus(HttpServletResponse.SC_OK);

    IOUtils.closeQuietly(outputStream);
    IOUtils.closeQuietly(is);
  }

  /**
   * TODO: use new AuthorizedResourcesHolder to check acccess to resourceUri
   * Verifies that the user has access or not to the requested resource
   */
  private void verifyAccess(final String resourceUri, final HttpServletResponse response) {
    if(!hasAccess) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      throw new UnauthorizedRequestException("Unauthorized resource request detected: " + resourceUri);
    }
  }
}