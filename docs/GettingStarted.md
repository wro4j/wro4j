---
title: Getting started 
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: "To get started with this theme, first make sure you have all the prerequisites in place; then build the theme following the sample build commands. Because this theme is set up for single sourcing projects, it doesn't follow the same pattern as most Jekyll projects (which have just a _config.yml file in the root directory)."
---


Related Pages: [wro4j in a nutshell](http://wro4j.github.com/wro4j), [Installation](Installation), [MavenPlugin](MavenPlugin),[DesignOverview](DesignOverview)

Before configuring wro4j into your application, it might be a good idea to see the [DesignOverview overall picture of wro4j design]. 


Getting started is as simple as following 3 steps:

* Configure [wro4j filter in web.xml](Installation)
* Create [WEB-INF/wro.xml](WroFileFormat) file
* Add resource to your html


## Step 1: Add the binary distribution of wro4j to you project classpath 

You have three options here:
### Add wro4j maven dependency. It can be found on maven central repo.
```xml
<dependency>
  <groupId>ro.isdc.wro4j</groupId>
  <artifactId>wro4j-core</artifactId>
  <version>${wro4j.version}</version>
</dependency>
```

If you want to use additional features (like Google closure compiler or YUI compressor), add also wro4j-extensions dependency. The only requirement is to use at least JDK 1.6.
```xml
<dependency>
  <groupId>ro.isdc.wro4j</groupId>
  <artifactId>wro4j-extensions</artifactId>
  <version>${wro4j.version}</version>
</dependency>
```

Note: using maven is the recommended option, because it will bring for you all dependencies (like commons-io, google closure or rhino).

### Second option is to download the binary distribution from [Downloads](https://drive.google.com/folderview?id=0Bw_8FNG2SipAT2VHOHlHbW9WLUE&usp=sharing) page and to put it in the classpath. <br><br>

### The third option is to build the library yourself:    
* Checkout from [github](https://github.com/wro4j/wro4j)).
* Build instruction with maven:

```python
cd wro4j
mvn install
```


## Step 2: Configure the wro4j Servlet Filter in web.xml 
More details can be found [here](Installation)

## Step 3: Create wro.xml under WEB-INF directory and organize your resources in groups 

```xml
<groups xmlns="http://www.isdc.ro/wro">
  <group name="g1">
    <js>classpath:com/mysite/resource/js/1.js</js>
    <css>classpath:com/mysite/resource/css/1.css</css>
    <group-ref>g2</group-ref>
  </group>

  <group name="g2">
    <js>/dwr/engine.js</js>
    <group-ref>g3</group-ref>
    <css>classpath:/static/css/2.css</css>
    <js>classpath:/static/*.js</js>
  </group>

  <group name="g3">
    <css>/static/css/style.css</css>
    <css>/static/css/global/*.css</css>
    <js>/static/js/**</js>
    <js>http://www.site.com/static/plugin.js</js>
  </group>

</groups>
```

For more information, see the ['wro.xml' File Format specification](WroFileFormat).

### About groups 
A group is a logical bundle of resources. A single group can contain both CSS and !JavaScript resources.  A file will be built for each different type of resource included in the group. For example, if a group named 'core-scripts' only contains '.js' files, then a single file named 'core-scripts.js' will be generated.  If a group named 'main' contains both '.js' and '.css' files, then two files will be generated, named 'main.js' and 'main.css'.

When creating groups, you may adopt different strategies. For instance:

* You can create a single group containing all files... (not so nice:)
* Each group may refer the resources contained in for each page of the application, like: home, contact, checkout, etc... 
* A group may contain widget related resources. Lets say you use tableSorter jquery widget which besides a js, has also few css used for default styling. In this case you can create a group called *tableSorter* which will include associated js & css resources. 
* Or you can group your resources whatever way you like.

## Step 4: Include desired groups as js or css resources 

Here is an example on how to include in a page all resources from group g2. Please not that you use only one reference to link all CSS resources in a group (same applies for JS resources). You have to use the correct extension '.css' or '.js' after the group name.

```html
<html>
  <head>
    <title>Web Frameworks Comparison</title>
    <link rel="stylesheet" type="text/css" href="/wro/g2.css" />
    <script type="text/javascript" src="/wro/g2.js"></script>
  </head>
  <body>
    //Body
  </body>
</html>
```

----

## Class Diagram 

[The code class structure is diagrammed here](ClassDiagram.jpg).