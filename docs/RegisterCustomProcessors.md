# Introduction 
Note: the feature described on this page is available since wro4j-1.4.7.

An alternative way to configure custom processors, is to register each processor with an associated alias as a service (for this the [ServiceRegistry](http://docs.oracle.com/javase/1.4.2/docs/api/javax/imageio/spi/ServiceRegistry.html) is used). 

It is possible to create custom !ServiceRegistry implementation which allows simple way of registering processors along with corresponding aliases. The !ServiceRegistry implementations are loaded from the classpath and used by !ConfigurableWroManagerFactory when looking up for an alias defined in **wro.properties** file

## Details 
ConfigurableWroManagerFactory allows a simplified configuration with aliases. The idea is that each processor has an associated alias and the s can be configured in wro.properties (the location and name of this file is configurable) like this:

```
preProcessors=cssUrlRewriting,cssImport,semicolonAppender
postProcessors=jsMin,lessCss
```

It is possible to associated new (or even same) aliases with new custom processor implementations. 

### Using ProcessorProvider 
This option is available since 1.4.7.
One option to provide custom associations of locator alias and corresponding implementation is to create a class which implements **ro.isdc.wro.model.resource.processor.support.ProcessorProvider** interface. For instance the default aliases are provided like this:

```java
public class DefaultProcessorProvider
    implements ProcessorProvider {
 
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    populateProcessorsMap(map);
    return map;
  }
  
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return toPostProcessors(providePreProcessors());
  }

  /**
   * Creates a map of postProcessors form a map of preProcessors. This method will be removed in 1.5.0 release when
   * there will be no differences between pre & post processor interface.
   */
  private Map<String, ResourcePostProcessor> toPostProcessors(
      final Map<String, ResourcePreProcessor> preProcessorsMap) {
    final Map<String, ResourcePostProcessor> map = new HashMap<String, ResourcePostProcessor>();
    for (Entry<String, ResourcePreProcessor> entry : preProcessorsMap.entrySet()) {
      map.put(entry.getKey(), new ProcessorDecorator(entry.getValue()));
    }
    return map;
  }
  
  private void populateProcessorsMap(final Map<String, ResourcePreProcessor> map) {
    map.put(CssUrlRewritingProcessor.ALIAS, new CssUrlRewritingProcessor());
    map.put(CssImportPreProcessor.ALIAS, new CssImportPreProcessor());
    map.put(CssVariablesProcessor.ALIAS, new CssVariablesProcessor());
    map.put(CssCompressorProcessor.ALIAS, new CssCompressorProcessor());
    map.put(SemicolonAppenderPreProcessor.ALIAS, new SemicolonAppenderPreProcessor());
    map.put(CssDataUriPreProcessor.ALIAS, new CssDataUriPreProcessor());
    map.put(FallbackCssDataUriProcessor.ALIAS, new FallbackCssDataUriProcessor());
    map.put(DuplicatesAwareCssDataUriPreProcessor.ALIAS_DUPLICATE, new DuplicatesAwareCssDataUriPreProcessor());
    map.put(JawrCssMinifierProcessor.ALIAS, new JawrCssMinifierProcessor());
    map.put(CssMinProcessor.ALIAS, new CssMinProcessor());
    map.put(JSMinProcessor.ALIAS, new JSMinProcessor());
    map.put(VariablizeColorsCssProcessor.ALIAS, new VariablizeColorsCssProcessor());
    map.put(ConformColorsCssProcessor.ALIAS, new ConformColorsCssProcessor());
    map.put(MultiLineCommentStripperProcessor.ALIAS, new MultiLineCommentStripperProcessor());
    map.put(ConsoleStripperProcessor.ALIAS, new ConsoleStripperProcessor());
  }
}
```

Next step is to register that provider to make it visible by ConfigurableWroManagerFactory. To register it, add a file at the following location in project classpath:
```
META-INF/services/ro.isdc.wro.model.resource.processor.support.ProcessorProvider
```

Update the content of this file with a single line containing the fully qualified name of the your custom provider, example:

```
com.mycompany.processor.CustomProcessorProvider
```

If you have more than one implementation of and want to register all of them, add each class name on the new line:

```
com.mycompany.processor.CustomProcessorProvider1
com.mycompany.processor.CustomProcessorProvider2
```

### Using ConfigurableProvider 
**ro.isdc.wro.util.provider.ConfigurableProvider** is an interface which extends **ro.isdc.wro.model.resource.processor.support.ProcessorProvider** and is useful when you want to provide not only processors but also other components like: locators, namingStrategy or hashStrategy.

The principle is the same, the only change is the provider implementation:

```java
public class MyConfigurableProvider
    extends ConfigurableProviderSupport {

  public Map<String,ResourcePreProcessor> providePreProcessors() {
    return //the map of preProcessors
  }

  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return //the map of postProcessors
  }  
}
```

Notice that the **DefaultConfigurableProvider** extends **ConfigurableProviderSupport** which provides default implementation for all interfaces the **ro.isdc.wro.util.provider.ConfigurableProvider** extends.

The next step is to register that implementation as a service at the following location in the classpath:

```
META-INF/services/ro.isdc.wro.util.provider.ConfigurableProvider
```

with the fully qualified name of the custom provider class:
```
com.mycompany.locator.MyConfigurableProvider
```

If you have more than one implementation of and want to register all of them, add each class name on the new line:

```
com.mycompany.locator.MyConfigurableProvider1
com.mycompany.locator.MyConfigurableProvider2
```