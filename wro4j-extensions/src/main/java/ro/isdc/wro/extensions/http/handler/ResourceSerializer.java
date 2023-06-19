package ro.isdc.wro.extensions.http.handler;

import java.lang.reflect.Type;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;
import ro.isdc.wro.model.resource.Resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializes {@link Resource} to json. It transforms the resources to a jsonElement which also provide a proxyUri
 * which the user can use do download the specific resource. If the resource is external
 * (eg. http://www.site.com/style.css) no proxyUri is provided.
 *
 * Example JSON produced by this serializer:
 * {
 *    "type": "CSS",
 *    "uri": "/static/css/style.css",
 *    "proxyUri": "/wro/wroResources?id\u003d/static/css/style.css"
 * }
 *
 * @author Ivar Conradi Østhus
 * @since 1.4.7
 */
public class ResourceSerializer implements JsonSerializer<Resource> {
  private final String basePath;

  public ResourceSerializer(final String requestURI) {
    this.basePath = requestURI;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonElement serialize(final Resource resource, final Type type, final JsonSerializationContext context) {
    final JsonObject jsonObject = new JsonObject();
    final String uri = resource.getUri();
    jsonObject.add("type", new JsonPrimitive(resource.getType().toString()));
    jsonObject.add("minimize", new JsonPrimitive(resource.isMinimize()));
    jsonObject.add("uri", new JsonPrimitive(uri));
    jsonObject.add("proxyUri", new JsonPrimitive(getExternalUri(uri)));
    return jsonObject;
  }

  private String getExternalUri(final String uri) {
    return isExternal(uri) ? uri : ResourceProxyRequestHandler.createProxyPath(basePath, uri);
  }

  private boolean isExternal(final String uri) {
    return uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://");
  }
}
