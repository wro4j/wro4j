/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * An implementation of {@link ConfigurableWroManagerFactory} that adds processors defined in extensions module.
 *
 * @author Alex Objelean
 */
public class ExtensionsConfigurableWroManagerFactory
    extends ConfigurableWroManagerFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {
    populateMapWithExtensionsProcessors(map);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {
    final Map<String, ResourcePreProcessor> preProcessorsMap = new HashMap<String, ResourcePreProcessor>();
    populateMapWithExtensionsProcessors(preProcessorsMap);
    for (Entry<String, ResourcePreProcessor> entry : preProcessorsMap.entrySet()) {
      map.put(entry.getKey(), ProcessorsUtils.toPostProcessor(entry.getValue()));
    }
  }

  /**
   * Populates a map of processors with processors existing in extensions module.
   *
   * @param map
   *          to populate.
   */
  public static void populateMapWithExtensionsProcessors(final Map<String, ResourcePreProcessor> map) {
    Validate.notNull(map);
    //use lazy initializer to avoid unused dependency runtime requirement. 
    map.put(YUICssCompressorProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new YUICssCompressorProcessor();
      }
    }));
    map.put(YUIJsCompressorProcessor.ALIAS_NO_MUNGE, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return YUIJsCompressorProcessor.noMungeCompressor();
      }
    }));
    map.put(YUIJsCompressorProcessor.ALIAS_MUNGE, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return YUIJsCompressorProcessor.doMungeCompressor();
      }
    }));
    map.put(DojoShrinksafeCompressorProcessor.ALIAS, new DojoShrinksafeCompressorProcessor());
    map.put(UglifyJsProcessor.ALIAS_UGLIFY, new UglifyJsProcessor());
    map.put(BeautifyJsProcessor.ALIAS_BEAUTIFY, new BeautifyJsProcessor());
    map.put(PackerJsProcessor.ALIAS, new PackerJsProcessor());
    map.put(LessCssProcessor.ALIAS, new LessCssProcessor());
    map.put(SassCssProcessor.ALIAS, new SassCssProcessor());
    map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS);
      }
    }));
    map.put(GoogleClosureCompressorProcessor.ALIAS_ADVANCED, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new GoogleClosureCompressorProcessor(
            CompilationLevel.ADVANCED_OPTIMIZATIONS);
      }
    }));
    map.put(CoffeeScriptProcessor.ALIAS, new CoffeeScriptProcessor());
    map.put(DustJsProcessor.ALIAS, new DustJsProcessor());
    map.put(CJsonProcessor.ALIAS_PACK, CJsonProcessor.packProcessor());
    map.put(CJsonProcessor.ALIAS_UNPACK, CJsonProcessor.unpackProcessor());
    map.put(JsonHPackProcessor.ALIAS_PACK, JsonHPackProcessor.packProcessor());
    map.put(JsonHPackProcessor.ALIAS_UNPACK, JsonHPackProcessor.unpackProcessor());
    map.put(JsHintProcessor.ALIAS, new JsHintProcessor());
    map.put(JsLintProcessor.ALIAS, new JsLintProcessor());
    map.put(CssLintProcessor.ALIAS,  new CssLintProcessor());
  }
}
