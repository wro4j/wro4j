/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.examples.manager;

import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

/**
 * @author Alex Objelean
 */
public class CustomWroManagerFactory
    extends BaseWroManagerFactory {
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  protected WroModelFactory newModelFactory() {
//    return new GroovyModelFactory() {
//      @Override
//      protected InputStream getModelResourceAsStream()
//        throws IOException {
//        return Context.get().getServletContext().getResourceAsStream("/WEB-INF/wro.groovy");
//      }
//    };
//  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    //preProcessors=bomStripper,cssImport,cssUrlRewriting,semicolonAppender,yuiJsMinAdvanced,yuiCssMin
    //postProcessors=cssVariables


    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    //factory.addPreProcessor(getPlaceholderProcessor());
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new JawrCssMinifierProcessor());
    //factory.addPreProcessor(new JSMinProcessor());
    factory.addPreProcessor(new YUICssCompressorProcessor());
    factory.addPostProcessor(new CssVariablesProcessor());
    factory.addPostProcessor(new RhinoLessCssProcessor());

    return factory;
  }
//
//  private ResourcePreProcessor getPlaceholderProcessor() {
//    return new PlaceholderProcessor().setPropertiesFactory(new ObjectFactory<Properties>() {
//      @Override
//      public Properties create() {
//        final Properties props = new Properties();
//        props.put("GLOBAL_COLOR", "red");
//        return props;
//      }
//    });
//  }
}
