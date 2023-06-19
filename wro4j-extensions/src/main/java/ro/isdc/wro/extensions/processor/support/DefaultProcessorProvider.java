package ro.isdc.wro.extensions.processor.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ro.isdc.wro.extensions.processor.css.BourbonCssProcessor;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.NodeLessCssProcessor;
import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.DustJsProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.HandlebarsJsProcessor;
import ro.isdc.wro.extensions.processor.js.HoganJsProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.extensions.processor.js.LiterateCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.NgAnnotateProcessor;
import ro.isdc.wro.extensions.processor.js.NgMinProcessor;
import ro.isdc.wro.extensions.processor.js.NodeCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.NodeLiterateCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.NodeTypeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.RhinoLiterateCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.RhinoTypeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.TypeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.util.LazyInitializer;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * The implementation which contributes with processors from core module.
 *
 * @author Alex Objelean
 * @author Thilo Planz
 */
public class DefaultProcessorProvider
    implements ProcessorProvider {
  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    return createMap();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    final Map<String, ResourcePostProcessor> resultMap = new HashMap<String, ResourcePostProcessor>();
    /**
     * Created to overcome the difference between {@link ResourcePreProcessor} and {@link ResourcePostProcessor}
     * interfaces which will be resolved in next major version.
     */
    final Map<String, ResourcePreProcessor> preProcessorsMap = createMap();
    for (final Entry<String, ResourcePreProcessor> entry : preProcessorsMap.entrySet()) {
      resultMap.put(entry.getKey(), new ProcessorDecorator(entry.getValue()));
    }
    return resultMap;
  }

  /**
   * @return the map of pre processors.
   */
  private Map<String, ResourcePreProcessor> createMap() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(YUICssCompressorProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new YUICssCompressorProcessor();
      }
    }));
    map.put(DojoShrinksafeCompressorProcessor.ALIAS, new LazyProcessorDecorator(
        new LazyInitializer<ResourcePreProcessor>() {
          @Override
          protected ResourcePreProcessor initialize() {
            return new DojoShrinksafeCompressorProcessor();
          }
        }));
    map.put(UglifyJsProcessor.ALIAS_UGLIFY, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new UglifyJsProcessor();
      }
    }));
    map.put(BeautifyJsProcessor.ALIAS_BEAUTIFY, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new BeautifyJsProcessor();
      }
    }));
    map.put(RhinoLessCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoLessCssProcessor();
      }
    }));
    map.put(NodeLessCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new NodeLessCssProcessor();
      }
    }));
    map.put(Less4jProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new Less4jProcessor();
      }
    }));
    map.put(LessCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new LessCssProcessor();
      }
    }));
    map.put(SassCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new SassCssProcessor();
      }
    }));
    map.put(RubySassCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RubySassCssProcessor();
      }
    }));
    map.put(BourbonCssProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new BourbonCssProcessor();
      }
    }));
    map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, new LazyProcessorDecorator(
        new LazyInitializer<ResourcePreProcessor>() {
          @Override
          protected ResourcePreProcessor initialize() {
            return new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS);
          }
        }));
    map.put(GoogleClosureCompressorProcessor.ALIAS_ADVANCED, new LazyProcessorDecorator(
        new LazyInitializer<ResourcePreProcessor>() {
          @Override
          protected ResourcePreProcessor initialize() {
            return new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS);
          }
        }));
    map.put(GoogleClosureCompressorProcessor.ALIAS_WHITESPACE_ONLY, new LazyProcessorDecorator(
        new LazyInitializer<ResourcePreProcessor>() {
          @Override
          protected ResourcePreProcessor initialize() {
            return new GoogleClosureCompressorProcessor(CompilationLevel.WHITESPACE_ONLY);
          }
        }));
    map.put(RhinoCoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoCoffeeScriptProcessor();
      }
    }));
    map.put(NodeCoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new NodeCoffeeScriptProcessor();
      }
    }));
    map.put(CoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new CoffeeScriptProcessor();
      }
    }));
    map.put(RhinoLiterateCoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
        @Override
        protected ResourcePreProcessor initialize() {
          return new RhinoLiterateCoffeeScriptProcessor();
        }
      }));
    map.put(NodeLiterateCoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
        @Override
        protected ResourcePreProcessor initialize() {
          return new NodeLiterateCoffeeScriptProcessor();
        }
      }));
    map.put(LiterateCoffeeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
        @Override
        protected ResourcePreProcessor initialize() {
          return new LiterateCoffeeScriptProcessor();
        }
      }));
    map.put(CJsonProcessor.ALIAS_PACK, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return CJsonProcessor.packProcessor();
      }
    }));
    map.put(CJsonProcessor.ALIAS_UNPACK, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return CJsonProcessor.unpackProcessor();
      }
    }));
    map.put(JsonHPackProcessor.ALIAS_PACK, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return JsonHPackProcessor.packProcessor();
      }
    }));
    map.put(JsonHPackProcessor.ALIAS_UNPACK, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return JsonHPackProcessor.unpackProcessor();
      }
    }));
    map.put(JsHintProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new JsHintProcessor();
      }
    }));
    map.put(JsLintProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new JsLintProcessor();
      }
    }));
    map.put(CssLintProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new CssLintProcessor();
      }
    }));
    map.put(DustJsProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new DustJsProcessor();
      }
    }));
    map.put(HoganJsProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new HoganJsProcessor();
      }
    }));
    map.put(HandlebarsJsProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new HandlebarsJsProcessor();
      }
    }));
    map.put(RhinoTypeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoTypeScriptProcessor();
      }
    }));
    map.put(TypeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new TypeScriptProcessor();
      }
    }));
    map.put(NodeTypeScriptProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new NodeTypeScriptProcessor();
      }
    }));
    map.put(NgMinProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
        @Override
        protected ResourcePreProcessor initialize() {
          return new NgMinProcessor();
        }
      }));
    map.put(NgAnnotateProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
        @Override
        protected ResourcePreProcessor initialize() {
          return new NgAnnotateProcessor();
        }
      }));
    return map;
  }
}
