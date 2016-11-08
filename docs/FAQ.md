# Frequently Asked Questions

## I have a question or a suggestion, where can I post it?
> Are you using wro4j on your site? Have you implemented something similar yourself? If so, please let us know! If you have any questions or ideas about some features you would like to see, if it's working for you, we'd love to hear about it, and if it's not, well we'd love to hear about that too! You could post to the [wro4j group](http://groups.google.com/group/wro4j).

## Where can I download the nightly builds?
> The latest snapshot are available on nexus snapshot repositories which are located either [here](https://oss.sonatype.org/content/groups/public/)
or [here](https://oss.sonatype.org/content/repositories/snapshots/) 


## Where are the javadocs?
> The javadocs for the latest version can be viewed  [here](http://alexo.github.com/wro4j/apidocs/1.4.2/index.html).

## Why would I use wro4j?
> * Improve the load time of your web application
> * Keep web resources organized
> * Integrate latest front-end technologies in your projects, like: less, coffeeScript, jsHint, cssLint or others. In other words, if you are searching for a simple way to bring front-end best practices to your project - wro4j can help you to do it in a simple way.

## Wro4j is a large project?
> Since wro4j has a lot of features, it may seem that it is a large project. However, it is based only on 3 simple concepts which is easy to understand and all of its features are leveraging around those concepts. You can understand it better by reading [DesignOverview] wiki page. 
Also, the project is split into several modules: core, extensions, maven-plugin & runner. The core module is very lightweight, containing a small number of dependencies (commons-io & commons-lang). The extensions module, contains many processors implementations and depends on many dependencies like rhino, jruby or google closure. This module will be splitted in the future into smaller reusable modules. 

## Can I use wro4j for multiple files coming from a outside domains?
> It doesn't matter the location of the resources you want to include into your project. The resources can be located anywhere, at any domain.

## Is there an IDE integration plugins for wro4j? 
> Currently there is only a plugin for eclipse supported. For more details read this [blog post](https://community.jboss.org/community/tools/blog/2012/01/17/css-and-js-minification-using-eclipse-maven-and-wro4j). 

## What are the similar projects?
> * [Jawr](https://jawr.dev.java.net/)
> * [Pack:tag](http://www.galan.de/projects/packtag)
> * [JavaScript Optimizer](http://js-optimizer.sourceforge.net/)

## What is a resource processor & how can I create a custom one?
> This [ResourceProcessors] wiki page contains a detailed description about resource processors and will answer your question.

## What's the best way to track new releases?
> The new release is announced on [discussion group](https://groups.google.com/forum/#!forum/wro4j) and on [twitter](http://twitter.com/#!/wro4j).

## How to get the wro4j sources?
> Initially the sources were hosted by google code ```svn checkout http://wro4j.googlecode.com/svn/trunk/```, later a git scm was preferred and since 1.2.0 the codebase was moved to [github](http://github.com/wro4j/wro4j). Though after each realease, there is an attempt to synchronize code base from github with google code, there may be some issues. Therefore, a checkout from github is preferred and guarantee that you have the latest source version. More about how you can checkout source can be found in the [GettingStarted](GettingStarted) page. 

## How can I change the Configuration Mode in the runtime
> Since 1.2.0 version, you can use JMX to change several properties, including configuration mode. Read more: [RuntimeConfigurationsUsingJMX] 

## What are available configuration options
> See the respective chapter in the [documentation](ConfigurationOptions.md).

## How to add easily custom resource processors
> The ConfigurableWroManagerFactory can help you. Read more [here](ConfigurableWroManagerFactory)

## How to define processors used during processing?
> You can control the processors chain, find out how to do that [here](http://code.google.com/p/wro4j/wiki/ProcessorsManagement)

## Can I use wro4j for multiple files coming from a outside domains
> Yes. It doesn't matter at all where the resources are located: locally, remotely, somewhere on the your local network, ftp or on your disc. All you have to do, is to specify all these location in the wro.xml file.

## Is wro4j only a runtime solution?
> * Starting with 1.2 branch, wro4j provides a build time solution - maven plugin. A detailed description of how you can use it can be found at this page: [MavenPlugin].
> * Starting with 1.3.4 version, there is a command line utility available, called [wro4j-runner](wro4j-runner)


## How to use TinyMCE with wro4j?
> Because TinyMCE loads the plugins dynamically, you can run into issues when attempting to "serve-up" the TinyMCE resources through wro4j. The solution is to create a group which contains all resources [needed by TinyMCE at once](http://blog.scriptito.com/compressing-tinymce-with-wro4j).

## How to configure wro4j different depending on environment
> You can use Spring for this, more details [here](ConfigureWro4jViaSpring)

## Is it possible to switch switch minimization off during development?
> If you are running wro4j in DEVELOPMENT configuration mode, you can switch minimization off by adding minimize=false parameter to resource url, like this:
```html
<script type="text/javascript" src="wro/all.js?minimize=false"></script>
```
> You can read more about it minimize aware processors at this [link](MinimizeAwareProcessor)

## I use an older version of wro4j (1.2.2 or 1.2.8). Should I upgrade to newer version?
> All the releases of wro4j are backward compatible. Therefore, you are encouraged to upgrade if you want to benefit from new features and bug fixes. The only problem you may have when switching from 1.2.x branch to 1.3.x branch is if you extend some wro4j code and do advanced stuff (which is very unlikely for regular users)

## Is it possible to deploy an application using wro4j to GAE (Google Application Engine)?
> GAE has some limitations on deployed applications. For instance, you cannot use JMX or you cannot create threads from within deployed application. By default wro4j enables JMX usage. In order to be sure that wro4j is compliant with GAE limitations, you have to apply some changes:
```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>
      ro.isdc.wro.http.WroFilter
    </filter-class>
    <init-param>
      <param-name>debug</param-name>
      <param-value>false</param-value>
    </init-param>
    <init-param>
      <param-name>jmxEnabled</param-name>
      <param-value>false</param-value>
    </init-param>
    <init-param>
      <param-name>cacheUpdatePeriod</param-name>
      <param-value>0</param-value>
    </init-param>
    <init-param>
      <param-name>modelUpdatePeriod</param-name>
      <param-value>0</param-value>
    </init-param>
  </filter>
```
> There are a couple of things to mention:
> **jmxEnabled** init param is set to false. This will disable the JMX usage of wro4j.
> **cacheUpdatePeriod** & **modelUpdatePeriod** are set to 0. That means that there will be no scheduler responsible for updating of the cache and model. By default these values are 0, so it could be easier just to not put any of these two init-params. 

## How to update the cache at runtime
> * If JMX is enabled you can trigger cache and/or model update through jconsole
> * Set the cacheUpdatePeriod and/or modelUpdatePeriod to 5 seconds in your web.xml (@see [GettingStarted](GettingStarted))
> * Trigger cache and/or model update through HTTP with a simple GET request:
> * for cache update - /wro/wroAPI/reloadCache
> * for model update - /wro/wroAPI/reloadModel
These requests will be processed only in DEVELOPMENT mode.


## Are there any performance concerns?
> When using wro4j build-time solution, you shouldn't worry. However if you prefer runtime solution, there are several aspects you must take care of:
> * depending on number of resources and type of processors, the bundling can be CPU expensive.
> * when using a public facing site (with a large number of users), don't forget to use DEPLOYMENT mode (equivalent of {{{ debug=false }}}). Also it is important to keep caching enabled (which is true by default). 
> * You can provide a custom [ExtendingCachingImplementation caching strategy] based on your application needs. 
> * You can turn on/off gzipping (use ```gzipResources=false```). This can be useful if you prefer CDN do handle the resource gzipping.
[Read more](IsWro4jSlow) about performance concerns.

## I'm using !CssDataUriPreProcessor but it doesn't change anything
> If CssDataUriPreProcessor or any other [similar processor](FallbackCssDataUriProcessor) doesn't change the images url's, the possible causes can be:
>
> * Images are too big. By default the processor applies base64 encoding transformation only on images < 32KB. You can change this limit by extending the processor and override the ```isReplaceAccepted(final String dataUri)``` method.
> * The other possible cause is that the images are not found or not available during processing.
> * If still the images are available and smaller than 32KB and still nothing happens, the reason is that the **CssUrlRewritingProcessor** was applied before the **CssDataUriPreProcessor**. It is important to apply the **CssDataUriPreProcessor** before **CssUrlRewritingProcessor**, otherwise images cannot be located properly by processor, leaving the outcome unchanged.

## I'm getting 404 instead minimized resource content and nothing is reported in logs
> The 404 is shown whenever wro4j failed to process the resources and continues with filter chaining. It worth mentioning that the logging is not  verbose at all with ERROR or INFO level (there were a lot of feature requests to make it less verbose). 
>
> If you are interested of problems occurred during processing, you have the following options:
> * Use development mode (debug=true) in configuration options. This will show the stacktrace in the page instead of continue with filter chaining.
> * Configure logger with DEBUG level for the following:
> --* ```ro.isdc.wro``` package for very detailed logging. Example: 
```
log4j.logger.ro.isdc=DEBUG
```
> --* ```ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator``` class if you are interested in exceptions reported by processors only. This is a more selective approach. Example:
```
log4j.logger.ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator=DEBUG
```
