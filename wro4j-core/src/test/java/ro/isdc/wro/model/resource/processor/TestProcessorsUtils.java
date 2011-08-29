/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * @author Alex Objelean
 */
public class TestProcessorsUtils {

  private static class AnyTypeProcessor
    implements ResourceProcessor {
    public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {}
  }


  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullArgumentForGetProcessorsByType() {
    ProcessorsUtils.getProcessorsByType(null, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetProcessorsByTypeWithEmptyCollection() {
    final Collection<ResourceProcessor> input = Collections.EMPTY_LIST;
    final Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(0, output.size());
  }

  @Test
  public void testGetProcessorsByType1() {
    final Collection<ResourceProcessor> input = Arrays.asList(new ResourceProcessor[] {
      new JSMinProcessor(), new CssMinProcessor()
    });
    Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(ResourceType.JS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(null, input);
    Assert.assertEquals(0, output.size());
  }


  @Test
  public void testGetProcessorsByType2() {
    final Collection<ResourceProcessor> input = Arrays.asList(new ResourceProcessor[] {
      new CssMinProcessor(), new AnyTypeProcessor()
    });
    Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(2, output.size());
    output = ProcessorsUtils.getProcessorsByType(ResourceType.JS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(null, input);
    Assert.assertEquals(1, output.size());
  }


  @Test
  public void testGetProcessorsByTypeWithDecorator1() {
    final Collection<ResourceProcessor> input = Arrays.asList(new ResourceProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new JSMinProcessor())
    });
    Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(0, output.size());
    output = ProcessorsUtils.getProcessorsByType(ResourceType.JS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(null, input);
    Assert.assertEquals(0, output.size());
  }

  @Test
  public void testGetProcessorsByTypeWithDecorator2() {
    final Collection<ResourceProcessor> input = Arrays.asList(new ResourceProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new CssMinProcessor())
    });
    Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(ResourceType.JS, input);
    Assert.assertEquals(0, output.size());
    output = ProcessorsUtils.getProcessorsByType(null, input);
    Assert.assertEquals(0, output.size());
  }


  @Test
  public void testGetProcessorsByTypeWithDecorator3() {
    final Collection<ResourceProcessor> input = Arrays.asList(new ResourceProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new AnyTypeProcessor()),
      CopyrightKeeperProcessorDecorator.decorate(new JSMinProcessor())
    });
    Collection<ResourceProcessor> output = ProcessorsUtils.getProcessorsByType(ResourceType.CSS, input);
    Assert.assertEquals(1, output.size());
    output = ProcessorsUtils.getProcessorsByType(ResourceType.JS, input);
    Assert.assertEquals(2, output.size());
    output = ProcessorsUtils.getProcessorsByType(null, input);
    Assert.assertEquals(1, output.size());
  }
}
