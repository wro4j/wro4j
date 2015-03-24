# Web Resource Optimizer for Java  #

[Fork me on Github.](https://github.com/alexo/wro4j)

> Free and Open Source Java project which brings together almost all the modern web tools: JsHint, CssLint, JsMin, Google Closure compressor, YUI Compressor, UglifyJs, Dojo Shrinksafe, Css Variables Support, JSON Compression, Less, Sass, CoffeeScript and [much more](Features.md). In the same time, the aim is to keep it as simple as possible and as extensible as possible in order to be easily adapted to application specific needs.

> [Easily](http://alexo.github.com/wro4j) improve your web application loading time. Keep project web resources (js & css) [well organized](WroFileFormat.md), merge & minify them at [run-time](Installation.md) (using a simple filter) or [build-time](MavenPlugin.md) (using maven plugin) and has a [dozen of features](Features.md) you may find useful when dealing with web resources.

> Read a [nice blog post](http://www.dzone.com/links/r/wro4j_page_load_optimization_and_lessjs.html) which will help you to understand how wro4j works.

> Want to support wro4j Open Source Development?
&lt;wiki:gadget url="http://wiki.wro4j.googlecode.com/git/gadget/paypal-donate-gadget.xml" width="100" height="40" border="0"/&gt;

### Code samples ###
```
Resource resource = Resource.valueOf("script.coffee", ResourceType.JS);
Reader reader = new FileReader("path/to/script.coffee");
Writer writer = new FileWriter("path/to/script.js");

//Transforming a coffee script file into a javascript file
new CoffeeScriptProcessor().process(resource, reader, writer);

//Using UglifyJs 
new UglifyJsProcessor().process(resource, reader, writer);

//Using BeautifyJs 
new BeautifyJsProcessor().process(resource, reader, writer);

//Using Less 
new LessCssProcessor().process(resource, reader, writer);

//Using Sass 
new SassCssProcessor().process(resource, reader, writer);
```

Read more about how processors [can be reused](ReusingProcessors.md) by your application.

# News #

**25 Sep 2014** -  **wro4j-1.7.7** is available. For a complete list of issues, see: [Issues](http://code.google.com/p/wro4j/issues/list?can=1&q=Milestone:Release-1.7.7) page or Visit [ReleaseNotes](ReleaseNotes.md) page.


**18 Jun 2014** -  **wro4j-1.7.6** is available. For a complete list of issues, see: [Issues](http://code.google.com/p/wro4j/issues/list?can=1&q=Milestone:Release-1.7.6) page or Visit [ReleaseNotes](ReleaseNotes.md) page.


**9 Apr 2014** -  A new release (**wro4j-1.7.5**) is available. For a complete list of issues, see: [Issues](http://code.google.com/p/wro4j/issues/list?can=1&q=Milestone:Release-1.7.5) page or Visit [ReleaseNotes](ReleaseNotes.md) page.


# Poll #
How do you use wro4j? Please [vote](http://www.easypolls.net/poll.html?p=50b91a60e4b0be35bc3c0e87)

See all [Polls](Polls.md)

---


# Docs #
  * Follow [@wro4j](http://twitter.com/#!/wro4j) on **twitter** for latest updates and other useful informations.
  * See the **[DesignOverview](DesignOverview.md)**, read documentation ([GettingStarted](GettingStarted.md)), [FAQ](FAQ.md), Tips, etc. Or check out the  [Features](Features.md).
  * Overview the API with the [wro4j-1.6.3 Javadoc](http://alexo.github.com/wro4j/javadoc/1.6.3/).


---

# Special Thanks #
## YourKit ##
![http://wiki.wro4j.googlecode.com/git/img/yourkit.png](http://wiki.wro4j.googlecode.com/git/img/yourkit.png)

YourKit is kindly supporting wro4j open source project with its full-featured Java Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).



---

# Community #
  * Easily report problems to [@wro4j](http://twitter.com/#!/wro4j) on **twitter**.
  * Have a quick question? You can chat with me on [StackOverflow Wro4j Chat](http://chat.stackoverflow.com/rooms/22709/wro4j)
  * Fork or follow [wro4j](http://alexo.github.com/wro4j/) on **[github](https://github.com/alexo/wro4j/)**
  * Ask general questions, provide feedback to the team on our [public mailing list](http://groups.google.com/group/wro4j).
    * In order to avoid spam, your messages will be moderated.
    * If you want to skip moderation, become the member of the group.
  * Use [Google Moderator](https://www.google.com/moderator/?#16/e=4ebbc) to ask questions, add suggestions or ideas for improvement.

---

# Issues #
http://code.google.com/p/wro4j/issues/list Search for existing issues, add a comment about your problem, and star the issue to vote for it. Click the 'New issue' link to create a new issue.

---


### Want to contribute? ###
  * Fork the project on [Github](https://github.com/alexo/wro4j). Note: the main development is performed on github (google code source is synced not very often).
  * Wandering what to work on? See task/bug list and pick up something you would like to work on.
  * Create an issue or fix one from [issues list](http://code.google.com/p/wro4j/issues/list). Before starting to commit, take a look on [WorkingWithGit](WorkingWithGit.md) wiki page.
  * If you know the answer to a question posted to our mailing list - don't hesitate to write a reply.
  * Share your ideas or ask questions on [mailing list](http://groups.google.com/group/wro4j) - that helps us improve javadocs/FAQ.
  * If you miss a particular feature - browse or ask on the mailing list, show us a sample code and describe the problem.
  * Write a blog post about how you use or extend wro4j.
  * Please suggest changes to javadoc/exception messages when you find something unclear.
  * If you have problems with documentation, find it non intuitive or hard to follow - let us know about it, we'll try to make it better according to your suggestions. Any constructive critique is greatly appreciated. Don't forget that this is an open source project developed and documented in spare time.