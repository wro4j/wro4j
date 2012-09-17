package ro.isdc.wro.model.resource.processor.decorator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;
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
  
  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAcceptInvalidProcessor() {
    new ProcessorDecorator(new Object());
  }
  
  @Test
  public void shouldCreateHelperWithPreProcessor() {
    new ProcessorDecorator(new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
      }
    });
  }
  
  @Test
  public void shouldCreateHelperWithPostProcessor() {
    new ProcessorDecorator(new ResourcePostProcessor() {
      public void process(final Reader reader, final Writer writer)
          throws IOException {
      }
    });
  }

  @Test
  public void shouldPreserveProcessorMetadataAfterTransform() {
    final ResourcePostProcessor postProcessor = new JSMinProcessor();
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
      public void process(final Resource resource, final Reader reader, final Writer writer)
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
    final ResourcePreProcessor processor = new ProcessorDecorator(new JSMinProcessor()) {
      @Override
      protected boolean isMinimizeInternal() {
        return false;
      }
    };
    assertFalse(new ProcessorDecorator(processor).isMinimize());
  }
  
  @Test
  public void shouldChangeSupportedTypesWhenInternalMethodIsOverriden() {
    final ResourcePreProcessor processor = new ProcessorDecorator(new JSMinProcessor()) {
      @Override
      protected SupportedResourceType getSupportedResourceTypeInternal() {
        return null;
      }
    };
    assertNull(null, new ProcessorDecorator(processor).getSupportedResourceType());
  }
  
  @Test
  public void shouldComputeIsMinimizeFlagOfDeepNestedDecoratedProcessor() {
    final ProcessorDecorator processor = new ProcessorDecorator(new ProcessorDecorator(new ProcessorDecorator(new JSMinProcessor())));
    assertTrue(processor.isMinimize());
  }
  
  private static class SupportAwareProcessor extends JSMinProcessor implements SupportAware {
   public boolean isSupported() {
      return true;
    } 
  }
  
  @Test
  public void shouldIdentifyProcessorSupport() {
    final SupportAwareProcessor supportAwareProcessor = mock(SupportAwareProcessor.class);
    ProcessorDecorator decorator = new ProcessorDecorator(supportAwareProcessor);
    
    when(supportAwareProcessor.isSupported()).thenReturn(true);
    assertEquals(true, decorator.isSupported());
    
    when(supportAwareProcessor.isSupported()).thenReturn(false);
    assertEquals(false, decorator.isSupported());
  }
}
