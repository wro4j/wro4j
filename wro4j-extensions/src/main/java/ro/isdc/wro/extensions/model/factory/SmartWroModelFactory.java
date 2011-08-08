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
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;


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
   * A little story about wroFile & wroParentFolder fields: these both were introduced as a result of backward
   * compatibility required by wro4j-maven-plugin for building the model. The idea is if wroFile is provided, then the
   * exact file will be used to create the model with all modelFactories provided by {@link SmartWroModelFactory}. The
   * next wroParentFolder is used and wro model files are searched inside that folder. If this is not provided, the
   * default lookup is performed.
   * <p/>
   * The exact file where the model is located.
   */
  private File wroFile;
  /**
   * The folder where the wro model file is searched.
   */
  private File wroParentFolder;

  /**
   * Allow client code to control the location of the wro model file for all available factories.
   *
   * @param wroParentFolder the wroParentFolder to set
   */
  public SmartWroModelFactory setWroParentFolder(final File wroParentFolder) {
    Validate.notNull(wroParentFolder);
    this.wroParentFolder = wroParentFolder;
    return this;
  }

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
    LOG.debug("wroParentFolder: " + wroParentFolder);
    factoryList.add(new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          return new FileInputStream(wroFile);
        }
        if (wroParentFolder != null) {
          return new FileInputStream(new File(wroParentFolder, XmlModelFactory.DEFAULT_FILE_NAME));
        }
        return super.getModelResourceAsStream();
      }
    });
    factoryList.add(new GroovyWroModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          return new FileInputStream(wroFile);
        }
        if (wroParentFolder != null) {
          return new FileInputStream(new File(wroParentFolder, GroovyWroModelFactory.DEFAULT_FILE_NAME));
        }
        return super.getModelResourceAsStream();
      }
    });
    factoryList.add(new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        if (wroFile != null) {
          return new FileInputStream(wroFile);
        }
        if (wroParentFolder != null) {
          return new FileInputStream(new File(wroParentFolder, JsonModelFactory.DEFAULT_FILE_NAME));
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
   * {@inheritDoc}
   */
  @Override
  public void destroy() {}

}
