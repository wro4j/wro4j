/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.algorithm.csslint.CssLintException;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
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
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * Handles -c or --compressor argument by checking if the value has an associated compressor.
 *
 * @author Alex Objelean
 */
public class CompressorOptionHandler extends OptionHandler<ResourcePreProcessor> {
  private static final Logger LOG = LoggerFactory.getLogger(CompressorOptionHandler.class);

  private final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();

  public CompressorOptionHandler(final CmdLineParser parser, final OptionDef option,
    final Setter<? super ResourcePreProcessor> setter) {
    super(parser, option, setter);
    initMap();
  }

  private void initMap() {
    map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS));
    map.put(GoogleClosureCompressorProcessor.ALIAS_ADVANCED, new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put(JSMinProcessor.ALIAS, new JSMinProcessor());
    map.put(YUIJsCompressorProcessor.ALIAS_NO_MUNGE, YUIJsCompressorProcessor.noMungeCompressor());
    map.put(YUIJsCompressorProcessor.ALIAS_MUNGE, YUIJsCompressorProcessor.doMungeCompressor());
    map.put(YUICssCompressorProcessor.ALIAS, new YUICssCompressorProcessor());
    map.put(JawrCssMinifierProcessor.ALIAS, new JawrCssMinifierProcessor());
    map.put(CssMinProcessor.ALIAS, new CssMinProcessor());
    map.put(CssCompressorProcessor.ALIAS, new CssCompressorProcessor());
    map.put(UglifyJsProcessor.ALIAS_UGLIFY, new UglifyJsProcessor());
    map.put(BeautifyJsProcessor.ALIAS_BEAUTIFY, new BeautifyJsProcessor());
    map.put(PackerJsProcessor.ALIAS, new PackerJsProcessor());
    map.put(DojoShrinksafeCompressorProcessor.ALIAS, new DojoShrinksafeCompressorProcessor());
    map.put(CssLintProcessor.ALIAS, new CssLintProcessor() {
      /**
       * {@inheritDoc}
       */
      @Override
      protected void onCssLintException(final CssLintException e, final Resource resource)
          throws Exception {
        super.onCssLintException(e, resource);
        System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
        System.err.println(e.getErrors());
      }
    });
    map.put(JsHintProcessor.ALIAS, new JsHintProcessor() {
      @Override
      protected void onJsHintException(final JsHintException e, final Resource resource)
          throws Exception {
        super.onJsHintException(e, resource);
        System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
        System.err.println(e.getErrors());
      }
    });
    map.put(CssDataUriPreProcessor.ALIAS, new CssDataUriPreProcessor());
    map.put(CJsonProcessor.ALIAS_PACK, CJsonProcessor.packProcessor());
    map.put(CJsonProcessor.ALIAS_UNPACK, CJsonProcessor.unpackProcessor());
    map.put(JsonHPackProcessor.ALIAS_PACK, JsonHPackProcessor.packProcessor());
    map.put(JsonHPackProcessor.ALIAS_UNPACK, JsonHPackProcessor.unpackProcessor());
    map.put(LessCssProcessor.ALIAS, new LessCssProcessor());
    map.put(SassCssProcessor.ALIAS, new SassCssProcessor());
    map.put(CoffeeScriptProcessor.ALIAS, new CoffeeScriptProcessor());
    map.put(VariablizeColorsCssProcessor.ALIAS, new VariablizeColorsCssProcessor());
    map.put(ConformColorsCssProcessor.ALIAS, new ConformColorsCssProcessor());
    map.put(CssVariablesProcessor.ALIAS, new CssVariablesProcessor());

  }


  @Override
  public String getDefaultMetaVariable() {
    return null;
  }


  @Override
  public int parseArguments(final Parameters params)
    throws CmdLineException {
    final String value = params.getParameter(0);
    LOG.debug("compressor argument: " + value);
    final ResourcePreProcessor processor = map.get(value);
    if (processor == null) {
      throw new CmdLineException("No processor defined for alias: " + value + ". Available alias are: " + map.keySet());
    }
    setter.addValue(processor);
    return 1;
  }

}