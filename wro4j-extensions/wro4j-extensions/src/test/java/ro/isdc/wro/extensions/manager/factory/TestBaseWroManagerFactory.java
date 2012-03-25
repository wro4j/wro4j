/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.extensions.manager.factory;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.extensions.model.factory.SmartWroModelFactory;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;

/**
 * @author Alex Objelean
 */
public class TestBaseWroManagerFactory {

  @Test
  public void defaultModelFactoryIsXml() {
    new BaseWroManagerFactory() {
      @Override
      protected WroModelFactory newModelFactory() {
        final WroModelFactory modelFactory = super.newModelFactory();
        Assert.assertEquals(SmartWroModelFactory.class, modelFactory.getClass());
        return modelFactory;
      }
    };
  }
}
