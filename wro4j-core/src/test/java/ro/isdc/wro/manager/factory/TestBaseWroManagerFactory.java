/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.manager.factory;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;

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
        Assert.assertEquals(XmlModelFactory.class, modelFactory.getClass());
        return modelFactory;
      }
    };
  }
}
