# Introduction
A processor decorator implements decorator design pattern. When it decorates a preprocessor it acts as a preprocessor and the same is true for postprocessor. If the decorated processor is of both types (pre & post processor), then the decorator is of both types as well.

All metadata about decorated processor is inherited by decorator. For instance, if the decorated processor is annotated with ```@SupportedResourceType(ResourceType.JS)``` - then the decorator will be applied only on javascript resources as well. The same applies for ```@Minimize``` annotation.

## Creating custom decorator
All processor decorators extend the ```AbstractProcessorDecorator``` class. When creating a custom decorator, you'll typically will extend that class, because it already implements all decorator logic.

This is a sample implementation of a decorator:

```java
public class NoOpProcessorDecorator
  extends AbstractProcessorDecorator {
  private NoOpProcessorDecorator(final ResourcePreProcessor preProcessor) {
    super(preProcessor);
  }

  private NoOpProcessorDecorator(final ResourcePostProcessor postProcessor) {
    super(postProcessor);
  }

  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
    	IOUtils.copy(reader, writer);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
```

This decorator is called !NoOpProcessorDecorator, and as its name states, it does nothing useful. It only copies the output produced by decorated processor to the writer. 

You can notice that !NoOpProcessorDecorator declares two constructors, one for decorating a preprocessor and other for postprocessor. 

An example of how this decorator is used is shown here:

```java
new NoOpProcessorDecorator(new JsMinProcessor());	
```
Here we've created a new processor which decorates ```JsMinProcessor```. As a result, the new processor will behave exactly the same as the JsMinProcessor. In other words, it will be applied only on js resources and it will not be applied when the minimization will be turned off.

# Available Decorators
The ```NoOpProcessorDecorator``` is not very useful. By default, wro4j contains two useful decorators: ```CopyrightKeeperProcessorDecorator``` and [ExtensionsAwareProcessorDecorator](ExtensionsAwareProcessorDecorator).

The ```CopyRightKeeperProcessorDecorator``` is useful for preserving copyright comments in case these are stripped by the decorated prcoessor. 

The ```ExtensionsAwareProcessorDecorator``` is useful to apply the decorated processor only on  resources which has a certain extension or extensions. 