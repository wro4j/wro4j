/*
* Copyright 2011 Wro4J
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.isdc.wro.extensions.model.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * Test {@link GroovyModelFactory}
 *
 * @author Romain Philibert
 * @created 19 Jul 2011
 */
public class TestGroovyModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestGroovyModelFactory.class);
  private GroovyModelFactory factory;


  @Test(expected = WroRuntimeException.class)
  public void testInvalidStream() throws Exception {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        throw new IOException();
      }
    };
    factory.create();
  }

  @Test
  public void createValidModel() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return TestGroovyModelFactory.class.getResourceAsStream("wro.groovy");
      };
    };
    final WroModel model = factory.create();
    Assert.assertNotNull(model);
    Assert.assertEquals(Arrays.asList("g2", "g1"), model.getGroupNames());
    Assert.assertEquals(2, model.getGroupByName("g1").getResources().size());
    Assert.assertTrue(model.getGroupByName("g1").getResources().get(0).isMinimize());
    Assert.assertEquals("/static/app.js", model.getGroupByName("g1").getResources().get(0).getUri());
    Assert.assertEquals(ResourceType.JS, model.getGroupByName("g1").getResources().get(0).getType());
    Assert.assertTrue(model.getGroupByName("g1").getResources().get(1).isMinimize());
    Assert.assertEquals("/static/app.css", model.getGroupByName("g1").getResources().get(1).getUri());
    Assert.assertEquals(ResourceType.CSS, model.getGroupByName("g1").getResources().get(1).getType());
    Assert.assertEquals(2, model.getGroupByName("g2").getResources().size());
    Assert.assertFalse(model.getGroupByName("g2").getResources().get(1).isMinimize());

    LOG.debug("model: ", model);
  }

  @Test
  public void createValidModelContainingHiphen() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return getClass().getResourceAsStream("wroWithHiphen.groovy");
      }
    };
    final WroModel model = factory.create();
    Assert.assertNotNull(model.getGroupByName("group-with-hiphen"));
  }

  @Test
  public void createGroupReferenceOrderShouldNotMatter() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return getClass().getResourceAsStream("wroGroupRefOrder.groovy");
      }
    };
    Assert.assertNotNull(factory.create());
  }

  @Test(expected=RecursiveGroupDefinitionException.class)
  public void testRecursiveGroupReference() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return getClass().getResourceAsStream("wroRecursiveReference.groovy");
      }
    };
    factory.create();
  }

  @Test(expected=WroRuntimeException.class)
  public void testDuplicateGroupName() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return getClass().getResourceAsStream("wroDuplicateGroupName.groovy");
      }
    };
    factory.create();
  }

  /**
   * Test the usecase when the resource has no URI.
   */
  @Test(expected = WroRuntimeException.class)
  public void createIncompleteModel() {
    factory = new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return getClass().getResourceAsStream("IncompleteWro.groovy");
      }
    };
    factory.create();
  }
}
