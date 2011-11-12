/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.DuplicatesAwareCssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

/**
 * Contains divers utility methods applied on processors.
 *
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class ProcessorsUtils {
  /**
   * Returns a collection free of minimize aware processors (annotated with @Minimize).
   *
   * @param <T>
   *          type of processor
   * @param processors
   *          a collection of processors.
   */
  public static <T> Collection<T> getMinimizeFreeProcessors(final Collection<T> processors) {
    final Collection<T> result = new ArrayList<T>();
    for (final T processor : processors) {
      if (!processor.getClass().isAnnotationPresent(Minimize.class)) {
        result.add(processor);
      }
    }
    return result;
  }

  public static <T> boolean isMinimizeAwareProcessor(final T processor) {
    if (processor instanceof MinimizeAware) {
      return ((MinimizeAware)processor).isMinimize();
    }
    return processor.getClass().isAnnotationPresent(Minimize.class);
  }

  public static <T> SupportedResourceType getSupportedResourceType(final T processor) {
    SupportedResourceType supportedType = processor.getClass().getAnnotation(SupportedResourceType.class);
    /**
     * This is a special case for processors which implement {@link SupportedResourceTypeProvider} interface. This is
     * useful for decorator processors which needs to "inherit" the {@link SupportedResourceType} of the decorated
     * processor.
     */
    if (processor instanceof SupportedResourceTypeAware) {
      supportedType = ((SupportedResourceTypeAware) processor).getSupportedResourceType();
    }
    return supportedType;
  }

  /**
   * @param <T> processor type. Can be {@link ResourcePreProcessor}, {@link ResourcePostProcessor} or null (any).
   * @param type {@link ResourceType} to apply for searching on available processors.
   * @param availableProcessors a list where to perform the search.
   * @return a list of found processors which satisfy the search criteria. There are 3 possibilities:
   *        <ul>
   *          <li>If you search by null (any) type - you'll get only processors which can be applied on any resource (not any particular type)</li>
   *          <li>If you search by JS type - you'll get processors which can be applied on JS resources & any (null) resources </li>
   *          <li>If you search by CSS type - you'll get processors which can be applied on CSS resources & any (null) resources </li>
   *        </ul>
   */
  public static <T> Collection<T> getProcessorsByType(final ResourceType type, final Collection<T> availableProcessors) {
    Validate.notNull(availableProcessors);
    final Collection<T> found = new ArrayList<T>();
    for (final T processor : availableProcessors) {
      final SupportedResourceType supportedType = getSupportedResourceType(processor);
      final boolean isTypeSatisfied = supportedType == null || (supportedType != null && type == supportedType.value());
      if (isTypeSatisfied) {
        found.add(processor);
      }
    }
    return found;
  }


  /**
   * Transforms a preProcessor into a postProcessor.
   *
   * @param preProcessor {@link ResourcePreProcessor} to transform.
   */
  public static ResourcePostProcessor toPostProcessor(final ResourcePreProcessor preProcessor) {
    return new ResourcePostProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        preProcessor.process(null, reader, writer);
      }
    };
  }

  /**
   * Transforms a postProcessor into a preProcessor.
   *
   * @param postProcessor {@link ResourcePostProcessor} to transform.
   */
  public static ResourcePreProcessor toPreProcessor(final ResourcePostProcessor postProcessor) {
    return new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
        postProcessor.process(reader, writer);
      }
    };
  }

  /**
   * @return preProcessor of type processorClass if any found or null otherwise.
   */
  @SuppressWarnings("unchecked")
  public static final <T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass,
    final Collection<ResourcePreProcessor> preProcessors) {
    T found = null;
    for (final ResourcePreProcessor processor : preProcessors) {
      if (processorClass.isInstance(processor)) {
        found = (T)processor;
        return found;
      }
    }
    return null;
  }

  public static Map<String, ResourcePreProcessor> createPreProcessorsMap() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    populateProcessorsMap(map);
    return map;
  }


  public static Map<String, ResourcePostProcessor> createPostProcessorsMap() {
    final Map<String, ResourcePostProcessor> map = new HashMap<String, ResourcePostProcessor>();
    populateProcessorsMap(map);
    return map;
  }

  @SuppressWarnings("unchecked")
  private static <T> void populateProcessorsMap(final Map<String, T> map) {
    map.put(CssUrlRewritingProcessor.ALIAS, (T) new CssUrlRewritingProcessor());
    map.put(CssImportPreProcessor.ALIAS, (T) new CssImportPreProcessor());
    map.put(CssVariablesProcessor.ALIAS, (T) new CssVariablesProcessor());
    map.put(CssCompressorProcessor.ALIAS, (T) new CssCompressorProcessor());
    map.put(SemicolonAppenderPreProcessor.ALIAS, (T) new SemicolonAppenderPreProcessor());
    map.put(CssDataUriPreProcessor.ALIAS, (T) new CssDataUriPreProcessor());
    map.put(DuplicatesAwareCssDataUriPreProcessor.ALIAS_DUPLICATE, (T) new DuplicatesAwareCssDataUriPreProcessor());
    map.put(JawrCssMinifierProcessor.ALIAS, (T) new JawrCssMinifierProcessor());
    map.put(CssMinProcessor.ALIAS, (T) new CssMinProcessor());
    map.put(JSMinProcessor.ALIAS, (T) new JSMinProcessor());
    map.put(VariablizeColorsCssProcessor.ALIAS, (T) new VariablizeColorsCssProcessor());
    map.put(ConformColorsCssProcessor.ALIAS, (T) new ConformColorsCssProcessor());
    map.put(MultiLineCommentStripperProcessor.ALIAS, (T) new MultiLineCommentStripperProcessor());
  }
}
