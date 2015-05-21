# Introduction

There are two types of processors in Wro4j:
* Pre Processors - executed on a resource before the merge
* Post Processors - executed on a resource resulted after the merge.

Most of the time post processors can also act as a pre processors. This gives you enough flexibility to decide when exactly do you want a processor to be applied on the resource (before or after merging).

Any processor can be used as both: pre processor or post processor. In the future, both types of processors will have the same interface and there will be no distinction in its implementation. 

## *wro4j-core* module processors

| Alias        | Class name     | Description  |
| ------------ |:--------------:| ------------:|
| cssUrlRewriting | CssUrlRewritingProcessor | Rewrites background images url of the provided css content. Applied only on css resources. |
| cssClasspathUrlAuthorization (will be renamed in next release) | CssUrlAuthorizationProcessor | Available since 1.7.6. Auhorize all the url's rewritten during processing css resources. |
| cssImport | CssImportPreProcessor | responsible for handling css *@import* statements. This should be used as a pre processor and will be applied only on resources of type CSS. As a result of processing, import statements will be removed and the processed css file will contain a merged content of all imports. |
| lessCssImport | LessCssImportPreProcessor | same as *cssImport*, but also capable of handling ```@import-once``` statements. This processor is available since 1.6.3. |
| cssVariables | CssVariablesProcessor | Performs variable replacement. For more details see [CssVariablesSupport](CssVariablesSupport).  |
| cssCompressor | CssCompressorProcessor | A css compressor, implemented by Andy Roberts. |
| semicolonAppender | SemicolonAppenderPreProcessor | Adds a semicolon (';') character to the end of each js file if it is missing, in order to avoid any problems after js resources are merged. |
| cssDataUri | [CssDataUriPreProcessor](Base64DataUriSupport) | Rewrites background images by replacing the url with data uri of the image. For more details see [Base64DataUriSupport](Base64DataUriSupport) |
| duplicateAwareCssDataUri | [DuplicatesAwareCssDataUriPreProcessor](DuplicatesAwareCssDataUriPreProcessor) | Similar to cssDataUri, but doesn't replace with dataUri when the same image is used more than once. |
| fallbackCssDataUri | [FallbackCssDataUriProcessor](FallbackCssDataUriProcessor) | Similar to cssDataUri, but uses both: base64 encoded & original url in order to work in browsers which do not support dataUri's. This processor is available since 1.4.7. |
| cssMinJawr | JawrCssMinifierProcessor | Css minimizer. The implementation is taken from jawr framework. |
| cssMin | CssMinProcessor | A simple css minimizer, which removes comments and unnecessary whitespaces. |
| jsMin | JSMinProcessor | Use JSMin utility for js compression |
| conformColors | ConformColorsCssProcessor | Transforms named colors from css to #rgb format. |
| variablizeColors | VariablizeColorsCssProcessor | Extracts all the colors used in css add details about how many times it was used.  |
| - | [CopyRightKeeperProcessorDecorator](CopyRightKeeperProcessorDecorator) | A decorator for any processor. Inspects the resource for copyright (licence) header and inserts them back if the decorated processor removes them. |
| - | [PlaceholderProcessor](PlaceholderProcessor) |  parse a resource and search for placeholders of this type: ```${}``` and replace them with the values found in a map provided the by client.| 
| - | ExtensionsAwareProcessorDecorator | Decorates any processor with ability to process only resources of a certain extension. |


## *wro4j-extensions* module processors

