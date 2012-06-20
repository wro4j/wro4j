package ro.isdc.wro.model.resource.support;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;


/**
 * Controls the resources which should be accessible through {@link ResourceProxyRequestHandler}. Prevent the security
 * issue which may occur when everything can be accessed using the proxy.
 * 
 * @author Alex Objelean
 */
public class ResourceAuthorizationManager {
  /**
   * @param uri
   *          of the resource to authorize.
   * @return true if the provided uri is an authorized resource.
   */
  public boolean isAuthorized(final String uri) {
      return true;
  }
}
