/**
 * Copyright@2011 wor4j
 */
package ro.isdc.wro.extensions.model.factory;

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


  public SmartWroModelFactory() {
    factoryList = newWroModelFactoryFactoryList();
  }


  /**
   * @return default list of factories to be used by {@link SmartWroModelFactory}.
   */
  protected List<WroModelFactory> newWroModelFactoryFactoryList() {
    final List<WroModelFactory> factoryList = new ArrayList<WroModelFactory>();
    factoryList.add(new XmlModelFactory());
    factoryList.add(new GroovyWroModelFactory());
    factoryList.add(new JsonModelFactory());
    return factoryList;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    for (final WroModelFactory factory : factoryList) {
      try {
        final Class<? extends WroModelFactory> factoryClass = factory.getClass().asSubclass(WroModelFactory.class);
        LOG.info("Trying to use {} for model creation", getClassName(factoryClass));
        return factory.create();
      } catch (final WroRuntimeException e) {
        LOG.info("Model creation using {} failed. Trying another ...", getClassName(factory.getClass()));
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
