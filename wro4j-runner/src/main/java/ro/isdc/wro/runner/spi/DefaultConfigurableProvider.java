package ro.isdc.wro.runner.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.runner.processor.RunnerCssLintProcessor;
import ro.isdc.wro.runner.processor.RunnerJsHintProcessor;
import ro.isdc.wro.runner.processor.RunnerJsLintProcessor;
import ro.isdc.wro.util.provider.ConfigurableProviderSupport;

public class DefaultConfigurableProvider extends ConfigurableProviderSupport {
  @Override
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(RunnerCssLintProcessor.ALIAS, new RunnerCssLintProcessor());
    map.put(RunnerJsLintProcessor.ALIAS, new RunnerJsLintProcessor());
    map.put(RunnerJsHintProcessor.ALIAS, new RunnerJsHintProcessor());
    return map;
  }
}
