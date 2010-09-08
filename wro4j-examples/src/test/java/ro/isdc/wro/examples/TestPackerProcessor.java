/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.examples;



/**
 * TestLessCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestPackerProcessor
{}

//extends AbstractWroTest {
//  private static final Logger LOG = LoggerFactory.getLogger(TestPackerProcessor.class);
//  private ResourcePostProcessor processor;
//
//  @Before
//  public void setUp() {
//     processor = new PackerJsProcessor();
//  }
//
//  @Test
//  public void testPacker()
//      throws IOException {
//    final ScriptEngineManager manager = new ScriptEngineManager();
//    final List<ScriptEngineFactory> factories = getAvailableEngines(manager);
//    // create JavaScript engine
//    final ScriptEngine scriptEngine = manager.getEngineByName("js");
//    if (scriptEngine == null) {
//      throw new IllegalStateException("No ScriptManager for JavaScript is available. Available managers are: "
//          + factories);
//    }
//
//    LOG.debug("testPacker");
//    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/input.js", "classpath:"
//        + WroUtil.toPackageAsFolder(getClass()) + "/packer-output.js", new ResourceProcessor() {
//      public void process(final Reader reader, final Writer writer)
//          throws IOException {
//        processor.process(reader, writer);
//      }
//    });
//  }
//
//  private List<ScriptEngineFactory> getAvailableEngines(final ScriptEngineManager manager) {
//    final List<ScriptEngineFactory> engines = manager.getEngineFactories();
//    if (engines.isEmpty()) {
//      LOG.debug("No scripting engines were found");
//    } else {
//      LOG.debug("The following " + engines.size() + " scripting engines were found");
//      for (final ScriptEngineFactory engine : engines) {
//        LOG.debug("Engine name: " + engine.getEngineName());
//        LOG.debug("\tVersion: " + engine.getEngineVersion());
//        LOG.debug("\tLanguage: " + engine.getLanguageName());
//        final List<String> extensions = engine.getExtensions();
//        if (extensions.size() > 0) {
//          LOG.debug("\tEngine supports the following extensions:");
//          for (final String e : extensions) {
//            LOG.debug("\t\t" + e);
//          }
//        }
//        final List<String> shortNames = engine.getNames();
//        if (shortNames.size() > 0) {
//          LOG.debug("\tEngine has the following short names:");
//          for (final String n : engine.getNames()) {
//            LOG.debug("\t\t" + n);
//          }
//        }
//        LOG.debug("=========================");
//      }
//    }
//    return engines;
//  }
//}
