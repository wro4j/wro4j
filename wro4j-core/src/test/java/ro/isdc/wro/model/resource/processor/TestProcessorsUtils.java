/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.decorator.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.MinimizeAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorsUtils;


/**
 * @author Alex Objelean
 */
public class TestProcessorsUtils {

  private static class AnyTypeProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
    public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {}
    public void process(final Reader reader, final Writer writer)
      throws IOException {}
  }

  @Minimize
  private static class MinimizeAwareProcessor extends AnyTypeProcessor {
  }


  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullArgumentWhenFilteringProcessorsToApply() {
    ProcessorsUtils.filterProcessorsToApply(true, null, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetProcessorsByTypeWithEmptyCollection() {
    final Collection<ResourcePreProcessor> input = Collections.EMPTY_LIST;
    final Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS,
      input);
    assertEquals(0, output.size());
  }

  @Test
  public void testGetProcessorsByType1() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      new JSMinProcessor(), new CssMinProcessor()
    });
    Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(1, output.size());
    output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(1, output.size());
  }


  @Test
  public void testGetProcessorsByType2() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      new CssMinProcessor(), new AnyTypeProcessor()
    });
    Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(2, output.size());
    output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(1, output.size());
  }
  
  @Test(expected = NullPointerException.class)
  public void shouldNotFilterProcessorsWhenResourseTypeIsNull() {
    ProcessorsUtils.filterProcessorsToApply(true, null, Arrays.asList(new JSMinProcessor()));
  }

  @Test
  public void testGetProcessorsByTypeWithDecorator1() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new JSMinProcessor())
    });
    Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(0, output.size());
    output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(1, output.size());
  }

  @Test
  public void testGetProcessorsByTypeWithDecorator2() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new CssMinProcessor())
    });
    Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(1, output.size());
    output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(0, output.size());
  }


  @Test
  public void testGetProcessorsByTypeWithDecorator3() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new AnyTypeProcessor()),
      CopyrightKeeperProcessorDecorator.decorate(new JSMinProcessor())
    });
    Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(1, output.size());
    output = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(2, output.size());
  }

  /**
   * The purpose of the test is to check if the decorated processors are identified correctly as being {@link MinimizeAware}.
   */
  @Test
  public void shouldIdentifyCorrectlyMinimizeAwareProcessors() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new CssMinProcessor()),
      new JSMinProcessor()
    });
    final Collection<ResourcePreProcessor> output = ProcessorsUtils.filterProcessorsToApply(false, ResourceType.JS, input);
    assertEquals(0, output.size());
  }

  @Test
  public void shouldFilterProcessors() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      CopyrightKeeperProcessorDecorator.decorate(new AnyTypeProcessor()),
      new CssMinProcessor(), new MinimizeAwareProcessor(), new JSMinProcessor()
    });
    Collection<ResourcePreProcessor> actual = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.JS, input);
    assertEquals(3, actual.size());

    actual = ProcessorsUtils.filterProcessorsToApply(false, ResourceType.JS, input);
    assertEquals(1, actual.size());

    actual = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input);
    assertEquals(3, actual.size());

    actual = ProcessorsUtils.filterProcessorsToApply(false, ResourceType.CSS, input);
    assertEquals(1, actual.size());
  }

  @Test
  public void shouldPreserveOrderOfTheFilteredProcessors() {
    final Collection<ResourcePreProcessor> input = Arrays.asList(new ResourcePreProcessor[] {
      new JSMinProcessor(),
        new AnyTypeProcessor(), new CssMinProcessor(), new MinimizeAwareProcessor(),
        CopyrightKeeperProcessorDecorator.decorate(new CssImportPreProcessor()) });

    final Iterator<ResourcePreProcessor> iterator = ProcessorsUtils.filterProcessorsToApply(true, ResourceType.CSS, input).iterator();
    assertEquals(AnyTypeProcessor.class, iterator.next().getClass());
    assertEquals(CssMinProcessor.class, iterator.next().getClass());
    assertEquals(MinimizeAwareProcessor.class, iterator.next().getClass());
    assertEquals(CopyrightKeeperProcessorDecorator.class, iterator.next().getClass());
  }
}