| Alias        | Class name     | Description  |
| ------------ |:--------------:| ------------:|
| yuiCssMin | YUICssCompressorProcessor|  Use YUI css compression utility for processing a css resource. |
| dojoShrinksafe | DojoShrinksafeCompressorProcessor | Compresses javascript code using compressor implemented by Dojo Shrinksafe utility |
| uglifyJs | UglifyJsProcessor | Compress js using [uglifyJs utility](https://github.com/mishoo/UglifyJS). |
| beautifyJs | UglifyJsProcessor | Does exactly the opposite of uglifyJs. |
| packerJs | PackerJsProcessor | Uses [Dean Edwards packer utility](http://dean.edwards.name/packer/) to pack js resources. |
| lessCss | [LessCssProcessor](LessCssSupport) | Transforms less css code into vanilla css. | 
| sassCss | [SassCssProcessor](SassCssSupport) | Transforms sass css code into vanilla css. |
| rubySassCss | [RubySassCssProcessor](RubySassCss) | Available since 1.4.6. Similar to sassCss, but uses ruby instead of rhino & js. |
| googleClosureSimple | GoogleClosureCompressorProcessor | Compress javascript code with google closure compiler using SIMPLE_OPTIMIZATIONS |
| googleClosureAdvanced | GoogleClosureCompressorProcessor | Compress javascript code with google closure compiler using ADVANCED_OPTIMIZATIONS |
| googleClosureWhitespace | GoogleClosureCompressorProcessor | Compress javascript code with google closure compiler using WHITESPACE_ONLY. Available since 1.6.2 |
| coffeeScript | [CoffeeScriptSupport](CoffeeScriptProcessor) | Uses [coffee script](http://jashkenas.github.com/coffee-script/) to compile to javascript code. |
| nodeCoffeeScript | NodeCoffeeScriptProcessor | Available since 1.6.0. Uses node.js to compile coffee-script into javascript. Not supported on all platforms |
| literateCoffeeScript | LiterateCoffeeScriptProcessor |  [Literate CoffeeScript](http://www.coffeescriptlove.com/2013/02/literate-coffeescript.html), an implementation of literate programming. The implementation uses either node (if detected) or rhino as a fallback.|
| rhinoLiterateCoffeeScript | RhinoLiterateCoffeeScriptProcessor |  Similar to **literateCoffeeScript** but uses rhino explicitly.|
| nodeLiterateCoffeeScript | NodeLiterateCoffeeScriptProcessor |  Similar to **literateCoffeeScript** but uses node explicitly. This processor works only if the coffee script npm is installed. |
| cjson-pack | [CJsonProcessor](CJsonProcessor) | Compress JSON objects using CJson algorithm. |
| cjson-unpack | [CJsonProcessor](CJsonProcessor) | Uncompress JSON objects previously compressed with CJson algorithm. |
| jsonh-pack | [JsonHPackProcessor](JsonHPackProcessor) | Compress JSON objects using HPack algorithm. |
| jsonh-unpack | [JsonHPackProcessor] | Uncompress JSON objects previously compressed with HPack algorithm. |
| jsHint | JsHintProcessor | Does the static analysis of the javascript code using [jsHint](http://jshint.com/) code quality tool.|
| jsLint | JsLintProcessor | Does the static analysis of the javascript code using [jsLint](http://jslint.com/) code quality tool. Available since 1.4.2 release.|
| cssLint| CssLintProcessor| Does the static analysis of the css code using [http://csslint.net/ cssLint] code quality tool. |
| nodeCssLint| NodeCssLintProcessor| Uses node.js to compile less into css. Available since 1.5.0. Not supported on all platforms. |
| dustJs | [DustJsProcessor](DustJsProcessor) | Uses [DustJs](http://akdubya.github.com/dustjs/) engine to compile a template into javascript. Available since 1.4.5|
| handlebarsJs | [HandlebarsJsProcessor](HandlebarsJsProcessor) | Compiles [HandlebarsJS](http://handlebarsjs.com/precompilation.html) templates to javascript. Available since 1.4.7|
| hoganJs | [HoganJsProcessor](HoganJsProcessor) | Compiles [hogan.js](http://twitter.github.com/hogan.js/) templates to javascript. Available since 1.4.5|
| bourbonCss | [BourbonCssProcessor](BourbonCssProcessor) | A processor to support the [bourbon mixins library](http://thoughtbot.com/bourbon/) for sass. Available since 1.4.7 |
| typeScript | TypeScriptProcessor | Compiles the [typescript](http://www.typescriptlang.org/) code into javascript. Available since 1.6.0. Uses node.js for compilation. Not supported on all platforms. |
| less4j | Less4jProcessor | Available since 1.6.0. Compiles less into css. Uses [less4j](https://github.com/SomMeri/less4j) open source java library | 
| emberJs | EmberJsProcessor | Available since 1.6.2. Compiles templates into javascript using [emberjs](http://emberjs.com/) library. |
| ngMin | NgMinProcessor | Available since 1.7.4. [ngMin](http://www.thinkster.io/pick/XlWneEZCqY/angularjs-ngmin) is a "pre-minifier" for AngularJS. It modifies Angular code to prevent errors that may arise from minification. The implementation assume the required npm is installed. |
| ngAnnotate | NgAnnotateProcessor | Available since 1.7.8. [Ng-annotate](https://github.com/olov/ng-annotate) Adds and removes AngularJS dependency injection annotations. The implementation assume the required npm is installed. |
