package ro.isdc.wro.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
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

  private final FileTypeMap fileTypeMap = FileTypeMap.getDefaultFileTypeMap();

  /**
   * {@inheritDoc}
   */
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    final String resourceUri = request.getParameter(PARAM_RESOURCE_ID);
    if(!isAccessible(resourceUri)) {
      denyRequest(resourceUri);
    }

    OutputStream outputStream = response.getOutputStream();
    LOG.debug("locating stream for resourceId: {}", resourceUri);
    final InputStream is = uriLocatorFactory.locate(resourceUri);
    // TODO remove. This check is not required, since the factory will throw IOException anyway.
    if (is == null) {
      throw new WroRuntimeException("Cannot process request with uri: " + request.getRequestURI());
    }

    int length = IOUtils.copy(is, outputStream);
    
    //set the content length & type before the stream is closed
    response.setContentLength(length);
    response.setContentType(getContentType(resourceUri));
    response.setStatus(HttpServletResponse.SC_OK);

    IOUtils.closeQuietly(outputStream);
    IOUtils.closeQuietly(is);
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

  private void denyRequest(final String resourceUri) {
    throw new UnauthorizedRequestException("Unauthorized resource request detected: " + resourceUri);
  }

  /**
   * TODO: use new AuthorizedResourcesHolder to check acccess to resourceUri
   * Verifies that the user has access or not to the requested resource
   *
   * @param resourceUri of the resource to be proxied.
   * @return true if the uri can be accessed.
   */
  private boolean isAccessible(final String resourceUri) {
    return false;
  }

  /**
   * TODO move contentType detection to a separate class.
   */
  private String getContentType(final String resourceUri) {
    if (resourceUri.toLowerCase().endsWith(".css")) {
      return "text/css";
    } else if (resourceUri.toLowerCase().endsWith(".js")) {
      return "application/javascript";
    } else if (resourceUri.toLowerCase().endsWith(".png")) {
      return "image/png";
    } else {
      return fileTypeMap.getContentType(resourceUri);
    }
  }

}