/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.AbstractWroModelFactory;
import ro.isdc.wro.util.StopWatch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * <p>TODO: validate duplicate groups and null resource type</p>
 *
 * <p>Creates {@link WroModel} from a json.</p>
 *
 * <p>This class is thread-safe because it doesn't have any state.</p>
 *
 * @author Alex Objelean
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
   * Alias for this model factory used by provider.
   */
  public static final String ALIAS = "json";

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized WroModel create() {
    final StopWatch stopWatch = new StopWatch("Create Wro Model from Groovy");
    try {
      stopWatch.start("createModel");
      final Type type = new TypeToken<WroModel>() {}.getType();
      final InputStream is = getModelResourceAsStream();
      if (is == null) {
        throw new WroRuntimeException("Invalid model stream provided!");
      }
      final WroModel model = new Gson().fromJson(new InputStreamReader(new AutoCloseInputStream(is)), type);
      LOG.debug("json model: {}", model);
      if (model == null) {
        throw new WroRuntimeException("Invalid content provided, cannot build model!");
      }
      return model;
    } catch (final Exception e) {
      throw new WroRuntimeException("Invalid model found!", e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
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
