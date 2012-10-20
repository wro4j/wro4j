/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.examples.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.metadata.DefaultMetaDataFactory;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.extensions.model.factory.GroovyModelFactory;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.PlaceholderProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.util.ObjectFactory;

/**
 * @author Alex Objelean
 */
public class CustomWroManagerFactory
    extends BaseWroManagerFactory {

  private static final String KEY_JS_HINT_OPTIONS = "jsHintOptions";

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory newModelFactory() {
    return new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
        throws IOException {
        return Context.get().getServletContext().getResourceAsStream("/WEB-INF/wro.groovy");
      }
    };
  }

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
    //factory.addPreProcessor(new JSMinProcessor());
    factory.addPreProcessor(new YUICssCompressorProcessor());
    factory.addPostProcessor(new CssVariablesProcessor());

    factory.addPreProcessor(new JawrCssMinifierProcessor());
    factory.addPreProcessor(new JsHintProcessor() {
      @Inject
      private MetaDataFactory metaDataFactory;
      @Override
      protected String[] getOptions() {
        //Not very safe, probably can validate it before cast
        return (String[]) metaDataFactory.create().get(KEY_JS_HINT_OPTIONS);
      }
    });

    return factory;
  }

  @Override
  protected MetaDataFactory newMetaDataFactory() {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put(KEY_JS_HINT_OPTIONS, ArrayUtils.toArray("undef"));
    return new DefaultMetaDataFactory(map);
  }

  private ResourcePreProcessor getPlaceholderProcessor() {
    return new PlaceholderProcessor().setPropertiesFactory(new ObjectFactory<Properties>() {
      @Override
      public Properties create() {
        final Properties props = new Properties();
        props.put("GLOBAL_COLOR", "red");
        return props;
      }
    });
  }
}
