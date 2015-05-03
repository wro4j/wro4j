= Introduction
Describes what minimize aware processor means and how to use it.

**Related Pages**
[ResourceProcessors](ResourceProcessors)

# Details
A minimize aware processor, is a processor which performs some sort of minimization of resources. For instance:
YUIJsCompressorProcessor, YUICssCompressorProcessor, JsMinProcessor, CssMinProcessor fall into this category. The are minimize aware because as a result of processing the content is minimized. This sort minimization is useful for reducing the size of processed resource, but it has a single drawback: it is not anymore readable and debuggable. This why, during development you would like to switch off the minimization. 

Starting with version *1.2.1*, wro4j allows this by adding a minimize parameter to the resource url, like this:

```html
  <script type="text/javascript" src="wro/all.js?minimize=false"></script>
```

**Note**: the minimization works only when configuration mode is DEVELOPMENT

What is the difference between minimize aware processor and others from implementation point of view?
To differentiate a minimize aware resource from a default one, you annotate the processor class with @Minimize annotation. For example, you may want to replace the default JsMinProcessor with a better one, let's say GoogleClosureJsProcessor. It would look like this:

```java
@Minimize
@SupportedResourceType(ResourceType.JS)
public class GoogleClosureJsProcessor implements ResourcePreProcessor,
    ResourcePostProcessor {
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    //... implementation code
  }
}
```

The @Minimize annotation marks a processor as minimize aware and as a result, it will not be applied if minimization is turned off.