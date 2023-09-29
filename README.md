# <img src="https://raw.githubusercontent.com/wro4j/wro4j/master/docs/wro4j-logo.png" style="max-height:1.2em;max-width: 100%;vertical-align: bottom;"> Web Resource Optimizer for Java

[![Join the chat at https://gitter.im/wro4j/wro4j](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wro4j/wro4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://api.travis-ci.org/wro4j/wro4j.svg)](http://travis-ci.org/wro4j/wro4j)
[![Coverage Status](https://codecov.io/github/wro4j/wro4j/coverage.png?branch=master)](https://codecov.io/github/wro4j/wro4j?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ro.isdc.wro4j/wro4j-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ro.isdc.wro4j/wro4j-core)
[![Stories in Ready](https://badge.waffle.io/wro4j/wro4j.png?label=ready&title=Ready)](http://waffle.io/wro4j/wro4j)

Wro4j is a free and Open Source Java project which will help you to [easily improve](http://wro4j.github.com/wro4j) your web application page loading time. It can help you to keep your static resources (js & css) [well organized](http://wro4j.readthedocs.org/en/stable/WroFileFormat), merge & minify them at [run-time](http://wro4j.readthedocs.org/en/stable/Installation) (using a simple filter) or [build-time](http://wro4j.readthedocs.org/en/stable/MavenPlugin) (using maven plugin) and has a [dozen of features](http://wro4j.readthedocs.org/en/stable/Features) you may find useful when dealing with web resources.

## Getting Started

Wro4j has 2 possible operating modes:

* Runtime solution - the resources are processed lazily after the first request. Assuming you have defined 100 groups and only one is requested, the remaining 99 won't be processed.
* Build time solution - the resources are processed when the application is built. You are in control of defining what groups should be generated (either all of them or only a subset enumerated in configuration).

If what you need is to have all resources available during application startup, then probably using a build-time solution (Maven plugin) is the best suited for you. Alternatively, you could use the runtime solution and explicitly perform requests for all the groups after application startup.

In order to get started with Wro4j's runtime solution, you have to follow only 3 simple steps.

### Step 1: Add WroFilter to web.xml

```xml
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
</filter>

<filter-mapping>
  <filter-name>WebResourceOptimizer</filter-name>
  <url-pattern>/wro/*</url-pattern>
</filter-mapping>
```

### Step 2: Create wro.xml

```xml
<groups xmlns="http://www.isdc.ro/wro">
  <group name="all">
    <css>/asset/*.css</css>
    <js>/asset/*.js</js>
  </group>
</groups>
```

### Step 3: Use optimized resources

```html
<html>
  <head>
    <title>Web Page using wro4j</title>
    <link rel="stylesheet" type="text/css" href="/wro/all.css" />
    <script type="text/javascript" src="/wro/all.js"/>
  </head>
  <body>
    <!-- Insert body of your page here -->
  </body>
</html>
```

## Documentation

The documentation for this project is located [here](http://wro4j.readthedocs.org/en/stable/)

## Issues

Found a bug? Report it to the [issue tracker](https://github.com/wro4j/wro4j/issues)

## Feedback

If you have any questions or suggestions, please feel free to post a comment to the [discussion group](https://groups.google.com/forum/#!forum/wro4j)

[Follow us](http://twitter.com/#!/wro4j) on Twitter.

## License

This project is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
