# Introduction
It is possible to reuse wro4j processors only, without needing to have a filter or a maven plugin - only java code. This might be useful for creating custom implementation which reuse processors like: **CoffeeScript**, **LessCss** or any other [available processor](AvailableProcessors).

# Details 
Most of the processors extend the same interface: 

```java
public interface ResourcePreProcessor {
  void process(final Resource resource, final Reader reader, final Writer writer) throws IOException;
}
```

The idea is simple, process a *Resource* whose content should be taken from the Reader and write the outcome to the Writer. You should not be interested about the underlying details (ex: if the processor is using third party libraries,like Rhino, or does other tricks). In most of the case, using a processor is as simple as:

```java
Resource resource = Resource.create("script.coffee", ResourceType.JS);
Reader reader = new FileReader("/path/to/script.coffee");
Writer writer = new FileWriter("/path/to/script.js");
new CoffeeScriptProcessor().process(resource, reader, writer);
```

The above code reads a file located at **/path/to/script.coffee** containing a coffee script code and creates a javascript file located at **/path/to/script.js**. 

Since the location of the resource is abstracted to Reader/Writer you can easily read the content from a different location (not only files) and writing it to an arbitrary location (ex: servlet output stream).

## Reusing context-aware Processors 
Some of the processors require some extra details like: **WroConfiguration** or **ResourceUriLocators** or other wro4j related dependencies. For instance, the **GoogleClosureCompressorProcessor** relies on **WroConfiguration** object which holds the information about encoding.
This kind of processors requires a little bit extra coding to help the processor become aware of this kind of configurations. As a result the code might look like:

```java
//Create the configuration object and use it for current context
WroConfiguration config = new WroConfiguration();
Context.set(Context.standaloneContext(), config);
try {
  //Create injector which will inject all dependencies of the processor
  Injector injector = new InjectorBuilder().build();

  GoogleClosureCompressorProcessor processor = new GoogleClosureCompressorProcessor();

  //this will inject all required fields, after this point it is safe to  use processor outside of wro4j context. 
  injector.inject(processor);

  Resource resource = Resource.create("script.js", ResourceType.JS);
  Reader reader = new FileReader("path/to/script.js");
  Writer writer = new FileWriter("path/to/script.min.js");

  //Do the actual processing
  processor.process(resource, reader, writer);
} finally {
  Context.unset();
}
```