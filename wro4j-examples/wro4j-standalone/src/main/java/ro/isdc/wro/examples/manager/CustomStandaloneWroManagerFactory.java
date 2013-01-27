/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.examples.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * @author Alex Objelean
 */
public class CustomStandaloneWroManagerFactory
    extends DefaultStandaloneContextAwareManagerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(CustomStandaloneWroManagerFactory.class);

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new JSMinProcessor());

    // factory.addPreProcessor(YUIJsCompressorProcessor.doMungeCompressor());
    factory.addPostProcessor(new RhinoLessCssProcessor());
    factory.addPostProcessor(new YUICssCompressorProcessor());

    final ResourcePreProcessor processor = new JsHintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
        LOG.info("processing resource: " + resource);
        if (resource != null) {
          LOG.info("processing resource: " + resource.getUri());
        }
        super.process(resource, reader, writer);
      }
      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        LOG.error(
          e.getErrors().size() + " errors found while processing resource: " + resource.getUri() + " Errors are: "
            + e.getErrors());
      };
    }.setOptionsAsString("");
    return factory;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory newModelFactory() {
    return new WroModelFactory() {
      public WroModel create() {
        final WroModel model = new WroModel();
//        model.addGroup(new Group("all").addResource(
//          Resource.create("http://code.jquery.com/jquery-1.6.2.js", ResourceType.JS)));
        model.addGroup(new Group("all").addResource(
          Resource.create("/css/test.css", ResourceType.CSS)));
        return model;
      }

      public void destroy() {}
    };
  }
}
