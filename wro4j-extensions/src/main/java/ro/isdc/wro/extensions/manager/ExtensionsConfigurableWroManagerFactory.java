/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;

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
  protected void contributePostProcessors(final Map<String, ResourceProcessor> map) {
    pupulateMapWithExtensionsProcessors(map);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePreProcessors(final Map<String, ResourceProcessor> map) {
    pupulateMapWithExtensionsProcessors(map);
  }

  /**
   * Populates a map of processors with processors existing in extensions module.
   *
   * @param map to populate.
   */
  public static void pupulateMapWithExtensionsProcessors(final Map<String, ResourceProcessor> map) {
    Validate.notNull(map);
    map.put(YUICssCompressorProcessor.ALIAS, new YUICssCompressorProcessor());
    map.put(YUIJsCompressorProcessor.ALIAS_NO_MUNGE, YUIJsCompressorProcessor.noMungeCompressor());
    map.put(YUIJsCompressorProcessor.ALIAS_MUNGE, YUIJsCompressorProcessor.doMungeCompressor());
    map.put(DojoShrinksafeCompressorProcessor.ALIAS, new DojoShrinksafeCompressorProcessor());
    map.put(UglifyJsProcessor.ALIAS_UGLIFY, new UglifyJsProcessor());
    map.put(BeautifyJsProcessor.ALIAS_BEAUTIFY, new BeautifyJsProcessor());
    map.put(PackerJsProcessor.ALIAS, new PackerJsProcessor());
    map.put(LessCssProcessor.ALIAS, new LessCssProcessor());
    map.put(SassCssProcessor.ALIAS, new SassCssProcessor());
    map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, new GoogleClosureCompressorProcessor());
    map.put(GoogleClosureCompressorProcessor.ALIAS_ADVANCED, new GoogleClosureCompressorProcessor(
      CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put(CoffeeScriptProcessor.ALIAS, new CoffeeScriptProcessor());
    map.put(CJsonProcessor.ALIAS_PACK, CJsonProcessor.packProcessor());
    map.put(CJsonProcessor.ALIAS_UNPACK, CJsonProcessor.unpackProcessor());
    map.put(JsonHPackProcessor.ALIAS_PACK, JsonHPackProcessor.packProcessor());
    map.put(JsonHPackProcessor.ALIAS_UNPACK, JsonHPackProcessor.unpackProcessor());
    map.put(JsHintProcessor.ALIAS, new JsHintProcessor());
    map.put(CssLintProcessor.ALIAS, new CssLintProcessor());
  }
}
