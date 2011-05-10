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

import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
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
    map.put("googleClosureSimple", new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS));
    map.put("googleClosureAdvanced", new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put("jsMin", new JSMinProcessor());
    map.put("yuiJsMin", YUIJsCompressorProcessor.noMungeCompressor());
    map.put("yuiJsMinAdvanced", YUIJsCompressorProcessor.doMungeCompressor());
    map.put("uglifyJs", new UglifyJsProcessor());
    map.put("beautifyJs", new BeautifyJsProcessor());
    map.put("packerJs", new PackerJsProcessor());
    map.put("dojoShrinksafe", new DojoShrinksafeCompressorProcessor());
    map.put("coffeeScript", new CoffeeScriptProcessor());
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