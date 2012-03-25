/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.AbstractWroModelFactory;

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
  extends AbstractWroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JsonModelFactory.class);
  /**
   * Default name of the file used to retrieve the model.
   */
  private static final String DEFAULT_FILE_NAME = "wro.json";

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    try {
      final Type type = new TypeToken<WroModel>() {}.getType();
      final InputStream is = getModelResourceAsStream();
      if (is == null) {
        throw new WroRuntimeException("Invalid model stream provided!");
      }
      final WroModel model = new Gson().fromJson(new InputStreamReader(getModelResourceAsStream()), type);
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
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultModelFilename() {
    return DEFAULT_FILE_NAME;
  }
}
