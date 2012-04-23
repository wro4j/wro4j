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
import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

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
  protected void contributePostProcessors(final Map<String, ResourcePostProcessor> map) {
    pupulateMapWithExtensionsProcessors(map);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void contributePreProcessors(final Map<String, ResourcePreProcessor> map) {
    pupulateMapWithExtensionsProcessors(map);
  }

  /**
   * Populates a map of processors with processors existing in extensions module.
   *
   * @param <T>
   *          type of processors (pre or post). This can be one of the following: {@link ResourcePreProcessor} or
   *          {@link ResourcePostProcessor}.
   * @param map
   *          to populate.
   */
  @SuppressWarnings("unchecked")
  public static <T> void pupulateMapWithExtensionsProcessors(final Map<String, T> map) {
    Validate.notNull(map);
    map.put(YUICssCompressorProcessor.ALIAS, (T) new YUICssCompressorProcessor());
    map.put(YUIJsCompressorProcessor.ALIAS_NO_MUNGE, (T) YUIJsCompressorProcessor.noMungeCompressor());
    map.put(YUIJsCompressorProcessor.ALIAS_MUNGE, (T) YUIJsCompressorProcessor.doMungeCompressor());
    map.put(DojoShrinksafeCompressorProcessor.ALIAS, (T) new DojoShrinksafeCompressorProcessor());
    map.put(UglifyJsProcessor.ALIAS_UGLIFY, (T) new UglifyJsProcessor());
    map.put(BeautifyJsProcessor.ALIAS_BEAUTIFY, (T) new BeautifyJsProcessor());
    map.put(PackerJsProcessor.ALIAS, (T) new PackerJsProcessor());
    map.put(LessCssProcessor.ALIAS, (T) new LessCssProcessor());
    map.put(SassCssProcessor.ALIAS, (T) new SassCssProcessor());
    map.put(SassCssProcessor.ALIAS_RUBY, (T) new SassCssProcessor(SassCssProcessor.Engines.RUBY));
    map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, (T) new GoogleClosureCompressorProcessor());
    map.put(GoogleClosureCompressorProcessor.ALIAS_ADVANCED, (T) new GoogleClosureCompressorProcessor(
        CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put(CoffeeScriptProcessor.ALIAS, (T) new CoffeeScriptProcessor());
    map.put(DustJsProcessor.ALIAS, (T) new DustJsProcessor());
    map.put(CJsonProcessor.ALIAS_PACK, (T) CJsonProcessor.packProcessor());
    map.put(CJsonProcessor.ALIAS_UNPACK, (T) CJsonProcessor.unpackProcessor());
    map.put(JsonHPackProcessor.ALIAS_PACK, (T) JsonHPackProcessor.packProcessor());
    map.put(JsonHPackProcessor.ALIAS_UNPACK, (T) JsonHPackProcessor.unpackProcessor());
    map.put(JsHintProcessor.ALIAS, (T) new JsHintProcessor());
    map.put(JsLintProcessor.ALIAS, (T) new JsLintProcessor());
    map.put(CssLintProcessor.ALIAS, (T) new CssLintProcessor());
  }
}
