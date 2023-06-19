/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.extensions.model.factory;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.AbstractWroModelFactory;
import ro.isdc.wro.model.factory.LazyWroModelFactoryDecorator;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.WroUtil;


/**
 * When used, this factory will make it possible to migrate easily from xml to groovy (or json) DSL seamlessly.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public class SmartWroModelFactory
    extends AbstractWroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(SmartWroModelFactory.class);
  /**
   * The default location of the wro model file.
   */
  private static final String DEFAULT_WRO_FILE = "/src/main/webapp/WEB-INF/wro.xml";
  /**
   * Alias for this model factory used by provider.
   */
  public static final String ALIAS = "smart";
  @Inject
  private Injector injector;
  private List<WroModelFactory> factoryList;
  /**
   * The exact file where the model is located.
   */
  private File wroFile;
  /**
   * flag indicating if the wroFile should be auto detected.
   */
  private boolean autoDetectWroFile = true;

  /**
   * Use this factory method when you want to use the {@link SmartWroModelFactory} in standalone (maven plugin) context.
   * The autoDetect flag is set to true if the wroFile path is the same as the default wro file name.
   */
  public static SmartWroModelFactory createFromStandaloneContext(final StandaloneContext context) {
    notNull(context);
    final boolean autoDetectWroFile = WroUtil.normalize(context.getWroFile().getPath()).contains(
        WroUtil.normalize(DEFAULT_WRO_FILE));
    if (!autoDetectWroFile) {
      LOG.debug("autoDetect is " + autoDetectWroFile + " because wroFile: " + context.getWroFile()
          + " is not the same as the default one: " + DEFAULT_WRO_FILE);
    }
    return new SmartWroModelFactory().setWroFile(context.getWroFile()).setAutoDetectWroFile(autoDetectWroFile);
  }

  /**
   * The file to use for creating model. It is not required to set this field, but if you set, do not set a null object.
   *
   * @param wroFile
   *          the wroFile to set
   */
  public SmartWroModelFactory setWroFile(final File wroFile) {
    Validate.notNull(wroFile);
    this.wroFile = wroFile;
    return this;
  }

  /**
   * @return default list of factories to be used by {@link SmartWroModelFactory}.
   */
  protected List<WroModelFactory> newWroModelFactoryFactoryList() {
    final List<WroModelFactory> list = new ArrayList<WroModelFactory>();
    LOG.debug("auto detect wroFile: {}", autoDetectWroFile);
    list.add(newXmlModelFactory());
    list.add(newGroovyModelFactory());
    list.add(newJsonModelFactory());
    return list;
  }

  private XmlModelFactory newXmlModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        if (wroFile == null) {
          return super.getModelResourceAsStream();
        }
        return createAutoDetectedStream(getDefaultModelFilename());
      }
    };
  }

  private WroModelFactory newGroovyModelFactory() {
    return new LazyWroModelFactoryDecorator(new LazyInitializer<WroModelFactory>() {
      @Override
      protected WroModelFactory initialize() {
        final WroModelFactory modelFactory = new GroovyModelFactory() {
          @Override
          protected InputStream getModelResourceAsStream()
              throws IOException {
            if (wroFile == null) {
              return super.getModelResourceAsStream();
            }
            return createAutoDetectedStream(getDefaultModelFilename());
          }
        };
        injector.inject(modelFactory);
        return modelFactory;
      }
    });
  }

  private WroModelFactory newJsonModelFactory() {
    return new LazyWroModelFactoryDecorator(new LazyInitializer<WroModelFactory>() {
      @Override
      protected WroModelFactory initialize() {
        final WroModelFactory modelFactory = new JsonModelFactory() {
          @Override
          protected InputStream getModelResourceAsStream()
              throws IOException {
            if (wroFile == null) {
              return super.getModelResourceAsStream();
            }
            return createAutoDetectedStream(getDefaultModelFilename());
          }
        };
        injector.inject(modelFactory);
        return modelFactory;
      }
    });
  }

  /**
   * Handles the resource model auto detection.
   */
  private InputStream createAutoDetectedStream(final String defaultFileName)
      throws IOException {
    try {
      Validate.notNull(wroFile, "Cannot call this method if wroFile is null!");
      if (autoDetectWroFile) {
        final File file = new File(wroFile.getParentFile(), defaultFileName);
        LOG.debug("\tloading autodetected wro file: " + file);
        return new FileInputStream(file);
      }
      LOG.debug("loading wroFile: " + wroFile);
      return new FileInputStream(wroFile);
    } catch (final FileNotFoundException e) {
      // When auto detect is turned on, do not skip trying.. because the auto detection assume that the wro file name
      // can be wrong.
      if (autoDetectWroFile) {
        throw e;
      }
      throw new WroRuntimeException("The wroFile doesn't exist. Skip trying with other wro model factories", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    if (factoryList == null) {
      factoryList = newWroModelFactoryFactoryList();
    }
    if (factoryList != null) {
      // Holds the details about model creation which are logged only when no model can be created
      final StringBuffer logMessageBuffer = new StringBuffer();
      for (final WroModelFactory factory : factoryList) {
        try {
          // use injector for aggregated modelFactories
          injector.inject(factory);
          final Class<? extends WroModelFactory> factoryClass = factory.getClass().asSubclass(WroModelFactory.class);
          logMessageBuffer.append(" Using " + getClassName(factoryClass) + " for model creation..\n");
          return factory.create();
        } catch (final WroRuntimeException e) {
          LOG.debug("[FAIL] creating model... will try another factory: {}", e.getCause());
          logMessageBuffer.append("[FAIL] Model creation using " + getClassName(factory.getClass())
              + " failed. Trying another ...\n");
          logMessageBuffer.append("[FAIL] Exception occured while building the model using: "
              + getClassName(factory.getClass()) + " " + e.getMessage());
          // stop trying with other factories if the reason is IOException
          if (!autoDetectWroFile && e.getCause() instanceof IOException) {
            throw e;
          }
        }
      }
      LOG.error(logMessageBuffer.toString());
    }
    throw new WroRuntimeException("Cannot create model using any of provided factories");
  }

  /**
   * @return string representation of the factory name.
   */
  protected String getClassName(final Class<? extends WroModelFactory> factoryClass) {
    return factoryClass.isAnonymousClass() ? factoryClass.getSuperclass().getSimpleName()
        : factoryClass.getSimpleName();
  }

  /**
   * @param factoryList
   *          the factoryList to set
   */
  public SmartWroModelFactory setFactoryList(final List<WroModelFactory> factoryList) {
    Validate.notNull(factoryList);
    this.factoryList = factoryList;
    return this;
  }

  /**
   * In order to keep backward compatibility for building the model . The idea is if this field is false, then the exact
   * file will be used to create the model, otherwise, wro model file is auto-detected based on the parent folder where
   * the wroFile is located.
   *
   * @param autoDetectWroFile
   *          the autoDetectWroFile to set
   */
  public SmartWroModelFactory setAutoDetectWroFile(final boolean autoDetectWroFile) {
    this.autoDetectWroFile = autoDetectWroFile;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultModelFilename() {
    return DEFAULT_WRO_FILE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected InputStream getModelResourceAsStream()
      throws IOException {
    throw new IllegalStateException("This method should never be called!");
  }
}
