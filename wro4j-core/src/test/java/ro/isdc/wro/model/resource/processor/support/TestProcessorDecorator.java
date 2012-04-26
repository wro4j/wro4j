package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

/**
 * @author Alex Objelean
 */
public class TestProcessorDecorator {
  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullProcessor() {
    new ProcessorDecorator(null);
  }

  @Test
  public void shouldDecorateAProcessor() {
    new ProcessorDecorator(new ResourceProcessor() {
      public void process(Resource resource, Reader reader, Writer writer)
          throws IOException {
      }
    });
  }

  @Test
  public void shouldPreserveProcessorMetadataAfterTransform() {
    final ResourceProcessor postProcessor = new JSMinProcessor();
    ProcessorDecorator decorator = new ProcessorDecorator(postProcessor);
    assertTrue(Arrays.equals(new ResourceType[] {ResourceType.JS}, decorator.getSupportedResourceTypes()));
    assertTrue(decorator.isMinimize());
  }
  
  @Test
  public void shouldComputeCorrectlySupportedResourceTypes() {
    assertTrue(Arrays.equals(new ResourceType[] {ResourceType.JS}, new ProcessorDecorator(new JSMinProcessor()).getSupportedResourceTypes()));
    assertTrue(Arrays.equals(new ResourceType[] {ResourceType.CSS}, new ProcessorDecorator(new CssMinProcessor()).getSupportedResourceTypes()));
    assertTrue(Arrays.equals(ResourceType.values(), new ProcessorDecorator(new CommentStripperProcessor()).getSupportedResourceTypes()));
  }
  
  @Test
  public void shouldDetectJsMinAsMinimizeAwareProcessor() {
    assertTrue(new ProcessorDecorator(new JSMinProcessor()).isMinimize());
  }
  
  @Test
  public void shouldDetectANonMinimizeAwareProcessor() {
    assertFalse(new ProcessorDecorator(new SemicolonAppenderPreProcessor()).isMinimize());
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullResourceTypeForIsEligible() {
    assertTrue(new ProcessorDecorator(new JSMinProcessor()).isEligible(true, null));
  }
  
  @Test
  public void shouldComputeEligibilityForMinimizeAwareProcessorWithJsType() {
    assertTrue(new ProcessorDecorator(new JSMinProcessor()).isEligible(true, ResourceType.JS));
    assertFalse(new ProcessorDecorator(new JSMinProcessor()).isEligible(true, ResourceType.CSS));
    assertFalse(new ProcessorDecorator(new JSMinProcessor()).isEligible(false, ResourceType.JS));
    assertFalse(new ProcessorDecorator(new JSMinProcessor()).isEligible(false, ResourceType.CSS));
  }

  @Test
  public void shouldComputeEligibilityForProcessorWithJsType() {
    assertTrue(new ProcessorDecorator(new SemicolonAppenderPreProcessor()).isEligible(true, ResourceType.JS));
    assertTrue(new ProcessorDecorator(new SemicolonAppenderPreProcessor()).isEligible(false, ResourceType.JS));
    assertFalse(new ProcessorDecorator(new SemicolonAppenderPreProcessor()).isEligible(true, ResourceType.CSS));
    assertFalse(new ProcessorDecorator(new SemicolonAppenderPreProcessor()).isEligible(false, ResourceType.CSS));
  }

  @Test
  public void shouldComputeEligibilityForProcessorWithCssType() {
    assertTrue(new ProcessorDecorator(new CssVariablesProcessor()).isEligible(true, ResourceType.CSS));
    assertTrue(new ProcessorDecorator(new CssVariablesProcessor()).isEligible(false, ResourceType.CSS));
    assertFalse(new ProcessorDecorator(new CssVariablesProcessor()).isEligible(true, ResourceType.JS));
    assertFalse(new ProcessorDecorator(new CssVariablesProcessor()).isEligible(false, ResourceType.JS));
  }
  

  @Test
  public void shouldComputeEligibilityForMinimizeAwareProcessorWithCssType() {
    assertTrue(new ProcessorDecorator(new CssMinProcessor()).isEligible(true, ResourceType.CSS));
    assertFalse(new ProcessorDecorator(new CssMinProcessor()).isEligible(false, ResourceType.CSS));
    assertFalse(new ProcessorDecorator(new CssMinProcessor()).isEligible(true, ResourceType.JS));
    assertFalse(new ProcessorDecorator(new CssMinProcessor()).isEligible(false, ResourceType.JS));
  }
  

  @Test
  public void shouldComputeEligibilityForProcessorWithNoTypeAndNotMinimizeAware() {
    ResourcePreProcessor noOpProcessor = new ResourcePreProcessor() {
      public void process(Resource resource, Reader reader, Writer writer)
          throws IOException {
      }
    };
    assertTrue(new ProcessorDecorator(noOpProcessor).isEligible(true, ResourceType.CSS));
    assertTrue(new ProcessorDecorator(noOpProcessor).isEligible(false, ResourceType.CSS));
    assertTrue(new ProcessorDecorator(noOpProcessor).isEligible(true, ResourceType.JS));
    assertTrue(new ProcessorDecorator(noOpProcessor).isEligible(false, ResourceType.JS));
  }
  
  @Test
  public void shouldChangeMinimizaFlagWhenInternalMethodIsOverriden() {
    ResourceProcessor processor = new ProcessorDecorator(new JSMinProcessor()) {
      @Override
      protected boolean isMinimizeInternal() {
        return false;
      }
    };
    assertFalse(new ProcessorDecorator(processor).isMinimize());
  }
  
  @Test
  public void shouldChangeSupportedTypesWhenInternalMethodIsOverriden() {
    ResourceProcessor processor = new ProcessorDecorator(new JSMinProcessor()) {
      @Override
      protected SupportedResourceType getSupportedResourceTypeInternal() {
        return null;
      }
    };
    assertNull(null, new ProcessorDecorator(processor).getSupportedResourceType());
  }
}
