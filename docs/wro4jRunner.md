### Related pages 
[AvailableProcessors](AvailableProcessors) - a list of processors provided by wro4j and their alias.
[original blog post](http://web-resource-optimization.blogspot.com/2011/02/simple-client-side-build-system-with.html).


# Introduction
Starting with wro4j-1.3.4 release, it is not limited anymore to java development environment. If, for instance, you are developing a js framework with many small modules (ex: jquery, yui, mootools, etc) or if you are working on the client-side of a large web project, then wro4j can help you to easily organize all your static resources (css/js) and build them (merge and minimize) using a simple command line tool. The only prerequisite is to install jdk-1.6 on your machine.

A typicall a client-side project uses ant build script (build.xml) to describe how the js resources are merged and minimized. Also the supported compressors is limited to one only. Switching to another compressor is not supported. Using an ant script can be a good solution, but it also can be quite complex. Having a verbose and complex script is very hard to understand and maintain. Isn't there a simpler solution?

Since version (1.3.8) of wro4j-runner allows you to validate your javascript resources (using jsHint) and css resources (using cssLint). 

Since version 1.4.2 the jsLint support is available.

# Installation Steps
With new wro4j command line tool, you can achieve the same results with minimum effort. All you have to do, is to follow the following steps:

### Add wro4j-runner
 In our case, we add it to the lib folder (just where other jar files resides). The default relative context path depends on the location of the jar (this can be changed with an argument we'll get back to this later).

### Create wro.xml file and add it in the same folder where the jar is located
This file describes how you want your resources to be merged and the resources location. For more details about wro.xml, visit this page. Here is an example:
```xml
<groups xmlns="http://www.isdc.ro/wro">
  <group name="all">
    <js>/jquery-1.4.2.js</js>
    <js>/../src/**.js</js> 
  </group>
</groups>
```
### Run the following in your console
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar
```
As a result, a new folder (called 'wro') will be created. It will have one file: all.js containing a merged content of all js files from ui folder (as described in wro.xml).

# Configuration
Of course, it would be nice to have the all.js file compressed. In order to do that, make a small change to the console script:
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar -m
```
Adding -m attribute, inform the wro4j runner to minimize the scripts. By default, it uses JSMin processor for js compression. You can easily switch this compressor with other, here is an example:
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar -m -c uglifyJs
```
This will inform wro4j runner to use UglifyJs compressor instead. Similarly, you can use other compressor.
You don't have to worry when invoking the wro4j-runner with wrong arguments, it will inform you about the cause of the problem and in some cases can suggest possible solutions. Also, when everything is ok, you will see in command line the details about processing and total duration of the operation.

# Supported Processors
Currently wro4j-runner support the following js compressors:
  * uglifyJs - For UglifyJs compressor
  * beautifyJs - Exactly the opposite of the uglifyJs, it does what it says - makes compressed code readable.
  * googleClosureSimple - For Google Closure Compiler with simple optimization
  * googleClosureAdvanced - For Google Closure Compiler with advanced optimization
  * yuiJsMin - For YUI compressor with no munge
  * yuiJsMinAdvanced - For YUI compressor with munge
  * packerJs - Uses Dean Edwards Packer compressor (version 3.1)
  * dojoShrinksafe - Uses Dojo Shrinksafe compressor.
### Processors available since 1.3.8 version
  * jsMin - Uses Douglas Crockford [jsMin utility](http://www.crockford.com/javascript/jsmin.html)
  * yuiCssMin - minimize css code using YUI compressor utility
  * cssMinJawr - minimize css code using the compressor code used by jawr framework.
  * cssCompressor - uses css compressor written by Andy Roberts.
  * cssMin - yet another css min utility
  * cssLint - validates css code using [CSSLint utility](https://github.com/stubbornella/csslint)
  * jsHint - validates js code using [JsHint utility](http://jshint.com/)
  * cssDataUri - Rewrites background images in css files by replacing the url with data uri of the image
  * cjson-pack - Pack a json using cjson pack aglorithm (http://stevehanov.ca/blog/index.php?id=104)
  * cjson-unpack - Unpack the json packed with cjson algorithm
  * jsonh-pack - Pack a json using [json hpack aglorithm](https://github.com/WebReflection/json.hpack)
  * jsonh-unpack - Unpack the json packed with json hpack algorithm
  * lessCss - transform lessCss code into css, using [less.js](https://github.com/cloudhead/less.js).
  * sassCss - transform sassCss code into css, using [sass.js](https://github.com/visionmedia/sass.js)
  * coffeeScript - transform coffeeScript code into javascript, using [coffee-script](http://jashkenas.github.com/coffee-script/)
  * conformColors - parse css files and transform all colors to #rgb format.
  * cssVariables - parse a css using a [special syntax](http://disruptive-innovations.com/zoo/cssvariables/) and replace variables.

For a complete list of supported processors see this page: [AvailableProcessors](AvailableProcessors)

You can easily switch between any of the preferred compressors, depending on your tastes or preferences. Maybe for some projects one compressor suites better than other. The nice part is that wro4j can support any possible existing javascript compressors. For more details visit wro4j project home page.

# wro4j-runner arguments
Here you'll find all the arguments supported by wro4j runner tool.

  * -m or --minimize - Turns on the minimization by applying the default or specified compressor
  * -c or --compressor or --preProcessors - A comma separated list of pre processors.
  * --postProcessors - A comma separated list of post processors. (this option is available since 1.4.5) 
  * -i or --ignoreMissingResources - This is useful when you want the runner to do its job even when you specify an invalid resource in your wro.xml. By default missing resources are not ignored.
  * --targetGroups ${GROUPS} - Comma separated list of groups (defined in wro.xml) to process. If you don't specify this argument, all existing groups will be processed and for each of them will be created a file.
  * --destinationFolder ${PATH} - The folder where the target groups will be generated. By default it will create a folder called wro
  * --wroFile ${PATH} - location of the wro.xml file. By default runner will search it in the current folder.
  * --contextFolder ${PATH} - folder used as a root of the context relative resources (or where to search when you have a resource starting with / character). By default this is the current folder.

# Examples
### Running lessCss with wro4j-runner
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar --preProcessors lessCss
```
### Running cssLint with wro4j-runner
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar -c cssLint
```
### Running jsHint with wro4j-runner
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar -c jsHint
```

### Use several pre processors
```
java -jar wro4j-runner-1.4.5-jar-with-dependencies.jar -m --preProcessors cssLint,yuiJsMin,cssMin 
```