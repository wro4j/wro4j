# Introduction
Here you will learn about resource pre & post processors and how to implement them.

# Description 
Resource processors are responsible for applying some sort of processing of the resources. An example of the processor is *!JsMinProcessor*. This processor compress the javascript, removing comments and empty lines. 

There are two types of resource processors: 
  * Pre-Processor -  a processor which is applied on each resource before it is merged. A preprocessor is aware about the processed resource & its origin. An example of pre processor is: *!CssUrlRewritingProcessor*. This pre-processor is responsible for rewriting url's inside the css resource, in such way that after merge is complete the new urls would still point to a valid location. This kind of processor must be pre-processor, because it needs to know the resource it process, in order to rewrite the url depending on the location of the css resource. For more details, see javadoc of **CssUrlRewritingProcessor** class.
  * Post-Processor - same as pre-processor, except that it is applied after all resources are merged. A post processor doesn't care where the processed resources comes from. An example of post-processor is **JsMinProcessor**. This processor do its job after all resources are merged. Still, it may be implemented as a pre-processor, but the result will remain unchanged.

By default, wro4j comes with a set of resource processors which do most of the job you'll ever need. Already implemented resource processors are: **JsMinProcessor**, **CssMinProcessor**, **JawrCssMinifierProcessor**, **CssVariablesProcessor**, **AndryCssCompressorProcessor**, **BomStripperPreProcessor**, etc..

But sometimes, you have a custom requirement, or want a different approach for the same problem. In this case, all you have to do, is to implement your own resource processor. 

# Custom Resource Processors = 
To create a custom pre-processor, you have to implement *!ResourcePreProcessor* interface. This is how your custom pre-processor could look like this:

```java
public class MyCustomPreProcessor implements ResourcePreProcessor {
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    //read the content of the resoruce & write the processed content into the writer.
  }  
}
```

Similarly, to create a custom post-processor, you have to implement *!ResourcePostProcessor* interface. Below is an example of custom post processor:

```java
public class MyCustomPostProcessor implements ResourcePostProcessor {
  public void process(final Reader reader,
      final Writer writer) throws IOException {
    //do something & write to the writer
  }  
}
```

You may ask, how can I process only css or js resources?

That's pretty easy. All you have to do, is to annotate your processor implementation, like this:

```java
@SupportedResourceType(ResourceType.CSS)
public class MyCustomCssPostProcessor implements ResourcePostProcessor {
  //...
}
```

or, if you want your processor to process only javascript resources:

```java
@SupportedResourceType(ResourceType.JS)
public class MyCustomJsPostProcessor implements ResourcePostProcessor {
  //...
}
```

If you don't add any annotation, that means that the processor will be applied no matter what type of resource is processed.

The last step, is to configure wro4j, by adding your custom processor. To do this, you have at least two options:
  * Create a [ProcessorsFactory](ProcessorsManagement) which defines processors to be used during processors & add newly created **ProcessorsFactory** to [WroManager](WroManager).
  * use [ConfigurableWroManagerFactory](ConfigurableWroManagerFactory). 