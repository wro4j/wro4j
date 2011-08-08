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

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;


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
    LOG.debug("wroFile: " + wroFile);
    factoryList.add(new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          if (autoDetectWroFile) {
            return new FileInputStream(new File(wroFile.getParentFile(), XmlModelFactory.DEFAULT_FILE_NAME));
          }
          return new FileInputStream(wroFile);
        }
        return super.getModelResourceAsStream();
      }
    });
    factoryList.add(new GroovyWroModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          if (autoDetectWroFile) {
            return new FileInputStream(new File(wroFile.getParentFile(), GroovyWroModelFactory.DEFAULT_FILE_NAME));
          }
          return new FileInputStream(wroFile);
        }
        return super.getModelResourceAsStream();
      }
    });
    factoryList.add(new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          if (autoDetectWroFile) {
            return new FileInputStream(new File(wroFile.getParentFile(), JsonModelFactory.DEFAULT_FILE_NAME));
          }
          return new FileInputStream(wroFile);
        }
        return super.getModelResourceAsStream();
      }
    });
    return factoryList;
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
        LOG.debug("Exception occured while building the model using :" + getClassName(factory.getClass()), e);
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
