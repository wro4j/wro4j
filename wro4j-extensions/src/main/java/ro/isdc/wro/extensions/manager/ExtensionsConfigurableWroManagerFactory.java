/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import java.util.Map;

import ro.isdc.wro.extensions.processor.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.YUIJsCompressorProcessor;
import ro.isdc.wro.extensions.processor.rhino.less.LessCssProcessor;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * An implementation of {@link ConfigurableWroManagerFactory} that adds processors defined in extensions module.
 *
 * @author Alex Objelean
 */
public class ExtensionsConfigurableWroManagerFactory extends ConfigurableWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {
    map.put("yuiCssMin", new YUICssCompressorProcessor());
    map.put("yuiJsMin", new YUIJsCompressorProcessor());
    map.put("lessCss", new LessCssProcessor());
    map.put("googleClosureSimple", new GoogleClosureCompressorProcessor());
    map.put("googleClosureAdvanced", new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {
    map.put("lessCss", new LessCssProcessor());
  }
}
