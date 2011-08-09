/**
 * Copyright@2011 wor4j
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.AbstractResourceLocator;


/**
 * When used, this factory will make it possible to migrate easily from xml to groovy (or json) DSL seamlessly.
 *
 * @author Alex Objelean
 * @created 6 Aug 2011
 * @since 1.4.0
 */
public class SmartWroModelFactory
  implements WroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(SmartWroModelFactory.class);
  /**
   * The default location of the wro model file.
   */
  private static final String DEFAULT_WRO_FILE = "/src/main/webapp/WEB-INF/wro.xml";

  private List<WroModelFactory> factoryList;
  /**
   * The exact file where the model is located.
   */
  private File wroFile;
  /**
   * flag indicating if the wroFile should be autodetected.
   */
  private boolean autoDetectWroFile;


  /**
   * Use this factory method when you want to use the {@link SmartWroModelFactory} in standalone (maven plugin) context.
   */
  public static SmartWroModelFactory createFromStandaloneContext(final StandaloneContext context) {
    Validate.notNull(context);
    final boolean autoDetectWroFile = FilenameUtils.normalize(context.getWroFile().getPath()).contains(
      FilenameUtils.normalize(DEFAULT_WRO_FILE));
    return new SmartWroModelFactory().setWroFile(context.getWroFile()).setAutoDetectWroFile(autoDetectWroFile);
  }


  /**
   * The file to use for creating model. It is not required to set this field, but if you set, do not set a null object.
   *
   * @param wroFile the wroFile to set
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
    final List<WroModelFactory> factoryList = new ArrayList<WroModelFactory>();
    LOG.info("autoDetect wroFile: " + autoDetectWroFile);
    factoryList.add(newXmlModelFactory());
    factoryList.add(newGroovyModelFactory());
    factoryList.add(newJsonModelFactory());
    return factoryList;
  }


  private XmlModelFactory newXmlModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return createAutoDetectingResourceLocator(super.getModelResourceLocator(), XmlModelFactory.DEFAULT_FILE_NAME);
      }
    };
  }


  private GroovyWroModelFactory newGroovyModelFactory() {
    return new GroovyWroModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return createAutoDetectingResourceLocator(super.getModelResourceLocator(), GroovyWroModelFactory.DEFAULT_FILE_NAME);
      }
    };
  }


  private JsonModelFactory newJsonModelFactory() {
    return new JsonModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return createAutoDetectingResourceLocator(super.getModelResourceLocator(), JsonModelFactory.DEFAULT_FILE_NAME);
      };
    };
  };

  /**
   * Creates a {@link ResourceLocator} which is handles the autoDetection logic.
   */
  private ResourceLocator createAutoDetectingResourceLocator(final ResourceLocator resourceLocator,
    final String defaultFileName) {
    if (wroFile == null) {
      return resourceLocator;
    }
    return new AbstractResourceLocator() {
      @Override
      public InputStream getInputStream()
        throws IOException {
        if (autoDetectWroFile) {
          final File file = new File(wroFile.getParentFile(), defaultFileName);
          LOG.info("loading autodetected wro file: " + file);
          return new FileInputStream(file);
        }
        LOG.info("loading wroFile: " + wroFile);
        return new FileInputStream(wroFile);
      }
    };
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    if (factoryList == null) {
      factoryList = newWroModelFactoryFactoryList();
    }
    for (final WroModelFactory factory : factoryList) {
      try {
        final Class<? extends WroModelFactory> factoryClass = factory.getClass().asSubclass(WroModelFactory.class);
        LOG.info("Trying to use {} for model creation", getClassName(factoryClass));
        return factory.create();
      } catch (final WroRuntimeException e) {
        LOG.info("Model creation using {} failed. Trying another ...", getClassName(factory.getClass()));
        LOG.debug("Exception occured while building the model using: " + getClassName(factory.getClass()), e);
      }
    }
    throw new WroRuntimeException("Cannot create model using any of provided factories");
  }


  /**
   * @return string representation of the factory name.
   */
  protected String getClassName(final Class<? extends WroModelFactory> factoryClass) {
    final String className = factoryClass.isAnonymousClass()
      ? factoryClass.getSuperclass().getSimpleName()
      : factoryClass.getSimpleName();
    return className;
  }


  /**
   * @param factoryList the factoryList to set
   */
  public SmartWroModelFactory setFactoryList(final List<WroModelFactory> factoryList) {
    Validate.notNull(factoryList);
    this.factoryList = factoryList;
    return this;
  }


  /**
   * In order to keep backward compatibility for building the model . The idea is if this field is false, then the exact
   * file will be used to create the model, otherwise, wro model file is autodetected based on the parent folder where
   * the wroFile is located.
   *
   * @param autoDetectWroFile the autoDetectWroFile to set
   */
  public SmartWroModelFactory setAutoDetectWroFile(final boolean autoDetectWroFile) {
    this.autoDetectWroFile = autoDetectWroFile;
    return this;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {}

}
