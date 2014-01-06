package ro.isdc.wro.model.resource.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.decorator.BenchmarkProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.provider.ProviderFinder;

/**
 * Used to check how long does it take to process a given amount of resources for each processor.
 *
 * @author Alex Objelean
 */
@Ignore
public class BenchmarkProcessors {
  private static final Logger LOG = LoggerFactory.getLogger(BenchmarkProcessors.class);
  private UriLocator locator;
  private Injector injector;
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    locator = new UrlUriLocator();
    injector = WroTestUtils.createInjector();
    injector.inject(locator);
  }

  @Test
  public void test() throws Exception {
    final StopWatch watch = new StopWatch();
    watch.start("load processors");
    final List<ResourcePreProcessor> processors = loadProcessors();
    LOG.debug("found: {} processors", processors.size());
    watch.stop();
    LOG.debug(watch.prettyPrint());

    final String jsSample = IOUtils.toString(locator
        .locate("http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.js"), "UTF-8");
    final String cssSample = IOUtils.toString(locator
        .locate("https://gist.github.com/raw/4525988/29e5791d999181a12ae700633acc7823ed17eadb/bootstrap"), "UTF-8");
    for (final ResourcePreProcessor processor : processors) {
      final ProcessorDecorator decorated = decorateProcessor(processor);
      injector.inject(decorated);
      LOG.debug("\n\n========== Start processor: " + processor.toString());
      if (ArrayUtils.contains(decorated.getSupportedResourceTypes(), ResourceType.JS)) {
        decorated.process(new StringReader(jsSample), new StringWriter());
      } else {
        decorated.process(new StringReader(cssSample), new StringWriter());
      }
    }
  }

  private ExceptionHandlingProcessorDecorator decorateProcessor(final ResourcePreProcessor processor) {
    return new ExceptionHandlingProcessorDecorator(new BenchmarkProcessorDecorator(
        processor)) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      };
    };
  }

  private List<ResourcePreProcessor> loadProcessors() {
    final List<ProcessorProvider> providers = ProviderFinder.of(ProcessorProvider.class).find();
    final List<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
    for (final ProcessorProvider provider : providers) {
      processors.addAll(provider.providePreProcessors().values());
    }
    return processors;
  }
}
