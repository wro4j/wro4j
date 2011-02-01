/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.assembly;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

import com.google.javascript.jscomp.CompilationLevel;

/**
 *
 * @author Alex Objelean
 */
public class CompressorOptionHandler extends OptionHandler<ResourcePreProcessor> {
  Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();

  public CompressorOptionHandler(final CmdLineParser parser, final OptionDef option,
    final Setter<? super ResourcePreProcessor> setter) {
    super(parser, option, setter);
    initMap();
  }

  private void initMap() {
    map.put("googleClosureSimple", new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS));
    map.put("googleClosureAdvanced", new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    map.put("jsMin", new JSMinProcessor());
    map.put("yuiJsMin", new YUIJsCompressorProcessor(false));
    map.put("yuiJsMinAdvanced", new YUIJsCompressorProcessor(true));
    map.put("uglifyJs", new UglifyJsProcessor());
    map.put("beautifyJs", new BeautifyJsProcessor());
    map.put("packerJs", new PackerJsProcessor());
    map.put("dojoShrinksafe", new DojoShrinksafeCompressorProcessor());
  }


  @Override
  public String getDefaultMetaVariable() {
    return null;
  }


  @Override
  public int parseArguments(final Parameters params)
    throws CmdLineException {
    System.out.println("parseArgument");
    System.out.println("params: " + params);
    System.out.println("option.isArgument: " + option.isArgument());
    final String value = params.getParameter(0);
    final ResourcePreProcessor processor = map.get(value);
    if (processor == null) {
      throw new CmdLineException("No compressor defined for alias: " + value + ". Available alias are: " + map.keySet());
    }
    setter.addValue(processor);
    return 0;
  }

}