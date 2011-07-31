/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.extensions.processor.algorithm.csslint.CssLintException;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Handles -c or --compressor argument by checking if the value has an associated compressor.
 *
 * @author Alex Objelean
 */
public class CompressorOptionHandler extends OptionHandler<ResourcePreProcessor> {
  private static final Logger LOG = LoggerFactory.getLogger(CompressorOptionHandler.class);

  private Map<String, ResourcePreProcessor> map;


  public CompressorOptionHandler(final CmdLineParser parser, final OptionDef option,
    final Setter<? super ResourcePreProcessor> setter) {
    super(parser, option, setter);
    initMap();
  }


  private void initMap() {
    // add core processors
    map = ProcessorsUtils.createPreProcessorsMap();
    // add extension processors
    ExtensionsConfigurableWroManagerFactory.pupulateMapWithExtensionsProcessors(map);
    // add custom processors
    map.put(CssLintProcessor.ALIAS, new CssLintProcessor() {
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