Related Pages: [wro4j in a nutshell](http://alexo.github.com/wro4j), [Installation](Installation.md), [MavenPlugin](MavenPlugin.md),[DesignOverview](DesignOverview.md)

Before configuring wro4j into your application, it might be a good idea to see the [overall picture of wro4j design](DesignOverview.md).


Getting started is as simple as following 3 steps:
  1. Configure [wro4j filter in web.xml](Installation.md)
  1. Create [WEB-INF/wro.xml](WroFileFormat.md) file
  1. Add resource to your html


## Step 1: Add the binary distribution of wro4j to you project classpath ##

You have three options here:
  * Add wro4j maven dependency. It can be found on maven central repo.
```
<dependency>
  <groupId>ro.isdc.wro4j</groupId>
  <artifactId>wro4j-core</artifactId>
  <version>${wro4j.version}</version>
</dependency>
```

If you want to use additional features (like Google closure compiler or YUI compressor), add also wro4j-extensions dependency. The only requirement is to use at least JDK 1.6.
```
<dependency>
  <groupId>ro.isdc.wro4j</groupId>
  <artifactId>wro4j-extensions</artifactId>
  <version>${wro4j.version}</version>
</dependency>
```

Note: using maven is the recommended option, because it will bring for you all dependencies (like commons-io, google closure or rhino).

  * Second option is to download the binary distribution from [Downloads](http://code.google.com/p/wro4j/downloads/list) page and to put it in the classpath. <br><br>
<ul><li>The third option is to build the library yourself:<br>
<ul><li>Checkout from <a href='https://github.com/alexo/wro4j'>github</a>).<br>
</li><li>Build instruction with maven:<br>
<pre><code>cd wro4j<br>
mvn install<br>
</code></pre></li></ul></li></ul>


<h2>Step 2: Configure the wro4j Servlet Filter in web.xml</h2>
More details can be found <a href='Installation.md'>here</a>

<h2>Step 3: Create wro.xml under WEB-INF directory and organize your resources in groups</h2>

<pre><code>&lt;groups xmlns="http://www.isdc.ro/wro"&gt;<br>
  &lt;group name="g1"&gt;<br>
    &lt;js&gt;classpath:com/mysite/resource/js/1.js&lt;/js&gt;<br>
    &lt;css&gt;classpath:com/mysite/resource/css/1.css&lt;/css&gt;<br>
    &lt;group-ref&gt;g2&lt;/group-ref&gt;<br>
  &lt;/group&gt;<br>
<br>
  &lt;group name="g2"&gt;<br>
    &lt;js&gt;/dwr/engine.js&lt;/js&gt;<br>
    &lt;group-ref&gt;g3&lt;/group-ref&gt;<br>
    &lt;css&gt;classpath:/static/css/2.css&lt;/css&gt;<br>
    &lt;js&gt;classpath:/static/*.js&lt;/js&gt;<br>
  &lt;/group&gt;<br>
<br>
  &lt;group name="g3"&gt;<br>
    &lt;css&gt;/static/css/style.css&lt;/css&gt;<br>
    &lt;css&gt;/static/css/global/*.css&lt;/css&gt;<br>
    &lt;js&gt;/static/js/**&lt;/js&gt;<br>
    &lt;js&gt;http://www.site.com/static/plugin.js&lt;/js&gt;<br>
  &lt;/group&gt;<br>
<br>
&lt;/groups&gt;<br>
</code></pre>

<blockquote>(For more information, see the <a href='WroFileFormat.md'>'wro.xml' File Format specification</a>.)</blockquote>

<h3>About groups</h3>
A group is a logical bundle of resources. A single group can contain both CSS and JavaScript resources.  A file will be built for each different type of resource included in the group. For example, if a group named 'core-scripts' only contains '.js' files, then a single file named 'core-scripts.js' will be generated.  If a group named 'main' contains both '.js' and '.css' files, then two files will be generated, named 'main.js' and 'main.css'.<br>
<br>
When creating groups, you may adopt different strategies. For instance:<br>
<ol><li>You can create a single group containing all files... (not so nice:)<br>
</li><li>Each group may refer the resources contained in for each page of the application, like: home, contact, checkout, etc...<br>
</li><li>A group may contain widget related resources. Lets say you use tableSorter jquery widget which besides a js, has also few css used for default styling. In this case you can create a group called <b>tableSorter</b> which will include associated js & css resources.<br>
</li><li>Or you can group your resources whatever way you like...</li></ol>

<h2>Step 4: Include desired groups as js or css resources</h2>

Here is an example on how to include in a page all resources from group g2. Please not that you use only one reference to link all CSS resources in a group (same applies for JS resources). You have to use the correct extension '.css' or '.js' after the group name.<br>
<br>
<pre><code>&lt;html&gt;<br>
  &lt;head&gt;<br>
    &lt;title&gt;Web Frameworks Comparison&lt;/title&gt;<br>
    &lt;link rel="stylesheet" type="text/css" href="/wro/g2.css" /&gt;<br>
    &lt;script type="text/javascript" src="/wro/g2.js"&gt;&lt;/script&gt;<br>
  &lt;/head&gt;<br>
  &lt;body&gt;<br>
    //Body<br>
  &lt;/body&gt;<br>
&lt;/html&gt;<br>
</code></pre>

<hr />

<h2>Class Diagram</h2>

<a href='http://wro4j.googlecode.com/svn/wiki/ClassDiagram.jpg'>The code class structure is diagrammed here</a>.