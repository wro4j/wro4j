package ro.isdc.wro.http.handler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

  public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    if(!hasAccessToResource(resourceUri)) {
      accessDeniedResponse(resourceUri, response);
    }

    OutputStream outputStream = response.getOutputStream();
    LOG.debug("locating stream for resourceId: {}", resourceUri);
    final InputStream is = uriLocatorFactory.locate(resourceUri);
    if (is == null) {
      throw new WroRuntimeException("Cannot process request with uri: " + request.getRequestURI());
    }
    IOUtils.copy(is, outputStream);
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(outputStream);
  }

  /**
   * {@inheritDoc}
   */
  public boolean accept(HttpServletRequest request) {
    return StringUtils.contains(request.getRequestURI(), PATH_RESOURCES);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {
    return true;
  }

  private void accessDeniedResponse(String resourceUri, HttpServletResponse response) {
    throw new UnauthorizedRequestException("Unauthorized resource request detected! " + resourceUri);
  }

  /**
   * TODO: use new AuthorizedResourcesHolder to check acccess to resourceUri
   * Verifies that the user has access or not to the requested resource
   *
   * @param resourceUri of the resource to be proxied.
   * @return
   */
  private boolean hasAccessToResource(String resourceUri) {
    return true;
  }

}