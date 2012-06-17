package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ProcessorProvider;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.DuplicatesAwareCssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.FallbackCssDataUriProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.ConsoleStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * The implementation which contributes with processors from core module.
 * 
 * @author Alex Objelean
 * @created 1 Jun 2012
 */
public class DefaultProcessorProvider
    implements ProcessorProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePreProcessors() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    populateProcessorsMap(map);
    return map;
  }
  
  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePostProcessors() {
    return providePreProcessors();
  }
  
  private void populateProcessorsMap(final Map<String, ResourceProcessor> map) {
    map.put(CssUrlRewritingProcessor.ALIAS, new CssUrlRewritingProcessor());
    map.put(CssImportPreProcessor.ALIAS, new CssImportPreProcessor());
    map.put(CssVariablesProcessor.ALIAS, new CssVariablesProcessor());
    map.put(CssCompressorProcessor.ALIAS, new CssCompressorProcessor());
    map.put(SemicolonAppenderPreProcessor.ALIAS, new SemicolonAppenderPreProcessor());
    map.put(CssDataUriPreProcessor.ALIAS, new CssDataUriPreProcessor());
    map.put(FallbackCssDataUriProcessor.ALIAS, new FallbackCssDataUriProcessor());
    map.put(DuplicatesAwareCssDataUriPreProcessor.ALIAS_DUPLICATE, new DuplicatesAwareCssDataUriPreProcessor());
    map.put(JawrCssMinifierProcessor.ALIAS, new JawrCssMinifierProcessor());
    map.put(CssMinProcessor.ALIAS, new CssMinProcessor());
    map.put(JSMinProcessor.ALIAS, new JSMinProcessor());
    map.put(VariablizeColorsCssProcessor.ALIAS, new VariablizeColorsCssProcessor());
    map.put(ConformColorsCssProcessor.ALIAS, new ConformColorsCssProcessor());
    map.put(MultiLineCommentStripperProcessor.ALIAS, new MultiLineCommentStripperProcessor());
    map.put(ConsoleStripperProcessor.ALIAS, new ConsoleStripperProcessor());
  }
}
