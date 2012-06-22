package ro.isdc.wro.extensions.http.handler;

import com.google.gson.*;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;

import java.lang.reflect.Type;

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
 * @created 15 Jun 2012
 * @since 1.4.7
 */
public class ResourceSerializer implements JsonSerializer<Resource> {
  private final String basePath;

  public ResourceSerializer(String requestURI) {
    this.basePath = requestURI;
  }

  @Override
  public JsonElement serialize(Resource resource, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonObject jsonObject = new JsonObject();
    String uri = resource.getUri();
    jsonObject.add("type", new JsonPrimitive(resource.getType().toString()));
    jsonObject.add("uri", new JsonPrimitive(uri));
    jsonObject.add("proxyUri", new JsonPrimitive(getExternalUri(uri)));
    return jsonObject;
  }

  private String getExternalUri(String uri) {
    if(isExternal(uri)) {
      return uri;
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(basePath);
    stringBuilder.append(CssUrlRewritingProcessor.PATH_RESOURCES);
    stringBuilder.append("?id=");
    stringBuilder.append(uri);
    return stringBuilder.toString();
  }

  private boolean isExternal(String uri) {
    return uri.toLowerCase().startsWith("http://");
  }
}
