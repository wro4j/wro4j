# Introduction
Most of the compressors strip unnecessary content along with the comments. Sometimes it is required to preserve copyright/license information even after compressor is applied. A copyright comment can look like this (extracted from jQuery library):

```js
/*!
 * jQuery JavaScript Library v1.6.1
 * http://jquery.com/
 *
 * Copyright 2011, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 * Copyright 2011, The Dojo Foundation
 * Released under the MIT, BSD, and GPL Licenses.
 *
 * Date: Thu May 12 15:04:36 2011 -0400
 */
(function( window, undefined ) {
  //... 
  window.jQuery = window.$ = jQuery;
})(window);
```

Some of the compressors do allow preserving this kind of headers after compression, but is there a way to have this feature for all of them?

Starting with wro4j-1.3.7 release, a new processor has been created exactly for that purpose. It is called [CopyrightKeeperProcessorDecorator](https://github.com/wro4j/wro4j/blob/v1.3.7/wro4j-core/src/main/java/ro/isdc/wro/model/resource/processor/impl/CopyrightKeeperProcessorDecorator.java) and below you'll find out more details about it and how to use it.

# Details 
As its name states, [CopyrightKeeperProcessorDecorator](https://github.com/wro4j/wro4j/blob/v1.3.7/wro4j-core/src/main/java/ro/isdc/wro/model/resource/processor/impl/CopyrightKeeperProcessorDecorator.java) is a decorator over any kind of pre & post processor. 
This processor will be applied on resource of the same type as the decorated processor is configured. In other words, if you decorate JsMin processor, it will process only javascript resources (not css). Similarly, when decorating CssMinProcessor, on css resources will be processed.
CopyrightKeeperProcessorDecorator workflow:
* Parse the resource content and search for copyright/licence comments
* If any copyright/licence comments are found, save them before proceeding with decorated processor logic.
* Apply the decorated processor logic and compress the resource.
* Check if the compressed code still contains copyright/licence comments. If none found, prepend the saved comments to the result content. 

  
  
Currently, the only way it is possible to integrate this processor in your application is programmatically decorate processors you want. Here is a snippet which creates a custom WroManagerFactory:

```java 
public class MyWroManagerFactory extends BaseWroManagerFactory { 
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    ResourcePreProcessor processor = new JsMinProcessor();
    factory.setResourcePreProcessors(CopyrightKeeperProcessorDecorator.decorate(processor);
    return factory;
  } 
}
```

In the above example, we create a ProcessorsFactory which has a single pre processor (JsMin) which is decorated by CopyrightKeeperProcessorDecorator. It is more likely that you'll need also other processors besides JsMin. Also, instead of JsMin you may want to use UglifyJs or GoogleClosure compressors. This approach works for any kind of compressors, for both type of supported resources: js & css.