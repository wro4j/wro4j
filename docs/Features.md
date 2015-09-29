---
title: Features Overview
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: "An overview of most important features"
---


  *  **Free and open source**: Released under an Apache 2.0 license, wro4j can be used free of charge with or without modifications, for both commercial and noncommercial purposes.

  *  **[Easy to understand](DesignOverview)** - One of the wro4j goal is simplicity. Its [design](DesignOverview) is easy to understand and provides the enough flexibility to extend the framework and add new features over time or to adapt according to your project needs. 

  *  **[Easy to setup](http://wro4j.github.com/wro4j)**: All you need to start using *wro4j* is to declare a filter in your deployment descriptor, create a simple [wro.xml](WroFileFormat) or [wro.groovy](GroovyWroModel) configuration file (alternatively you can build the model [WroModelRuntime programmatically]), and include the desired resource(s) in your page. A complete [setup](GettingStarted ) takes a matter of minutes and requires little or no modifications over time. As a build-time solution, you can choose between [MavenPlugin](MavenPlugin) and [wro4j-runner command line tool](http://web-resource-optimization.blogspot.com/2011/02/simple-client-side-build-system-with.html). Also a [grails plugin](GrailsPlugin) is available.

  *  **Lightweight**: The only dependency project has is: commons-io & slf4j for logging. That is why, *wro4j* is very easy to integrate with any j2ee web application, using any type of web frameworks: request based (*[Struts](http://struts.apache.org/), [Spring MVC](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html), [Stripes](http://www.stripesframework.org/display/stripes/Home)*) or component based (*[Wicket](http://wicket.apache.org/ ), [Tapestry](http://tapestry.apache.org/ ), JSF*). In fact, it is completely independent of the technology you are using. See the [GettingStarted Getting Started Guide] for details.

  *  **Minification and processing**: wro4j provides a built-in mechanism for resource minification & compressing. Also, it is very easy to extend it with other third party tools like YUI, [ShrinkSafe Dojo](http://dojotoolkit.org/reference-guide/shrinksafe/) or other. The included resources can be pre-processed (before merging) or post-processed (after merging). It is easy to add a new processor or a list of processors. There are several minification tools supported by wro4j: *!JsMin*, *Google Closure compressor*, *[YUI Compressor](http://developer.yahoo.com/yui/compressor/)*, [http://dean.edwards.name/packer/ PackerJs] by Dean Edwards, *[https://github.com/mishoo/UglifyJS UglifyJs]*, *[Dojo Shrinksafe](http://dojotoolkit.org/reference-guide/shrinksafe/)* compressor. Also wro4j provides processors which perform the opposite of compressing and minimizing, like: *!BeautifyJs* (based on uglifyJs). 

  *  **Preserve copyright/licence headers**: Starting with version 1.3.7, it is possible to preserve copyright/licence headers for any kind of existing compressors, even if their default implementation doesn't support this feature. More details: [CopyRightKeeperProcessorDecorator](CopyRightKeeperProcessorDecorator)

  * **[Css Variables Support](CssVariablesSupport)** - How long have you wanted to name colors and such in your CSS instead of having to use search and replace (which breaks if you share the same colors? With built in !CssVariablesPreprocessor you can start using variables in your css files. The css variables notation was inspired from [here](http://disruptive-innovations.com/zoo/cssvariables/). 
  
  * **Css Meta Frameworks Support** - is a way to simplify CSS code. There are varous meta frameworks. Wro4j provide support for: [LessCssSupport](LessCssSupport), [SassCssSupport](SassCssSupport).

  * **Javascript Meta Frameworks Support** - allows you to write for example [Coffee Script](http://jashkenas.github.com/coffee-script/) or [TypeScript](http://www.typescriptlang.org/) code which will be compiled to javascript, see all [available processors](AvailableProcessors). Other meta frameworks (ex: [google traceur](http://code.google.com/p/traceur-compiler)) will be supported in the future.

  * **Javascript templating engine support** - provide a clean separation between presentation and logic without sacrificing ease of use by creating templates which compils to javascript functions using : [DustJsProcessor](DustJsProcessor),[HandlebarsJsProcessor](HandlebarsJsProcessor).

  * **Javascript Static Code Analysis** - you can use one of the following: [JsHint](http://jshint.com/) or [JsLint](http://www.jslint.com/) to validate javascript code. It is possible to use this feature also as a [maven plugin](http://web-resource-optimization.blogspot.com/2011/03/build-time-javascript-code-analysis.html).

  * **Css Code validation** - uses [CssLint](http://csslint.net/) utility to validate css. This feature can be used also as a maven plugin.

  * **Resource merging** - all resources (javascript or css files) can be merged. The way the resources are merged is described in a xml configuration file. Thus your application you can benefit of the reduction of number of requests.

  * **[JSON Compression](http://web-resource-optimization.blogspot.com/2011/06/json-compression-algorithms.html)** - wro4j provides an implementation for two algorithms which pack & unpack json. These algorithms are: [CJson](http://stevehanov.ca/blog/index.php?id=104 ) & [Json HPack](https://github.com/WebReflection/json.hpack)
 
  * **Css url rewriting** - after css resources are merged, the relative url will not point any more to a valid location. That is why it is important to rewrite the url's in the css files after merging. This is the most powerful feature of the *wro4j* and is performed by default by the framework. 

  * **Wildcard support** - allow wildcard resource selection. A detailed description of this feature is described on this page: see [WildcardSupport Wildcard Support]

  * **Web Resource Dynamic Interpolation** - It is possible to define css & js resources which contains expressions (ex: ${COLOR}) which are replaced on runtime. This feature is provided by [PlaceholderProcessor](PlaceholderProcessor).

  * **[Base64 encoded image in css](Base64DataUriSupport)** - Replace urls of the images from css with corresponding data uri representation.

  * **Merged Resource Versioning using a [NamingStrategy](OutputNamingStrategy) for the maven plugin** - it is possible to control the name of the processed resources using maven plugin. More details: [Maven plugin resource naming strategy](OutputNamingStrategy)

  * **Extensibility** - The core project doesn't have any dependency except commons-io. To keep things this way, another project has been created, called *wro4j-extension*. This project contains additional Processors (Minify, Obfuscate) using third party dependencies like *YUI*, *[ShrinkSafe Dojo](http://dojotoolkit.org/reference-guide/shrinksafe/)*, etc. This project is also an example of how easy it is to extend the core project & plug-in your custom implementation. Think of extensibility as of a way to integrate a css meta framework into your application. This can be easily done by creating a *CSSPostProcessor* which know how to hande a css meta definition & transforming them in regular css. For a complete list of processors, visit [AvailableProcessors](AvailableProcessors) wiki page.

  * **Caching** - A cache component is available and enabled by default to serve resources directly from memory instead of from the filesystem. You can [easily plugin a different caching implementation](ExtendingCachingImplementation) (JCS, !EhCache). Caching is enabled by default but can be disabled by configuration.

  * **Scheduled cache update** - it is possible to configure wro4j in such a way, that the cache would be flushed periodically and updated with latest resource changes.

  * **Resource Types and Location** - *wro4j* works with two types of resources: JavaScript and CSS. The resources can have absolutely any location. *wro4j* supports referencing resources inside the servlet context, from classpath, webjars, disk or using absolute URLs. You can easily add a new type of referencing scheme by implementing *!UriLocator* interface. This can be useful if you store your resources somewhere else (for instance in DB). 

  * **Webjars Support** - Since 1.7.0, wro4j add support for [webjars](http://www.webjars.org/). This simplifies the way you can manage the client-side dependencies in JVM-based web applications.

  * **Runtime JMX configuration** - You can change some of configuration in the runtime using jconsole and JMX. More details here: [RuntimeConfigurationsUsingJMX](RuntimeConfigurationsUsingJMX). This feature, allows you to manage all you js & css resources without ever restarting the server. 

  * **IDE integration** - [m2e-wro4j](https://github.com/jbosstools/m2e-wro4j) is an Eclipse Plugin (provided as third party contribution) which allows wro4j-maven-plugin to be invoked when your web resources are modified. For more details read this [blog post] (https://community.jboss.org/community/tools/blog/2012/01/17/css-and-js-minification-using-eclipse-maven-and-wro4j).