/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dojotoolkit.shrinksafe.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;

import com.github.lltyk.rhino17r1.Context;


/**
 * Compresses javascript code using compressor implemented by Dojo Shrinksafe utility.
 *
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class DojoShrinksafeCompressorProcessor
  implements ResourcePostProcessor, ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(DojoShrinksafeCompressorProcessor.class);
  public static final String ALIAS = "dojoShrinksafe";

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start("pack");
    //initialize rhino context
    Context.enter();
    try {
      final String script = IOUtils.toString(reader);
      final String stripConsole = null; //normal, warn, all
      LOG.debug("compressing script: {}", StringUtils.abbreviate(script, 40));
      final String out = Compressor.compressScript(script, 0, 0, stripConsole);

      writer.write(out);
    } finally {
      Context.exit();
      reader.close();
      writer.close();
      watch.stop();
      LOG.debug(watch.prettyPrint());
    }
  }
}
