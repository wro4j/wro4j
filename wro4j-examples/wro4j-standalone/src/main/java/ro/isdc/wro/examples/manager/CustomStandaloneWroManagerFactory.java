/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.examples.manager;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
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

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new JSMinProcessor());

    // factory.addPreProcessor(YUIJsCompressorProcessor.doMungeCompressor());
    factory.addPostProcessor(new LessCssProcessor());
    factory.addPostProcessor(new YUICssCompressorProcessor());
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
        model.addGroup(new Group("all").addResource(Resource.create("http://code.jquery.com/jquery-1.6.2.js",
          ResourceType.JS)));
        return model;
      }

      public void destroy() {}
    };
  }
}
