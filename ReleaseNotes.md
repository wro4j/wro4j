### Release 1.7.7 ###
**Date: 25 Sep 2014**
  * [Issue854](https://code.google.com/p/wro4j/issues/detail?id=854)	 Jshint plugin should not try processing css resources
  * [Issue885](https://code.google.com/p/wro4j/issues/detail?id=885)	 Update less4j dependency to latest version
  * [Issue891](https://code.google.com/p/wro4j/issues/detail?id=891)	 Optimize hashing implementation
  * [Issue893](https://code.google.com/p/wro4j/issues/detail?id=893)	 Upgrade google closure compiler dependency
  * [Issue894](https://code.google.com/p/wro4j/issues/detail?id=894)	 Upgrade jruby dependency (performance optimization)
  * [Issue895](https://code.google.com/p/wro4j/issues/detail?id=895)	 ChainedProcessor uses WroTestUtils which uses junit, making it not work at runtime
  * [Issue896](https://code.google.com/p/wro4j/issues/detail?id=896)	 Confusing exception with incrementalBuildEnabled
  * [Issue897](https://code.google.com/p/wro4j/issues/detail?id=897)	 Optimize how often files are checksummed
  * [Issue900](https://code.google.com/p/wro4j/issues/detail?id=900)	 Prevent buildDirectory from being null
  * [Issue902](https://code.google.com/p/wro4j/issues/detail?id=902)	 Race condition in LazyProcessorDecorator when parallelProcessing is enabled
  * [Issue903](https://code.google.com/p/wro4j/issues/detail?id=903)	 Update coffee-script webjars dependency to latest version

### Release 1.7.6 ###
**Date: 18 Jun 2014**
  * [Issue865](https://code.google.com/p/wro4j/issues/detail?id=865)	 wro4j-maven-plugin : cssUrlRewriting act differently on Windows and Linux
  * [Issue869](https://code.google.com/p/wro4j/issues/detail?id=869)	 "MinimizeEnabled" not handled by wro.properties
  * [Issue871](https://code.google.com/p/wro4j/issues/detail?id=871)	 DisableCache clears the Model before the ResourceAuthorizationManager access it
  * [Issue872](https://code.google.com/p/wro4j/issues/detail?id=872)	 ResourceProxyRequestHandler throwing UnauthorizedRequestException when resource uri contains question mark and pound
  * [Issue876](https://code.google.com/p/wro4j/issues/detail?id=876)	 Update less4j dependency to latest version
  * [Issue877](https://code.google.com/p/wro4j/issues/detail?id=877)	 CssUrlRewritingProcessor does not handle properly empty url reference [url("")]
  * [Issue878](https://code.google.com/p/wro4j/issues/detail?id=878)	 Add provision to take context path into account - Build Time Solution using MavenPlugin
  * [Issue879](https://code.google.com/p/wro4j/issues/detail?id=879)	 Adding CssUrlAuthorizationProcessor post processor
  * [Issue880](https://code.google.com/p/wro4j/issues/detail?id=880)	 Update webjars-locator dependency to latest version
  * [Issue882](https://code.google.com/p/wro4j/issues/detail?id=882)	 Update google closure dependency
  * [Issue883](https://code.google.com/p/wro4j/issues/detail?id=883)	 Update dependency of emberjs webjar

### Release 1.7.5 ###
**Date: 9 Apr 2014**
  * [Issue860](https://code.google.com/p/wro4j/issues/detail?id=860)	mvn wro4j:run fails due to optional configuration missing
  * [Issue861](https://code.google.com/p/wro4j/issues/detail?id=861)	Less4jProcessor doesn't process properly imports on windows platform
  * [Issue862](https://code.google.com/p/wro4j/issues/detail?id=862)	Update less4j dependency to latest version
  * [Issue864](https://code.google.com/p/wro4j/issues/detail?id=864)	File descriptor leak
  * [Issue866](https://code.google.com/p/wro4j/issues/detail?id=866)	Update handlebars webjar dependency version

### Release 1.7.4 ###
**Date: 21 Mar 2014**
  * [Issue511](https://code.google.com/p/wro4j/issues/detail?id=511)	ResourceWatcher should check for changes in parallel
  * [Issue715](https://code.google.com/p/wro4j/issues/detail?id=715)	java.lang.NullPointerException in AbstractNodeWithFallbackProcessor.java:62
  * [Issue830](https://code.google.com/p/wro4j/issues/detail?id=830)	Using 'parallelProcessing' in Maven plugin produces intermittent NullPointerException during build process
  * [Issue834](https://code.google.com/p/wro4j/issues/detail?id=834)	The grails plugin sometimes returns 404 for first request
  * [Issue835](https://code.google.com/p/wro4j/issues/detail?id=835)	Upgrade coffee-script webjar dependency
  * [Issue836](https://code.google.com/p/wro4j/issues/detail?id=836)	Upgrade emberjs webjar dependency
  * [Issue837](https://code.google.com/p/wro4j/issues/detail?id=837)	Upgrade handlebars webjar dependency
  * [Issue838](https://code.google.com/p/wro4j/issues/detail?id=838)	Resource watcher doesn't invalidate cache when change detected for wro4j-grails-plugin
  * [Issue840](https://code.google.com/p/wro4j/issues/detail?id=840)	ResourceProxyRequestHandler accept method failing to match in v1.7.3, results in 404s
  * [Issue841](https://code.google.com/p/wro4j/issues/detail?id=841)	Update jruby dependency version
  * [Issue842](https://code.google.com/p/wro4j/issues/detail?id=842)	Create Less4jProcessorFilter
  * [Issue843](https://code.google.com/p/wro4j/issues/detail?id=843)	ro.isdc.wro.extensions.processor.js.NgMinProcessor is never supported
  * [Issue844](https://code.google.com/p/wro4j/issues/detail?id=844)	Upgrade jshint webjar version
  * [Issue845](https://code.google.com/p/wro4j/issues/detail?id=845)	Less4jProcessor should process import statements
  * [Issue850](https://code.google.com/p/wro4j/issues/detail?id=850)	Update less4j dependency to latest version
  * [Issue851](https://code.google.com/p/wro4j/issues/detail?id=851)	Improve google closure processor in multithreaded environment
  * [Issue853](https://code.google.com/p/wro4j/issues/detail?id=853)	ServletContextUriLocator fails to use requestDispatcher
  * [Issue855](https://code.google.com/p/wro4j/issues/detail?id=855)	cssImport tries to import absolute URLs (http://...)
  * [Issue858](https://code.google.com/p/wro4j/issues/detail?id=858)	Fix RubySass compile error for non-ascii characters
  * [Issue859](https://code.google.com/p/wro4j/issues/detail?id=859)	AbstractProcessorFilter doesn't handle 30x reponse status code


### Release 1.7.3 ###
**Date: 23 Jan 2014**
  * [Issue789](https://code.google.com/p/wro4j/issues/detail?id=789)	java.lang.StringIndexOutOfBoundsException: at org.webjars.WebJarAssetLocator.aggregateFile()
  * [Issue813](https://code.google.com/p/wro4j/issues/detail?id=813)	Combining a javascript file that ends with a comment (without a newline) results in unexpected output
  * [Issue814](https://code.google.com/p/wro4j/issues/detail?id=814)	Update less4j dependency to latest version
  * [Issue822](https://code.google.com/p/wro4j/issues/detail?id=822)	ResourceWatcher should allow check for changes asynchronously
  * [Issue824](https://code.google.com/p/wro4j/issues/detail?id=824)	Context leaks when requests are nested
  * [Issue826](https://code.google.com/p/wro4j/issues/detail?id=826)	UriLocator fails to retrieve valid resource when it contains question mark
  * [Issue827](https://code.google.com/p/wro4j/issues/detail?id=827)	Update jshint dependency
  * [Issue828](https://code.google.com/p/wro4j/issues/detail?id=828)	Update emberjs dependency to latest version.
  * [Issue829](https://code.google.com/p/wro4j/issues/detail?id=829)	Update handlebars webjar dependency to latest version
  * [Issue832](https://code.google.com/p/wro4j/issues/detail?id=832)	BuildContextHolder used for incremental build does not clean up properly

### Release 1.7.2 ###
**Date: 5 Nov 2013**
  * [Issue660](https://code.google.com/p/wro4j/issues/detail?id=660)	JsMin fails when processing a regex
  * [Issue785](https://code.google.com/p/wro4j/issues/detail?id=785)	Wro4jCommandLineRunner not governed by wro.properties
  * [Issue786](https://code.google.com/p/wro4j/issues/detail?id=786)	Extending customization using SPI doesn't work with maven plugin
  * [Issue787](https://code.google.com/p/wro4j/issues/detail?id=787)	wro4j-runner should allow custom location of wro.properties
  * [Issue788](https://code.google.com/p/wro4j/issues/detail?id=788)	Multiple context folders for maven plugin
  * [Issue790](https://code.google.com/p/wro4j/issues/detail?id=790)	ignoreMissingResources property from wro.properties ignored when using maven plugin
  * [Issue792](https://code.google.com/p/wro4j/issues/detail?id=792)	Incremental build support for linters in maven plugin
  * [Issue793](https://code.google.com/p/wro4j/issues/detail?id=793)	Update emberjs dependency version
  * [Issue794](https://code.google.com/p/wro4j/issues/detail?id=794)	Update handlebars dependency version
  * [Issue800](https://code.google.com/p/wro4j/issues/detail?id=800)	groupNameMappingFile doesn't support filepaths that don't yet exist
  * [Issue802](https://code.google.com/p/wro4j/issues/detail?id=802)	Expose an alias for SingleLineCommentStripperProcessor
  * [Issue803](https://code.google.com/p/wro4j/issues/detail?id=803)	Allow wro4j-maven-plugin callers to skip execution
  * [Issue805](https://code.google.com/p/wro4j/issues/detail?id=805)	upgrade to latest version of jruby
  * [Issue807](https://code.google.com/p/wro4j/issues/detail?id=807)	Listener for resourceWatcher

### Release 1.7.1 ###
**Date: 19 Sept 2013**
  * [Issue536](https://code.google.com/p/wro4j/issues/detail?id=536)	parallelize maven plugin
  * [Issue745](https://code.google.com/p/wro4j/issues/detail?id=745)	Pooling processors may leak timer threads
  * [Issue746](https://code.google.com/p/wro4j/issues/detail?id=746)	Loading WebJar assets fails if path contains whitespace
  * [Issue747](https://code.google.com/p/wro4j/issues/detail?id=747)	Aggregate Path Prefix for ImageUrlRewriter calculated incorrectly on Windows
  * [Issue749](https://code.google.com/p/wro4j/issues/detail?id=749)	ResourceWatcher is not working since 1.7.0
  * [Issue751](https://code.google.com/p/wro4j/issues/detail?id=751)	No pre/postProcessors with extended ConfigurableWroManagerFactory
  * [Issue752](https://code.google.com/p/wro4j/issues/detail?id=752)	Maven plugin won't run on ARM architecture - jruby failure
  * [Issue753](https://code.google.com/p/wro4j/issues/detail?id=753)	jruby-complete pulls in unwanted classes
  * [Issue757](https://code.google.com/p/wro4j/issues/detail?id=757)	Allow turning off of resource minification through JMX MBean
  * [Issue760](https://code.google.com/p/wro4j/issues/detail?id=760)	failFast option is causing Exception in onAfterExecute
  * [Issue764](https://code.google.com/p/wro4j/issues/detail?id=764)	Handling Request, forward and error dispatches with grails plugin
  * [Issue773](https://code.google.com/p/wro4j/issues/detail?id=773)	Update less4j dependency to latest version
  * [Issue774](https://code.google.com/p/wro4j/issues/detail?id=774)	Move wro4j-examples projects to a dedicated repository
  * [Issue775](https://code.google.com/p/wro4j/issues/detail?id=775)	Move wro4j-grails project to a dedicated repository
  * [Issue777](https://code.google.com/p/wro4j/issues/detail?id=777)	add SERVLET\_CONTEXT\_ONLY LocatorStrategy
  * [Issue779](https://code.google.com/p/wro4j/issues/detail?id=779)	Incremental build support for maven plugin
  * [Issue782](https://code.google.com/p/wro4j/issues/detail?id=782)	Parallel pre processing is not enabled with maven plugin.


### Release 1.7.0 ###
**Date: 10 Jun 2013**
  * [Issue619](https://code.google.com/p/wro4j/issues/detail?id=619)	Simplify jsHint upgrade without a wro4j release required
  * [Issue631](https://code.google.com/p/wro4j/issues/detail?id=631)	Import of model with no groups or model not found crashes the model creation
  * [Issue633](https://code.google.com/p/wro4j/issues/detail?id=633)	ResourceWatcherUpdatePeriod clear the cache also when there is no change
  * [Issue642](https://code.google.com/p/wro4j/issues/detail?id=642)	Create webjar resource locator
  * [Issue648](https://code.google.com/p/wro4j/issues/detail?id=648)	Update JsHintProcessor to latest dependency
  * [Issue692](https://code.google.com/p/wro4j/issues/detail?id=692)	Upgrade less4j to latest version
  * [Issue693](https://code.google.com/p/wro4j/issues/detail?id=693)	Update to latest version of google closure
  * [Issue695](https://code.google.com/p/wro4j/issues/detail?id=695)	AbstractCssImportPreProcessor leaks ThreadLocal variable during Tomcat shutdown
  * [Issue696](https://code.google.com/p/wro4j/issues/detail?id=696)	Error with v1.6.3 and CSS data URLs
  * [Issue697](https://code.google.com/p/wro4j/issues/detail?id=697)	wro4j-runner missing required dependency
  * [Issue701](https://code.google.com/p/wro4j/issues/detail?id=701)	Allow all errors to be found before the build fails.
  * [Issue702](https://code.google.com/p/wro4j/issues/detail?id=702)	wro4j-maven-plugin doesn't persist fingerprints for imported css
  * [Issue703](https://code.google.com/p/wro4j/issues/detail?id=703)	Rhino Less CSS processor breaks on // in a string, eg http://example.com
  * [Issue704](https://code.google.com/p/wro4j/issues/detail?id=704)	Less4j warning log improvement
  * [Issue705](https://code.google.com/p/wro4j/issues/detail?id=705)	Create filter responsible for gzipping resource contents
  * [Issue709](https://code.google.com/p/wro4j/issues/detail?id=709)	Upgrade rhinoCoffeeScriptProcessor to version 1.6.2
  * [Issue711](https://code.google.com/p/wro4j/issues/detail?id=711)	import-once not working
  * [Issue716](https://code.google.com/p/wro4j/issues/detail?id=716)	Update jslint to latest version
  * [Issue720](https://code.google.com/p/wro4j/issues/detail?id=720)	JMX bean is not unregistered during destroy
  * [Issue722](https://code.google.com/p/wro4j/issues/detail?id=722)	NodeCoffeeScriptProcessor does not support additional command-line options, such as --bare
  * [Issue723](https://code.google.com/p/wro4j/issues/detail?id=723)	No exception thrown when using invalid resource uri is used
  * [Issue724](https://code.google.com/p/wro4j/issues/detail?id=724)	DefaultUriLocator should use all locators found in classpath
  * [Issue726](https://code.google.com/p/wro4j/issues/detail?id=726)	Upgrade handlebars processor to latest version
  * [Issue728](https://code.google.com/p/wro4j/issues/detail?id=728)	Improve problem reporting for Less4jProcessor
  * [Issue729](https://code.google.com/p/wro4j/issues/detail?id=729)	Upgrade ember.js processor to latest version
  * [Issue730](https://code.google.com/p/wro4j/issues/detail?id=730)	Incremental change build should process the target group when the target folder does not exist anymore
  * [Issue731](https://code.google.com/p/wro4j/issues/detail?id=731)	Reduce logging of the JSHint maven plugin
  * [Issue732](https://code.google.com/p/wro4j/issues/detail?id=732)	Incremental build feature doesn't use `<targetGroups>` configuration
  * [Issue733](https://code.google.com/p/wro4j/issues/detail?id=733)	consoleStripper regex changes.
  * [Issue736](https://code.google.com/p/wro4j/issues/detail?id=736)	ConfigurableWroFilter ignores managerFactoryClassName configuration property
  * [Issue739](https://code.google.com/p/wro4j/issues/detail?id=739)	Improve error message when jshint is provided with invalid options
  * [Issue740](https://code.google.com/p/wro4j/issues/detail?id=740)	Non-synchronized WeakHashMap causes infinite loop in Injector
  * [Issue742](https://code.google.com/p/wro4j/issues/detail?id=742)	CssUrlRewriting issue when dealing with classpath resource referring context relative resources
  * [Issue743](https://code.google.com/p/wro4j/issues/detail?id=743)	Resources served through ResourceProxyRequestHandler do not use cache headers
  * [Issue744](https://code.google.com/p/wro4j/issues/detail?id=744)	Empty content gzip response issue



### Release 1.6.3 ###
**Date: 11 Mar 2013**

  * [Issue458](https://code.google.com/p/wro4j/issues/detail?id=458)	LifecycleCallback should be thread safe.
  * [Issue544](https://code.google.com/p/wro4j/issues/detail?id=544)	Improved configuration of the preferred model DSL
  * [Issue558](https://code.google.com/p/wro4j/issues/detail?id=558)	Support for abstract group in Groovy DSL
  * [Issue628](https://code.google.com/p/wro4j/issues/detail?id=628)	WroManager should be immutable
  * [Issue650](https://code.google.com/p/wro4j/issues/detail?id=650)	cssImport processor translates urls incorrectly
  * [Issue654](https://code.google.com/p/wro4j/issues/detail?id=654)	Create RhinoTypeScriptProcessor.
  * [Issue655](https://code.google.com/p/wro4j/issues/detail?id=655)	Less4jProcessor should log all errors when fails
  * [Issue656](https://code.google.com/p/wro4j/issues/detail?id=656)	Upgrade cssLintProcessor
  * [Issue658](https://code.google.com/p/wro4j/issues/detail?id=658)	Upgrade ember.js to latest version
  * [Issue659](https://code.google.com/p/wro4j/issues/detail?id=659)	Maven-plugin: ReportFormat set in pom file seem to not be read
  * [Issue661](https://code.google.com/p/wro4j/issues/detail?id=661)	Add a reportFormat option to the JSHint processor that works similar to the CSSLint processor's reportFormat option
  * [Issue664](https://code.google.com/p/wro4j/issues/detail?id=664)	Running JSHint processor in an Execution step via the WRO4J 1.6.2 Maven Plugin is outputting `<lint>` rather than `<jslint>` element
  * [Issue665](https://code.google.com/p/wro4j/issues/detail?id=665)	Deprecate InjectableWroManagerFactoryDecorator
  * [Issue668](https://code.google.com/p/wro4j/issues/detail?id=668)    groupNameMappingFile and incremental eclipse builds
  * [Issue670](https://code.google.com/p/wro4j/issues/detail?id=670)	Minimize dependencies of SmartWroModelFactory
  * [Issue672](https://code.google.com/p/wro4j/issues/detail?id=672)	Last-modified and Expires headers use system-default Locale
  * [Issue675](https://code.google.com/p/wro4j/issues/detail?id=675)	Get rid of largest dependencies for wro4j-runner
  * [Issue679](https://code.google.com/p/wro4j/issues/detail?id=679)	Ember precompiler for 1.0.0 RC1
  * [Issue681](https://code.google.com/p/wro4j/issues/detail?id=681)	HoganJs processor compilation missing semicolon
  * [Issue682](https://code.google.com/p/wro4j/issues/detail?id=682)	Allow Closure Compiler errors to fail the build
  * [Issue684](https://code.google.com/p/wro4j/issues/detail?id=684)	cssImport should support less import-once
  * [Issue685](https://code.google.com/p/wro4j/issues/detail?id=685)	Upgrade less4j to latest version
  * [Issue686](https://code.google.com/p/wro4j/issues/detail?id=686)	Upgrade rhinoCoffeeScriptProcessor to version 1.6.1
  * [Issue687](https://code.google.com/p/wro4j/issues/detail?id=687)	ClasspathUriLocator fails to find wildcards resources when the application path has spaces in it
  * [Issue688](https://code.google.com/p/wro4j/issues/detail?id=688)	Additional JSHint property: failThreshold
  * [Issue690](https://code.google.com/p/wro4j/issues/detail?id=690)	JSHint execution summary


### Release 1.6.2 ###
**Date: 10 Jan 2013**
  * [Issue480](https://code.google.com/p/wro4j/issues/detail?id=480)	Css url rewriting doesn't compute replace properly font rule with multiple url's
  * [Issue622](https://code.google.com/p/wro4j/issues/detail?id=622)	Upgrade to latest version of less4j
  * [Issue623](https://code.google.com/p/wro4j/issues/detail?id=623)	Invalid Import of xml model shows misleading stacktrace
  * [Issue624](https://code.google.com/p/wro4j/issues/detail?id=624)	WildcardExpanderModelTransformer throws failure warnings if no assets are found
  * [Issue625](https://code.google.com/p/wro4j/issues/detail?id=625)	setting up encoding in command line
  * [Issue632](https://code.google.com/p/wro4j/issues/detail?id=632)	wro4j:jshint triggers slf4j warning
  * [Issue635](https://code.google.com/p/wro4j/issues/detail?id=635)	change csslint.xml root xml element from csslint to lint
  * [Issue636](https://code.google.com/p/wro4j/issues/detail?id=636)	Create emberjs processor
  * [Issue637](https://code.google.com/p/wro4j/issues/detail?id=637)	Upgrade HandlebarsJsProcessor
  * [Issue638](https://code.google.com/p/wro4j/issues/detail?id=638)	The groupName does not strip jsessionID
  * [Issue643](https://code.google.com/p/wro4j/issues/detail?id=643)	wro4j-core doesn't compile with jdk 1.5
  * [Issue644](https://code.google.com/p/wro4j/issues/detail?id=644)	Providers loaded from ServiceLoader cannot override default providers
  * [Issue645](https://code.google.com/p/wro4j/issues/detail?id=645)	Provide an alias for google closureProcessor using WHITESPACE\_ONLY optimization level
  * [Issue646](https://code.google.com/p/wro4j/issues/detail?id=646)	Update google closure dependency version
  * [Issue647](https://code.google.com/p/wro4j/issues/detail?id=647)	Update rhinoLessCss processor to 1.3.3
  * [Issue649](https://code.google.com/p/wro4j/issues/detail?id=649)	Update JsLintProcessor with latest jslint version



### Release 1.6.1 ###
**Date: 25 Nov 2012**
  * [Issue598](https://code.google.com/p/wro4j/issues/detail?id=598)	NPE with GoogleClosureCompressorProcessor in wro4j 1.6.0
  * [Issue599](https://code.google.com/p/wro4j/issues/detail?id=599)	Make DustJs compiler configurable with System property
  * [Issue602](https://code.google.com/p/wro4j/issues/detail?id=602)	Resource watcher thinks files have been modified when they in fact have not, after upgrade to 1.6
  * [Issue603](https://code.google.com/p/wro4j/issues/detail?id=603)	JawrCssMinifier creates overly long lines
  * [Issue604](https://code.google.com/p/wro4j/issues/detail?id=604)	Upgrade Less4j dependency to latest version
  * [Issue606](https://code.google.com/p/wro4j/issues/detail?id=606)	jshint maven goal fails with an exception during report generation
  * [Issue608](https://code.google.com/p/wro4j/issues/detail?id=608)	Included Hogan JS Processor Does Not Appear Useable
  * [Issue613](https://code.google.com/p/wro4j/issues/detail?id=613)	Update JSHint & JsLint dependencies
  * [Issue617](https://code.google.com/p/wro4j/issues/detail?id=617)	JsHint xml report does not respect format expected by Jenkins
  * [Issue618](https://code.google.com/p/wro4j/issues/detail?id=618)	JsHint generated xml report is empty when there are errors




### Release 1.6.0 ###
**Date: 24 Oct 2012**
  * [Issue465](https://code.google.com/p/wro4j/issues/detail?id=465)	The cache key should be configurable with custom attributes
  * [Issue563](https://code.google.com/p/wro4j/issues/detail?id=563)	ResourceWatcher cannot detect change of resources referred by @import directive
  * [Issue565](https://code.google.com/p/wro4j/issues/detail?id=565)	Close of FileOutputStream in Wro4jMojo.writeGroupNameMap()
  * [Issue566](https://code.google.com/p/wro4j/issues/detail?id=566)	NodeLessCssProcessor support is not computed properly on Windows
  * [Issue567](https://code.google.com/p/wro4j/issues/detail?id=567)	Create TypeScriptProcessor
  * [Issue569](https://code.google.com/p/wro4j/issues/detail?id=569)	CssImportPreProcessor fails with stackOverflowException
  * [Issue571](https://code.google.com/p/wro4j/issues/detail?id=571)	Incremental build should detect changes of resources referred by @import statements
  * [Issue572](https://code.google.com/p/wro4j/issues/detail?id=572)	Create less4j processor
  * [Issue574](https://code.google.com/p/wro4j/issues/detail?id=574)	Use different log level when ignoreMissingResource=true
  * [Issue576](https://code.google.com/p/wro4j/issues/detail?id=576)	CssImport issue with LessCss processor
  * [Issue579](https://code.google.com/p/wro4j/issues/detail?id=579)	NoClassDefFoundError for 1.5.0
  * [Issue580](https://code.google.com/p/wro4j/issues/detail?id=580)	wro4j-runner-1.5.0 doesn't support cssImport anymore
  * [Issue581](https://code.google.com/p/wro4j/issues/detail?id=581)	resourceWatcherUpdatePeriod does not work for nested groups
  * [Issue585](https://code.google.com/p/wro4j/issues/detail?id=585)	Remove YUIJSCompressorProcessor
  * [Issue586](https://code.google.com/p/wro4j/issues/detail?id=586)	Add enable flag to WroFilter
  * [Issue587](https://code.google.com/p/wro4j/issues/detail?id=587)	Update rhinoLessCss processor to 1.3.1
  * [Issue589](https://code.google.com/p/wro4j/issues/detail?id=589)	Meven plugin error after upgrading to 1.5.0
  * [Issue590](https://code.google.com/p/wro4j/issues/detail?id=590)	Alternative way of configuring processors
  * [Issue591](https://code.google.com/p/wro4j/issues/detail?id=591)	ConfigurableWroManagerFactory fails when cacheUpdatePeriod is greater than 0.
  * [Issue592](https://code.google.com/p/wro4j/issues/detail?id=592)	Replace existing DustJS compiler with LinkedIn's updated version
  * [Issue596](https://code.google.com/p/wro4j/issues/detail?id=596)	Upgrade coffeeScriptProcessor to coffee-script-1.4.0
  * [Issue597](https://code.google.com/p/wro4j/issues/detail?id=597)	Create NodeCoffeeScriptProcessor

### Release 1.5.0 ###

**Date: 27 Sep 2012**
  * [Issue257](https://code.google.com/p/wro4j/issues/detail?id=257)	XML reporting for cssLint & jsHint maven plugin
  * [Issue423](https://code.google.com/p/wro4j/issues/detail?id=423)	Use `<group-ref>` defined in an `<import>` wro.xml
  * [Issue435](https://code.google.com/p/wro4j/issues/detail?id=435)	Create PathPatternProcessorDecorator
  * [Issue459](https://code.google.com/p/wro4j/issues/detail?id=459)	wro4j maven plugin should support incremental build
  * [Issue523](https://code.google.com/p/wro4j/issues/detail?id=523)	Upgrade rubySassCss processor to 3.2.1
  * [Issue530](https://code.google.com/p/wro4j/issues/detail?id=530)	css image request causes http 403
  * [Issue537](https://code.google.com/p/wro4j/issues/detail?id=537)	Update evnjs in LessCssProcessor
  * [Issue539](https://code.google.com/p/wro4j/issues/detail?id=539)	Background:url("" ) drops the closing "
  * [Issue541](https://code.google.com/p/wro4j/issues/detail?id=541)	Improve lessCss error reporting
  * [Issue542](https://code.google.com/p/wro4j/issues/detail?id=542)	Create  NodeLessCssProcessor based on lessc binary
  * [Issue543](https://code.google.com/p/wro4j/issues/detail?id=543)	Create Fallback aware LessCss processor
  * [Issue549](https://code.google.com/p/wro4j/issues/detail?id=549)	Update cssLint to latest version
  * [Issue550](https://code.google.com/p/wro4j/issues/detail?id=550)	Update rhino based processors to latest version
  * [Issue551](https://code.google.com/p/wro4j/issues/detail?id=551)	CssMin semicolon bug
  * [Issue552](https://code.google.com/p/wro4j/issues/detail?id=552)	Register RequestHandlers as service provider interface (spi)
  * [Issue553](https://code.google.com/p/wro4j/issues/detail?id=553)	Update google closure dependency version
  * [Issue554](https://code.google.com/p/wro4j/issues/detail?id=554)	reloading the cache fails
  * [Issue555](https://code.google.com/p/wro4j/issues/detail?id=555)	CacheStrategy should be configurable with ConfigurableWroManagerFactory
  * [Issue557](https://code.google.com/p/wro4j/issues/detail?id=557)	ServletContextAttributeHelper returns uninitialized managerFactory
  * [Issue560](https://code.google.com/p/wro4j/issues/detail?id=560)	wro4jrunner missing log4j dependency
  * [Issue561](https://code.google.com/p/wro4j/issues/detail?id=561)	ModelAsJsonRequestHandler should be enabled only in DEVELOPMENT mode
  * [Issue562](https://code.google.com/p/wro4j/issues/detail?id=562)	A reload of model should not trigger cache reload
  * [Issue564](https://code.google.com/p/wro4j/issues/detail?id=564)	Too verbose logging on missing resources

### Release 1.4.9 ###

**Date: 7 Sep 2012**
  * [Issue499](https://code.google.com/p/wro4j/issues/detail?id=499)	Upgrade google closure to latest version
  * [Issue513](https://code.google.com/p/wro4j/issues/detail?id=513)	ERROR ResourceWatcherRunnable:81 - Exception while checking for resource changes logged on tomcat shutdown
  * [Issue514](https://code.google.com/p/wro4j/issues/detail?id=514)	Make ResourceWatcher run efficiently
  * [Issue518](https://code.google.com/p/wro4j/issues/detail?id=518)    Maven plugin generated resource location enhancement
  * [Issue519](https://code.google.com/p/wro4j/issues/detail?id=519)    Add support for abstract group concept
  * [Issue524](https://code.google.com/p/wro4j/issues/detail?id=524)    maven plugin configuration issue
  * [Issue526](https://code.google.com/p/wro4j/issues/detail?id=526)    Resource leak caused by CssImportPreProcessor
  * [Issue528](https://code.google.com/p/wro4j/issues/detail?id=528)    Redundand CacheStrategy decoration
  * [Issue529](https://code.google.com/p/wro4j/issues/detail?id=529)    Missing cache header attributes in css images
  * [Issue534](https://code.google.com/p/wro4j/issues/detail?id=534)    Suppress logging of ClientAbortException in WroFilter.

### Release 1.4.8.1 ###

**Date: 12 Aug 2012**
  * [Issue507](https://code.google.com/p/wro4j/issues/detail?id=507)	Processing cssImport of custom jquery-ui.css StackOverflowError
  * [Issue510](https://code.google.com/p/wro4j/issues/detail?id=510)	Log version when logging configuration
  * [Issue512](https://code.google.com/p/wro4j/issues/detail?id=512)	Too verbose logging on missing resources when resourceWatcher is enabled
  * [Issue515](https://code.google.com/p/wro4j/issues/detail?id=515)    Upgrade sass-gems to latest version

### Release 1.4.8 ###

**Date: 9 Aug 2012**
  * [Issue185](https://code.google.com/p/wro4j/issues/detail?id=185)	Invalidate parts of the cache on resource change
  * [Issue478](https://code.google.com/p/wro4j/issues/detail?id=478)	WroFilter#newWroConfigurationFactory() extendability is difficult
  * [Issue479](https://code.google.com/p/wro4j/issues/detail?id=479)	Too verbose logging on missing resources
  * [Issue482](https://code.google.com/p/wro4j/issues/detail?id=482)	Referencing not existing files in path in the same as wroFilter mapped to causes threads spawning and locking in when disableCache=true
  * [Issue483](https://code.google.com/p/wro4j/issues/detail?id=483)	ServletContextPropertyWroConfigurationFactory#createProperties is not closing stream.
  * [Issue484](https://code.google.com/p/wro4j/issues/detail?id=484)	HandlebarsJs & HoganJs processors always generate null template name
  * [Issue485](https://code.google.com/p/wro4j/issues/detail?id=485)	Content length is not computed correctly
  * [Issue495](https://code.google.com/p/wro4j/issues/detail?id=495)	Default expires headers should be configurable
  * [Issue196](https://code.google.com/p/wro4j/issues/detail?id=196)	ConfigurableWroManagerFactory does not invoke methods responsible for contributing processors
  * [Issue497](https://code.google.com/p/wro4j/issues/detail?id=497)	ModelAsJsonRequestHandler doesn't  display the minimize attribute
  * [Issue498](https://code.google.com/p/wro4j/issues/detail?id=498)	GoogleClosure processor is not threadSafe
  * [Issue500](https://code.google.com/p/wro4j/issues/detail?id=500)	WroContextFilter causing the Context to create new WroConfiguration everytime
  * [Issue502](https://code.google.com/p/wro4j/issues/detail?id=502)	Twitter Bootstrap 2.0  is not compiled well by less-processor
  * [Issue505](https://code.google.com/p/wro4j/issues/detail?id=505)	CssImport processor recursion detection is not thread-safe
  * [Issue506](https://code.google.com/p/wro4j/issues/detail?id=506)	CssImportPreProcessor: Remove imports in CSS comments

### Release 1.4.7 ###
**Date: 30 June 2012**
  * [Issue225](https://code.google.com/p/wro4j/issues/detail?id=225)	Expose model resources in filter as JSON
  * [Issue405](https://code.google.com/p/wro4j/issues/detail?id=405)	cssUrlRewriting does not take context path into account
  * [Issue414](https://code.google.com/p/wro4j/issues/detail?id=414)	Create handlebars processor
  * [Issue430](https://code.google.com/p/wro4j/issues/detail?id=430)	Add support for HoganJs
  * [Issue431](https://code.google.com/p/wro4j/issues/detail?id=431)	WroManager cannot be created using ServletContextAttributeHelper outside the request cycle
  * [Issue432](https://code.google.com/p/wro4j/issues/detail?id=432)	ConfigurableWroFilter cannot load extentions
  * [Issue433](https://code.google.com/p/wro4j/issues/detail?id=433)	Bourbon Sass Mixins library support
  * [Issue434](https://code.google.com/p/wro4j/issues/detail?id=434)	CSS being randomly mixed in with JS (on OSx)
  * [Issue436](https://code.google.com/p/wro4j/issues/detail?id=436)	Change http-server used in demo-projects
  * [Issue438](https://code.google.com/p/wro4j/issues/detail?id=438)	Support for RequestHandlers concept
  * [Issue439](https://code.google.com/p/wro4j/issues/detail?id=439)	reloadCache & reloadModel api calls are broken
  * [Issue440](https://code.google.com/p/wro4j/issues/detail?id=440)	WildcardExpanderModelTransformer problem with / url
  * [Issue442](https://code.google.com/p/wro4j/issues/detail?id=442)	Allow configuration of processor failing behavior
  * [Issue443](https://code.google.com/p/wro4j/issues/detail?id=443)	Add options to the uglifyJs processor or an uglifyJsAdvanced processor
  * [Issue445](https://code.google.com/p/wro4j/issues/detail?id=445)	Resources cannot be located when jRebel is enabled
  * [Issue447](https://code.google.com/p/wro4j/issues/detail?id=447)	ConfigurableProcessorsFactory processor creation is not thread safe
  * [Issue448](https://code.google.com/p/wro4j/issues/detail?id=448)	Create alternative cssDataUri processor implementation
  * [Issue449](https://code.google.com/p/wro4j/issues/detail?id=449)	Create a ProcessorsFactory which uses ServiceRegistry for loading processors
  * [Issue452](https://code.google.com/p/wro4j/issues/detail?id=452)	DefaultWroManagerFactory doesn't implement WroConfigurationChangeListener
  * [Issue453](https://code.google.com/p/wro4j/issues/detail?id=453)	Headers set after content send
  * [Issue454](https://code.google.com/p/wro4j/issues/detail?id=454)	Simplify NamingStrategy & HashStrategy configuration using ConfigurableWroManagerFactory
  * [Issue455](https://code.google.com/p/wro4j/issues/detail?id=455)	WildcardExpanderModelTransformer is not thread safe
  * [Issue456](https://code.google.com/p/wro4j/issues/detail?id=456)	SassCssSupport doesn't handle spaces instead of tabs
  * [Issue460](https://code.google.com/p/wro4j/issues/detail?id=460)	Stack overflow compiling wro4j-bootstrap-sample using the default WroManagerFactory
  * [Issue462](https://code.google.com/p/wro4j/issues/detail?id=462)	CssDataUriPreProcessor hits error (unknown mime type) for linked fonts
  * [Issue463](https://code.google.com/p/wro4j/issues/detail?id=463)	CSS Image URL rewriting not working for CSS hosted external servers.
  * [Issue467](https://code.google.com/p/wro4j/issues/detail?id=467)	Provide access to individual wro-resources
  * [Issue468](https://code.google.com/p/wro4j/issues/detail?id=468)	Allow UriLocators configuration using ServiceRegistry
  * [Issue469](https://code.google.com/p/wro4j/issues/detail?id=469)	Given a resource URI, simplify a way to find out which group it belongs to
  * [Issue472](https://code.google.com/p/wro4j/issues/detail?id=472)	UriLocators configuration are not picked up from wro.properties config file
  * [Issue473](https://code.google.com/p/wro4j/issues/detail?id=473)	ServletContextUriLocator should use fallback strategy when dispatcher fails
  * [Issue475](https://code.google.com/p/wro4j/issues/detail?id=475)	Update rhino based processors to latest version
  * [Issue476](https://code.google.com/p/wro4j/issues/detail?id=476)	CSSMin.parseProperties doesn't check for empty property


### Release 1.4.6 ###
**Date: 10 May 2012**
  * [Issue304](https://code.google.com/p/wro4j/issues/detail?id=304)     Use of InheritableThreadLocal in ro.isdc.wro.config.Context questionable
  * [Issue358](https://code.google.com/p/wro4j/issues/detail?id=358)     Migrate to rhino 1.7R3 version
  * [Issue372](https://code.google.com/p/wro4j/issues/detail?id=372)     Create SassCssProcessor using jRuby underlying implementation
  * [Issue400](https://code.google.com/p/wro4j/issues/detail?id=400)     Lazy loading instantiation of processors and locators
  * [Issue415](https://code.google.com/p/wro4j/issues/detail?id=415)     Maven plugin processor extension configuration
  * [Issue416](https://code.google.com/p/wro4j/issues/detail?id=416)     ServletContextUriLocator not working with Spring mvc resources
  * [Issue417](https://code.google.com/p/wro4j/issues/detail?id=417)     Allow configuration using ServletContextListener
  * [Issue418](https://code.google.com/p/wro4j/issues/detail?id=418)     Suppress logging level for maven plugin
  * [Issue419](https://code.google.com/p/wro4j/issues/detail?id=419)     Encoding value is not initialized properly
  * [Issue420](https://code.google.com/p/wro4j/issues/detail?id=420)     Content length is not set
  * [Issue424](https://code.google.com/p/wro4j/issues/detail?id=424)     Memory & Performance improvements
  * [Issue425](https://code.google.com/p/wro4j/issues/detail?id=425)     CssImportPreProcessor and CssUrlRewritingProcessor produce invalid image URL when using nested imports
  * [Issue427](https://code.google.com/p/wro4j/issues/detail?id=427)     Allow ConsoleStripperProcessor be added through wro.properties
  * [Issue428](https://code.google.com/p/wro4j/issues/detail?id=428)     Update rhino based processors to latest version
  * [Issue429](https://code.google.com/p/wro4j/issues/detail?id=429)     Update google closure dependency version

### Release 1.4.5 ###
**Date: 7 April 2012**
  * [Issue12](https://code.google.com/p/wro4j/issues/detail?id=12)     Create console.log & console.debug stripper JS processor
  * [Issue382](https://code.google.com/p/wro4j/issues/detail?id=382)    Rhino based processors fails when slf4j-api is not included
  * [Issue383](https://code.google.com/p/wro4j/issues/detail?id=383)     Backslashes in CSS duplicated
  * [Issue384](https://code.google.com/p/wro4j/issues/detail?id=384)     Test failing on osx platform
  * [Issue385](https://code.google.com/p/wro4j/issues/detail?id=385)     ServletContextUriLocator does not work on WebSphere 6.1
  * [Issue389](https://code.google.com/p/wro4j/issues/detail?id=389)     Configure connectionTimeout in milliseconds instead of seconds
  * [Issue390](https://code.google.com/p/wro4j/issues/detail?id=390)     Create DustJs processor
  * [Issue391](https://code.google.com/p/wro4j/issues/detail?id=391)     Create CoffeeScript & Less Filters
  * [Issue393](https://code.google.com/p/wro4j/issues/detail?id=393)     Error while using the 'import' tag in the wro.xml file
  * [Issue394](https://code.google.com/p/wro4j/issues/detail?id=394)     CopyrightKeeperProcessorDecorator ignores Resource#isMinimize()
  * [Issue395](https://code.google.com/p/wro4j/issues/detail?id=395)     Add postProcessors support to wro4j-runner
  * [Issue397](https://code.google.com/p/wro4j/issues/detail?id=397)     Update less.js processor
  * [Issue407](https://code.google.com/p/wro4j/issues/detail?id=407)     Update rhino based processors to latest version
  * [Issue408](https://code.google.com/p/wro4j/issues/detail?id=408)     Update google closure dependency version
  * [Issue409](https://code.google.com/p/wro4j/issues/detail?id=409)     Proceed with filter chain when requesting a group with no resources
  * [Issue410](https://code.google.com/p/wro4j/issues/detail?id=410)     Reload Cache scheduler should process only requested groups
  * [Issue411](https://code.google.com/p/wro4j/issues/detail?id=411)     Locator cannot find resources during scheduler update
  * [Issue412](https://code.google.com/p/wro4j/issues/detail?id=412)     ConnectionTimeout cannot be configured using wro.properties
  * [Issue413](https://code.google.com/p/wro4j/issues/detail?id=413)     Group is not extracted correctly when resource url contains jsessionID

### Release 1.4.4 ###
**Date: 20 February 2012**
  * [Issue88](https://code.google.com/p/wro4j/issues/detail?id=88)	Invalidate the cache when model is updated
  * [Issue359](https://code.google.com/p/wro4j/issues/detail?id=359)	PerformanceLoggerCallback doesn't work properly in some situations
  * [Issue361](https://code.google.com/p/wro4j/issues/detail?id=361)	LessCssProcessor fails silently
  * [Issue362](https://code.google.com/p/wro4j/issues/detail?id=362)	Deep recursive wildcard pattern  in classpath groups
  * [Issue364](https://code.google.com/p/wro4j/issues/detail?id=364)	ObjectPoolHelper should use WHEN\_EXHAUSTED\_GROW policy
  * [Issue365](https://code.google.com/p/wro4j/issues/detail?id=365)	Problem in LessCss extensibility
  * [Issue366](https://code.google.com/p/wro4j/issues/detail?id=366)	csslint not working via maven since 1.4.2
  * [Issue369](https://code.google.com/p/wro4j/issues/detail?id=369)	Upgrade rhino based processors to latest versions
  * [Issue370](https://code.google.com/p/wro4j/issues/detail?id=370)	reloadModel and reloadCache should be ignored if wro is not initialized yet
  * [Issue373](https://code.google.com/p/wro4j/issues/detail?id=373)	NullPointerException while creating a ProcessorsFactory which has decorated processors
  * [Issue378](https://code.google.com/p/wro4j/issues/detail?id=378)	CSSMin bug on Properties containing colon ":"
  * [Issue381](https://code.google.com/p/wro4j/issues/detail?id=381)	Exception while rewriting css url's containing $ character.


### Release 1.4.3 ###
**Date: 8 January 2012**
  * [Issue253](https://code.google.com/p/wro4j/issues/detail?id=253)	New UTF-8 encoding issues
  * [Issue316](https://code.google.com/p/wro4j/issues/detail?id=316)	Callbacks support
  * [Issue339](https://code.google.com/p/wro4j/issues/detail?id=339)	Add a timestamp naming strategy.
  * [Issue341](https://code.google.com/p/wro4j/issues/detail?id=341)	No content type header sent for large resources
  * [Issue343](https://code.google.com/p/wro4j/issues/detail?id=343)	WroManagerFactory should be injectable to the filter
  * [Issue345](https://code.google.com/p/wro4j/issues/detail?id=345)	ETag must be set before the content is written.
  * [Issue346](https://code.google.com/p/wro4j/issues/detail?id=346)	Reserved names support for UglifyJsProcessor
  * [Issue347](https://code.google.com/p/wro4j/issues/detail?id=347)	WroConfiguration and Context should be Injectable
  * [Issue350](https://code.google.com/p/wro4j/issues/detail?id=350)	Upgrade uglifyJs processor to latest version
  * [Issue353](https://code.google.com/p/wro4j/issues/detail?id=353)	wro4j-core doesn't work with jdk 1.5
  * [Issue354](https://code.google.com/p/wro4j/issues/detail?id=354)	Parallel preProcessing flag for wro4j-runner
  * [Issue355](https://code.google.com/p/wro4j/issues/detail?id=355)	Update less.js processor to latest version
  * [Issue356](https://code.google.com/p/wro4j/issues/detail?id=356)	Update uglifyJs processor to latest version
  * [Issue357](https://code.google.com/p/wro4j/issues/detail?id=357)	Update linters dependencies to latest version

### Release 1.4.2 ###
**Date: 7 December 2011**
  * [Issue100](https://code.google.com/p/wro4j/issues/detail?id=100)	JS Lint integration
  * [Issue305](https://code.google.com/p/wro4j/issues/detail?id=305)	Multiple concurrent calls into WroFilter when Context.get().getConfig().getCacheUpdatePeriod() > 0 cause multiple watcher thread factories
  * [Issue306](https://code.google.com/p/wro4j/issues/detail?id=306)	wro4j filter fails on concurrent requests
  * [Issue308](https://code.google.com/p/wro4j/issues/detail?id=308)	Update google closure dependency version
  * [Issue309](https://code.google.com/p/wro4j/issues/detail?id=309)	Incorrect WARN statements report
  * [Issue310](https://code.google.com/p/wro4j/issues/detail?id=310)	Rhino is not exited, leaks memory
  * [Issue312](https://code.google.com/p/wro4j/issues/detail?id=312)	Wildcard resources are not order properly
  * [Issue321](https://code.google.com/p/wro4j/issues/detail?id=321)	FileNotFoundException and NullPointerException are raised when multiple applications on tomcat
  * [Issue322](https://code.google.com/p/wro4j/issues/detail?id=322)	CSS url rewriting doesn't handle properly whitespace
  * [Issue323](https://code.google.com/p/wro4j/issues/detail?id=323)	Allow gzipped content to be cached
  * [Issue324](https://code.google.com/p/wro4j/issues/detail?id=324)	Changing cacheUpdatePeriod with JMX is broken
  * [Issue325](https://code.google.com/p/wro4j/issues/detail?id=325)	Expose additional properties through JMX
  * [Issue326](https://code.google.com/p/wro4j/issues/detail?id=326)	Google Closure externs configuration support
  * [Issue327](https://code.google.com/p/wro4j/issues/detail?id=327)	Processors relying on rhino are not thread-safe
  * [Issue328](https://code.google.com/p/wro4j/issues/detail?id=328)	Multiline comment stripper processor issue
  * [Issue329](https://code.google.com/p/wro4j/issues/detail?id=329)	Update cssLint processor to latest version
  * [Issue331](https://code.google.com/p/wro4j/issues/detail?id=331)	Parallel resource preProcessing  support
  * [Issue332](https://code.google.com/p/wro4j/issues/detail?id=332)	Update uglifyJs processor to latest version
  * [Issue334](https://code.google.com/p/wro4j/issues/detail?id=334)	Runner: exceptions are caught & logged, but, should exit with System.exit(1) so Ant and other CLI interfaces know there was an error
  * [Issue335](https://code.google.com/p/wro4j/issues/detail?id=335)	JsHint predef options bug
  * [Issue336](https://code.google.com/p/wro4j/issues/detail?id=336)	Maven plugin configuration issue

### Release 1.4.1 ###
**Date: 18 September 2011**
  * [Issue286](https://code.google.com/p/wro4j/issues/detail?id=286)	Upgrade dojoShrinksafe processor
  * [Issue287](https://code.google.com/p/wro4j/issues/detail?id=287)	ConfigurableWroFilter and no processors defined
  * [Issue288](https://code.google.com/p/wro4j/issues/detail?id=288)	jshint maven plugin goal and customManagerFactory
  * [Issue290](https://code.google.com/p/wro4j/issues/detail?id=290)	Update jsHint processor to latest version
  * [Issue291](https://code.google.com/p/wro4j/issues/detail?id=291)	Update cssLint processor to latest version
  * [Issue293](https://code.google.com/p/wro4j/issues/detail?id=293)	Exceptions are not always logged
  * [Issue295](https://code.google.com/p/wro4j/issues/detail?id=295)	Create ExtensionAwareProcessorDecorator
  * [Issue296](https://code.google.com/p/wro4j/issues/detail?id=296)	Extend JsHint option configuration
  * [Issue297](https://code.google.com/p/wro4j/issues/detail?id=297)	Wrong report of the line number in jsHint
  * [Issue298](https://code.google.com/p/wro4j/issues/detail?id=298)	Make alias processor configuration extension aware
  * [Issue299](https://code.google.com/p/wro4j/issues/detail?id=299)	Problem with image background in maven plugin
  * [Issue300](https://code.google.com/p/wro4j/issues/detail?id=300)	Update uglifyJs processor to latest version
  * [Issue301](https://code.google.com/p/wro4j/issues/detail?id=301)	Upgrade CoffeeScript to latest version

### Release 1.4.0 ###
[Release Higlights](Release_Highlights_1_4_0.md)
**Date: 26 August 2011**
  * [Issue23](https://code.google.com/p/wro4j/issues/detail?id=23)	Create [Grails plugin](GrailsPlugin.md)
  * [Issue196](https://code.google.com/p/wro4j/issues/detail?id=196)	Build wro model with [groovy script](GroovyWroModel.md)
  * [Issue221](https://code.google.com/p/wro4j/issues/detail?id=221)	Normalize css url path generated by CssUrlRewritingProcessor
  * [Issue245](https://code.google.com/p/wro4j/issues/detail?id=245)	Wildcard classpath resources and maven plugin
  * [Issue246](https://code.google.com/p/wro4j/issues/detail?id=246)	JsonHPack packer should accept plain JSON object
  * [Issue247](https://code.google.com/p/wro4j/issues/detail?id=247)	Wildcards expander support
  * [Issue252](https://code.google.com/p/wro4j/issues/detail?id=252)	Create [SmartWroModelFactory](SmartWroModelFactory.md)
  * [Issue254](https://code.google.com/p/wro4j/issues/detail?id=254)	Simplify maven plugin processors configuration
  * [Issue255](https://code.google.com/p/wro4j/issues/detail?id=255)	Add ModelTransformer support
  * [Issue256](https://code.google.com/p/wro4j/issues/detail?id=256)	Update uglifyJs processor to version 1.0.6
  * [Issue259](https://code.google.com/p/wro4j/issues/detail?id=259)	Problem with background url in css after aggregation
  * [Issue260](https://code.google.com/p/wro4j/issues/detail?id=260)	Update less.js to latest version
  * [Issue261](https://code.google.com/p/wro4j/issues/detail?id=261)	Remove BOM characters by default
  * [Issue262](https://code.google.com/p/wro4j/issues/detail?id=262)	Google closure compiler extensibility
  * [Issue263](https://code.google.com/p/wro4j/issues/detail?id=263)	Update cssLint processor to latest version
  * [Issue264](https://code.google.com/p/wro4j/issues/detail?id=264)	Configuration of pre processors for wro4j-runner
  * [Issue269](https://code.google.com/p/wro4j/issues/detail?id=269)	Processors configuration in config properties file
  * [Issue270](https://code.google.com/p/wro4j/issues/detail?id=270)	Improve performance of processors depending on Rhino
  * [Issue272](https://code.google.com/p/wro4j/issues/detail?id=272)	OOM in !LessCSS engine
  * [Issue277](https://code.google.com/p/wro4j/issues/detail?id=277)	google closure version upgrade
  * [Issue281](https://code.google.com/p/wro4j/issues/detail?id=281)	Reload model bug
  * [Issue283](https://code.google.com/p/wro4j/issues/detail?id=283)	Create a properties file to hold the mapping between original & renamed resource for maven plugin
  * [Issue284](https://code.google.com/p/wro4j/issues/detail?id=284)	Simplify processors configuration with ConfigurableWroFilter


### New release (1.3.8) ###

**Date: 22 June 2011**
  * [Issue226](https://code.google.com/p/wro4j/issues/detail?id=226) CopyrightKeeperProcessorDecorator doesn't inherit @Minimize annotation
  * [Issue228](https://code.google.com/p/wro4j/issues/detail?id=228) ServletContextLocator doesn't handle jsp files
  * [Issue229](https://code.google.com/p/wro4j/issues/detail?id=229) Create JSON pack/unpack processors
  * [Issue230](https://code.google.com/p/wro4j/issues/detail?id=230) Create PlaceholderProcessor
  * [Issue231](https://code.google.com/p/wro4j/issues/detail?id=231) minimizing new CSS 3 "@media" features.
  * [Issue232](https://code.google.com/p/wro4j/issues/detail?id=232) Create CssLint processor
  * [Issue233](https://code.google.com/p/wro4j/issues/detail?id=233) WroConfiguration always print a warning message "You cannot disable cache in DEPLOYMENT mode"
  * [Issue234](https://code.google.com/p/wro4j/issues/detail?id=234) Create GoogleAdvancedStandaloneManagerFactory
  * [Issue235](https://code.google.com/p/wro4j/issues/detail?id=235) Allow configure WroFilter from properties file
  * [Issue236](https://code.google.com/p/wro4j/issues/detail?id=236) Configure managerFactoryClassName from property configuration file
  * [Issue237](https://code.google.com/p/wro4j/issues/detail?id=237) Create CssLint maven plugin
  * [Issue238](https://code.google.com/p/wro4j/issues/detail?id=238) Pre processors error reporting enhancement
  * [Issue239](https://code.google.com/p/wro4j/issues/detail?id=239) Change Wro4j maven plugin execution phase to compile
  * [Issue240](https://code.google.com/p/wro4j/issues/detail?id=240) SemicolonAppenderPreProcessor and empty scripts
  * [Issue241](https://code.google.com/p/wro4j/issues/detail?id=241) CssImportPreProcessor and ignoreMissingResources
  * [Issue242](https://code.google.com/p/wro4j/issues/detail?id=242) Add more processors to wro4j runner

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.8

### New release (1.3.7) ###

**Date: 31 May 2011**
  * [Issue190](https://code.google.com/p/wro4j/issues/detail?id=190) Add coffeScript support to wro4j-runner
  * [Issue208](https://code.google.com/p/wro4j/issues/detail?id=208) Ability to specify file.encoding in ConfigurableWroFilter
  * [Issue209](https://code.google.com/p/wro4j/issues/detail?id=209) wro4j maven plugin detailed exception message
  * [Issue212](https://code.google.com/p/wro4j/issues/detail?id=212) use a factory for WroConfiguration creation
  * [Issue214](https://code.google.com/p/wro4j/issues/detail?id=214) Create Copyright Information Processor
  * [Issue215](https://code.google.com/p/wro4j/issues/detail?id=215) Update google closure dependency version
  * [Issue216](https://code.google.com/p/wro4j/issues/detail?id=216) ConfigurableWroFilter configuration with Properties file
  * [Issue222](https://code.google.com/p/wro4j/issues/detail?id=222) Upgrade CoffeeScript to version 1.1.1 & processor extensibility support
  * [Issue223](https://code.google.com/p/wro4j/issues/detail?id=223) Update uglifyJs processor to version 1.0.2
  * [Issue224](https://code.google.com/p/wro4j/issues/detail?id=224) upgrade LessCss processor to latest version

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.7

### New release (1.3.6) ###

**Date: 12 April 2011**
  * [Issue33](https://code.google.com/p/wro4j/issues/detail?id=33) Integrate LRU cache strategy
  * [Issue92](https://code.google.com/p/wro4j/issues/detail?id=92) Build wro model with JSON
  * [Issue138](https://code.google.com/p/wro4j/issues/detail?id=138) Externalize processor provider capability
  * [Issue181](https://code.google.com/p/wro4j/issues/detail?id=181) Allow multiple wro.xml
  * [Issue182](https://code.google.com/p/wro4j/issues/detail?id=182) WroModelFactory refactoring
  * [Issue183](https://code.google.com/p/wro4j/issues/detail?id=183) ClassPathUriLocator wildcard support doesn't work with resources inside JARs
  * [Issue186](https://code.google.com/p/wro4j/issues/detail?id=186) Less Css processor errors are not intuitive
  * [Issue187](https://code.google.com/p/wro4j/issues/detail?id=187) Create [Coffee script](http://jashkenas.github.com/coffee-script/) processor
  * [Issue192](https://code.google.com/p/wro4j/issues/detail?id=192) wro4j with jawrCssMinifier cannot handle css3 attribute selectors
  * [Issue197](https://code.google.com/p/wro4j/issues/detail?id=197) Update google closure dependency version
  * [Issue200](https://code.google.com/p/wro4j/issues/detail?id=200) Update uglifyJs compressor to latest version (1.0.1)
  * [Issue203](https://code.google.com/p/wro4j/issues/detail?id=203) CssDataUri doesn't work with absolute url images
  * [Issue204](https://code.google.com/p/wro4j/issues/detail?id=204) Add CssDataUriPreProcessor to ConfigurableWroManagerFactory

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.6

### New release (1.3.5) ###

**Date: 10 March 2011**
  * [Issue141](https://code.google.com/p/wro4j/issues/detail?id=141) CssDataUriPreProcessor should detect duplicate uri's
  * [Issue171](https://code.google.com/p/wro4j/issues/detail?id=171) Browser loads optimized script/css and then waits for 20 seconds...
  * [Issue172](https://code.google.com/p/wro4j/issues/detail?id=172) Create a jsHint processor
  * [Issue174](https://code.google.com/p/wro4j/issues/detail?id=174) Gzipping resources doesn't not work on server enforcing response.setContentLength
  * [Issue176](https://code.google.com/p/wro4j/issues/detail?id=176) Create JsHint maven plugin (@see http://web-resource-optimization.blogspot.com/2011/03/build-time-javascript-code-analysis.html)
  * [Issue180](https://code.google.com/p/wro4j/issues/detail?id=180) Upgrade uglifyJs

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.5

### New release (1.3.4) ###
**Date: 13 February 2011**
  * [Issue46](https://code.google.com/p/wro4j/issues/detail?id=46) Create command line tool (http://web-resource-optimization.blogspot.com/2011/02/simple-client-side-build-system-with.html) called wro4j-runner.
  * [Issue162](https://code.google.com/p/wro4j/issues/detail?id=162) Input stream has been finalized or forced closed without being explicitly closed
  * [Issue163](https://code.google.com/p/wro4j/issues/detail?id=163) lessCss parser shows INFO logging
  * [Issue164](https://code.google.com/p/wro4j/issues/detail?id=164) Wildcard resources and classpath locator are not working properly
  * [Issue166](https://code.google.com/p/wro4j/issues/detail?id=166) Add disableCacheInDevelopment flag to settings
  * [Issue168](https://code.google.com/p/wro4j/issues/detail?id=168) Change Caching Headers in DEVELOPMENT Mode
  * [Issue169](https://code.google.com/p/wro4j/issues/detail?id=169) Upgrade less.js version to 1.0.41

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.4

### New release (1.3.3) ###

**Date: 11 January 2011**
  * [Issue155](https://code.google.com/p/wro4j/issues/detail?id=155) Use DefaultCodingConvention for GoogleClosure compiler processor.
  * [Issue156](https://code.google.com/p/wro4j/issues/detail?id=156) Improve Gzip compression support
  * [Issue157](https://code.google.com/p/wro4j/issues/detail?id=157) Enclose ETag value in quotes
  * [Issue158](https://code.google.com/p/wro4j/issues/detail?id=158) Prevent specific files from being compressed/minified
  * [Issue159](https://code.google.com/p/wro4j/issues/detail?id=159) SemicolonAppenderPreProcessor should append semicolon only if needed
  * [Issue160](https://code.google.com/p/wro4j/issues/detail?id=160) Update uglifyJs processor to latest version
  * [Issue161](https://code.google.com/p/wro4j/issues/detail?id=161) Supress spurious duplicate resource detection on reload

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.3

### New release (1.3.2) ###

**Date: 11 December 2010**
  * [Issue146](https://code.google.com/p/wro4j/issues/detail?id=146) Resource status code is always 200
  * [Issue147](https://code.google.com/p/wro4j/issues/detail?id=147) Use official google closure dependency
  * [Issue148](https://code.google.com/p/wro4j/issues/detail?id=148) Update less.js processor
  * [Issue149](https://code.google.com/p/wro4j/issues/detail?id=149) CssImportPreProcessor uses a too restrictive PATTERN for finding imports
  * [Issue150](https://code.google.com/p/wro4j/issues/detail?id=150) Maven artifact for wro4j-core and wro4j-extensions pulls unnecessary dependencies
  * [Issue151](https://code.google.com/p/wro4j/issues/detail?id=151) ServletContext missing resources on Tomcat
  * [Issue152](https://code.google.com/p/wro4j/issues/detail?id=152) Wildcard Resources are not ordered alphabetically
  * [Issue153](https://code.google.com/p/wro4j/issues/detail?id=153) Update uglifyJs processor to latest version

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.2

### New release (1.3.1) ###

**Date: 7 November 2010**
  * [Issue122](https://code.google.com/p/wro4j/issues/detail?id=122) Create UglifyJs processor
  * [Issue142](https://code.google.com/p/wro4j/issues/detail?id=142) YuiJsMin compressor is broken in wro4j-1.3.0
  * [Issue143](https://code.google.com/p/wro4j/issues/detail?id=143) Integrate DojoShrinksafe compressor
  * [Issue144](https://code.google.com/p/wro4j/issues/detail?id=144) Prevent caching of wro api requests
  * [Issue145](https://code.google.com/p/wro4j/issues/detail?id=145) Create beautifyJsProcessor based on UglifyJs beautifier

Details: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.1

### New release (1.3.0) ###

**Date: 1 November 2010**
  * [Issue13](https://code.google.com/p/wro4j/issues/detail?id=13) Resource fingerprinting
  * [Issue68](https://code.google.com/p/wro4j/issues/detail?id=68) Find an alternative way to set configuration mode
  * [Issue80](https://code.google.com/p/wro4j/issues/detail?id=80) Maven plugin classpath resources support
  * [Issue86](https://code.google.com/p/wro4j/issues/detail?id=86) Integrate SASS css meta framework
  * [Issue96](https://code.google.com/p/wro4j/issues/detail?id=96) Demo web module
  * [Issue97](https://code.google.com/p/wro4j/issues/detail?id=97) Trigger cache & model update through http request
  * [Issue99](https://code.google.com/p/wro4j/issues/detail?id=99) Enable customized versioning of output resource for wro4j maven plugin
  * [Issue101](https://code.google.com/p/wro4j/issues/detail?id=101) Dynamic resource locator must support redirects
  * [Issue102](https://code.google.com/p/wro4j/issues/detail?id=102) Replace current LessCss processor implementation with a newer one
  * [Issue103](https://code.google.com/p/wro4j/issues/detail?id=103) Create a processor for Packer JS compressor
  * [Issue104](https://code.google.com/p/wro4j/issues/detail?id=104) Create preconfigured WroManagerFactories for maven using YUI & Google Closure
  * [Issue105](https://code.google.com/p/wro4j/issues/detail?id=105) WroConfiguration should not be the same for many applications
  * [Issue106](https://code.google.com/p/wro4j/issues/detail?id=106) Make targetGroups parameter optional for wro4j maven plugin
  * [Issue107](https://code.google.com/p/wro4j/issues/detail?id=107) Use daemon threads for schedulers
  * [Issue108](https://code.google.com/p/wro4j/issues/detail?id=108) Use scheduleWithFixedDelay when scheduling model update
  * [Issue109](https://code.google.com/p/wro4j/issues/detail?id=109) Create Conform Colors Css processor
  * [Issue110](https://code.google.com/p/wro4j/issues/detail?id=110) Create VariablizeColors css processor
  * [Issue112](https://code.google.com/p/wro4j/issues/detail?id=112) Create a css processor based on Andy Roberts CssCompressor
  * [Issue113](https://code.google.com/p/wro4j/issues/detail?id=113) Encoding issue
  * [Issue114](https://code.google.com/p/wro4j/issues/detail?id=114) Maven plugin doesn't handle correctly wildcards
  * [Issue115](https://code.google.com/p/wro4j/issues/detail?id=115) Detect duplicated resources
  * [Issue116](https://code.google.com/p/wro4j/issues/detail?id=116) Maven plugin shouldn't create empty files
  * [Issue117](https://code.google.com/p/wro4j/issues/detail?id=117) Maven plugin should allow configuration of naming strategy
  * [Issue121](https://code.google.com/p/wro4j/issues/detail?id=121) wro4j does not work behind a RequestDispatcher.include
  * [Issue123](https://code.google.com/p/wro4j/issues/detail?id=123) Reuse YUICompressor code & remove dependency
  * [Issue124](https://code.google.com/p/wro4j/issues/detail?id=124) newCacheStrategy method should be protected in BaseWroManagerFactory
  * [Issue125](https://code.google.com/p/wro4j/issues/detail?id=125) Processors execution order
  * [Issue128](https://code.google.com/p/wro4j/issues/detail?id=128) Upgrade LessCss to 1.0.36 version
  * [Issue129](https://code.google.com/p/wro4j/issues/detail?id=129) Upgrade google closure dependency to latest revision
  * [Issue131](https://code.google.com/p/wro4j/issues/detail?id=131) Classpath UriLocator doesn't accept empty spaces
  * [Issue134](https://code.google.com/p/wro4j/issues/detail?id=134) Resource comparison test - refactoring
  * [Issue135](https://code.google.com/p/wro4j/issues/detail?id=135) Get rid of wro4j-test-utils artifact

See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.3.0


### New release (1.2.8) ###
**Date: 27 June 2010**
  * [Issue91](https://code.google.com/p/wro4j/issues/detail?id=91) CSS url rewriting creates incorrect urls for CSS rules that include quoted urls
  * [Issue93](https://code.google.com/p/wro4j/issues/detail?id=93) Use scheduleWithFixedDelay when scheduling cache update

See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.8

### New release (1.2.7) ###
**Date: 10 May 2010**
  * [Issue21](https://code.google.com/p/wro4j/issues/detail?id=21) Wildcard syntax support in group definition
  * [Issue38](https://code.google.com/p/wro4j/issues/detail?id=38) Add support for base 64-encoded image in CSS
  * [Issue48](https://code.google.com/p/wro4j/issues/detail?id=48) interpoloation of the wro.xml
  * [Issue81](https://code.google.com/p/wro4j/issues/detail?id=81) Create NoProcessors ManagerFactory
  * [Issue83](https://code.google.com/p/wro4j/issues/detail?id=83) GroupExtractor should use HttpServletRequest to get the group name
  * [Issue84](https://code.google.com/p/wro4j/issues/detail?id=84) Slf4j dependency is not added by maven
  * [Issue85](https://code.google.com/p/wro4j/issues/detail?id=85) Create a google closure distribution for wro4j integration
  * [Issue87](https://code.google.com/p/wro4j/issues/detail?id=87) UriLocators implementations shouldn't be final
  * [Issue89](https://code.google.com/p/wro4j/issues/detail?id=89) Add ServletContext parameter to newModelFactory method
  * [Issue90](https://code.google.com/p/wro4j/issues/detail?id=90) BomStripper should support also CSS resources

See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.7

### New release (1.2.6) ###
**Date: 22 apr 2010**
  * [Issue15](https://code.google.com/p/wro4j/issues/detail?id=15) Integrate CSS meta frameworks (See [LessCssSupport](LessCssSupport.md) wiki page)
  * [Issue77](https://code.google.com/p/wro4j/issues/detail?id=77) Compatibility with servlet-api-2.3
  * [Issue78](https://code.google.com/p/wro4j/issues/detail?id=78) Add granular destinationFolder control for maven plugin
  * [Issue79](https://code.google.com/p/wro4j/issues/detail?id=79) HashCode implementation of Group and Resource classes
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.6

### New release (1.2.5) ###
**Date: 19 apr 2010**
  * [Issue74](https://code.google.com/p/wro4j/issues/detail?id=74) wro4j-maven-plugin-1.2.3 is broken
  * [Issue76](https://code.google.com/p/wro4j/issues/detail?id=76) Make wro4j compatible with java 1.5
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.5

### New release (1.2.4) ###
**Date: 18 apr 2010**
  * [Issue25](https://code.google.com/p/wro4j/issues/detail?id=25) Integrate Google Closure compiler
  * [Issue69](https://code.google.com/p/wro4j/issues/detail?id=69) Configurable MBean object name
  * [Issue70](https://code.google.com/p/wro4j/issues/detail?id=70) ConfigurableWroManagerFactory JMX problem
  * [Issue71](https://code.google.com/p/wro4j/issues/detail?id=71) CssVariablesProcessor must be before CssUrlRewritingProcessor
  * [Issue72](https://code.google.com/p/wro4j/issues/detail?id=72) Add ignoreMissingResources to wro4j Maven Plugin
  * [Issue73](https://code.google.com/p/wro4j/issues/detail?id=73) Processors execution order in ConfigurableWroManagerFactory
  * [Issue75](https://code.google.com/p/wro4j/issues/detail?id=75) Create ExtensionsConfigurableWroManagerFactory
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.4

### New release (1.2.3) ###
**Date: 15 apr 2010**
  * [Issue27](https://code.google.com/p/wro4j/issues/detail?id=27) Configure expires headers using init-param
  * [Issue61](https://code.google.com/p/wro4j/issues/detail?id=61) ConfigurableWroManagerFactory related exception
  * [Issue62](https://code.google.com/p/wro4j/issues/detail?id=62) Create a fallback aware ModelFactory
  * [Issue63](https://code.google.com/p/wro4j/issues/detail?id=63) Reloading cache is not working properly
  * [Issue64](https://code.google.com/p/wro4j/issues/detail?id=64) CssUrlRewriting for css from WEB-INF folder
  * [Issue65](https://code.google.com/p/wro4j/issues/detail?id=65) Exception when dealing with dynamic resources
  * [Issue66](https://code.google.com/p/wro4j/issues/detail?id=66) Update caching headers when the resources cache is updated
  * [Issue67](https://code.google.com/p/wro4j/issues/detail?id=67) Allow custom handling of WroRuntimeException
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.3

### New release (1.2.2) ###
**Date: 9 apr 2010**
  * [Issue58](https://code.google.com/p/wro4j/issues/detail?id=58) Configurable GroupsProcessors for Wro4j maven plugin
  * [Issue59](https://code.google.com/p/wro4j/issues/detail?id=59) JMX Configurations
  * [Issue60](https://code.google.com/p/wro4j/issues/detail?id=60) Failure when attempting to set Content-Encoding
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.2


### New release (1.2.1) ###
  * [Issue51](https://code.google.com/p/wro4j/issues/detail?id=51) wro4j maven plugin enhancements
  * [Issue52](https://code.google.com/p/wro4j/issues/detail?id=52) Invalid resource handling
  * [Issue54](https://code.google.com/p/wro4j/issues/detail?id=54) Make cacheUpdatePeriod and modelUpdatePeriod configurable
  * [Issue55](https://code.google.com/p/wro4j/issues/detail?id=55) Create semicolon Appender Javascript pre processor
  * [Issue56](https://code.google.com/p/wro4j/issues/detail?id=56) Switch minimization on/off in DEVELOPMENT mode
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.1

### New release (1.2.0) ###
  * [Issue2](https://code.google.com/p/wro4j/issues/detail?id=2) Enable/disable Gzip using request parameter
  * [Issue3](https://code.google.com/p/wro4j/issues/detail?id=3) Remove logic from WroFilter
  * [Issue5](https://code.google.com/p/wro4j/issues/detail?id=5) Document "How to extend & integrate"
  * [Issue6](https://code.google.com/p/wro4j/issues/detail?id=6) Create maven 2 plugin
  * [Issue16](https://code.google.com/p/wro4j/issues/detail?id=16) Upload wro4j to maven central repository
  * [Issue17](https://code.google.com/p/wro4j/issues/detail?id=17) Support @import in css resources
  * [Issue18](https://code.google.com/p/wro4j/issues/detail?id=18) Variables cannot be externalized
  * [Issue26](https://code.google.com/p/wro4j/issues/detail?id=26) Create WroManagerFactory capable of being configured using init-params
  * [Issue28](https://code.google.com/p/wro4j/issues/detail?id=28) JMX support to change the behaviour at runtime
  * [Issue30](https://code.google.com/p/wro4j/issues/detail?id=30) Create security strategy for resource streaming
  * [Issue35](https://code.google.com/p/wro4j/issues/detail?id=35) Create MultipleGroup uriRequestProcessor
  * [Issue36](https://code.google.com/p/wro4j/issues/detail?id=36) Runtime Configuration Option
  * [Issue40](https://code.google.com/p/wro4j/issues/detail?id=40) XmlModelFactory improvements
  * [Issue43](https://code.google.com/p/wro4j/issues/detail?id=43) Move code base to GitHub
  * [Issue47](https://code.google.com/p/wro4j/issues/detail?id=47) BOM Characters at beginning of JS files breaks JS concatenation
  * [Issue50](https://code.google.com/p/wro4j/issues/detail?id=50) Core dependency to slf4j-log4j12
See details here: http://code.google.com/p/wro4j/issues/list?can=1&q=milestone:1.2.0