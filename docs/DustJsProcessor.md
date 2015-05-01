# Introduction
Dust is a javascript templating engine designed to provide a clean separation between presentation and logic without sacrificing ease of use. 
A detailed documentation and its philosophy can be found on [project home page](http://akdubya.github.com/dustjs/).  

## Details
The DustJsProcessor uses DustJs engine to transform a template into javascript. For example, the following template:
```
Hello {name}! You have {count} new messages.
```

is compiled into:
```javascript
(function () {
    dust.register("hello", body_0);

    function body_0(chk, ctx) {
        return chk.write("Hello ").reference(ctx.get("name"), ctx, "h").write("! You have ").reference(ctx.get("count"), ctx, "h").write(" new messages.");
    }
    return body_0;
})();
```

The compiled function will register a template with a name of the compiled resource. For instance, the above compiled script resulted after processing the resource named ```hello.js```. That is why,  it does make sense to use DustJsProcessor as a preprocessor.

As you can see, the processor is responsible for generating compiled templates. In order to use dustJs templates in browser, you have to do additional steps (documented in DustJs guide):

### Include the full distribution if you want to compile templates within the browser (as in the online demo):
```xml
<script src="dust-full-0.3.0.min.js"></script>
```

### To render a template, call dust.render with the template name, a context object and a callback function:
```javascript
dust.render("hello", {name: "Fred", count: 1}, function(err, out) {
  console.log(out);
});
```

### The code above will write the following to the console:
```
Hello Fred! You have 1 new messages.
```

## Using DustJsProcessor  
In order to use DustJsProcessor, there are several options. 

### Configure wro.properties 
Dust js uses the following alias: ```DustJs```. Add this alias to wro.properties. Example:
```
preProcessors=dustJs
```

The above configuration will instruct wro4j to use DustJsProcessor as a preprocessor for all resources of type JS.
Since the dustJs templates are not quite js files, it probably does make sense to store dustJs templates in files with a different extension (example ```*.template```). In order to apply the DustJsProcessor only on resources with ```*.template``` extension, you can update the preProcessors configuration to:
```
preProcessors=dustJs.tempate
```
The above configuration will decorate the DustJsProcessor with [ExtensionsAwareProcessorDecorator](ExtensionsAwareProcessorDecorator), which will enforce the DustJsProcessor to be applied only on resources with ```*.template``` extension.


### Using DustJsProcessor programmatically 
You can also use or add DustJsProcessor directly in java code.
This is an example of how processor can be used in isolation:
```java
processor.process(Resource.create("hello.js"), new StringReader("Hello {name}!"), writer);
```	

Another alternative of using DustJsProcessor, is to add it to [ProcessorsManagement ProccessorsFactory]. Example:
{{{
public final class DefaultProcesorsFactory
  extends SimpleProcessorsFactory {
  public DefaultProcesorsFactory() {
    addPreProcessor(new DustJsProcessor());
  }
}
}}}