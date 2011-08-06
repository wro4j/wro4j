/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * TODO: validate duplicate groups & null resource type
 * <p/>
 * Creates {@link WroModel} from a json.
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 * @since 1.3.6
 */
public class JsonModelFactory
  implements WroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JsonModelFactory.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    try {
      final Type type = new TypeToken<WroModel>() {}.getType();
      final InputStream is = getConfigResourceAsStream();
      if (is == null) {
        throw new WroRuntimeException("Invalid model stream provided!");
      }
      final WroModel model = new Gson().fromJson(new InputStreamReader(getConfigResourceAsStream()), type);
      LOG.debug("json model: {}", model);
      if (model == null) {
        throw new WroRuntimeException("Invalid content provided, cannot build model!");
      }
      return model;
    } catch (final Exception e) {
      throw new WroRuntimeException("Invalid model found!", e);
    }
  }

  /**
   * Override this method, in order to provide different json model location.
   *
   * @return stream of the json representation of the model.
   * @throws IOException if the stream couldn't be read.
   */
  protected InputStream getConfigResourceAsStream() throws IOException {
    return Context.get().getServletContext().getResourceAsStream("/WEB-INF/wro.json");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {}

}
