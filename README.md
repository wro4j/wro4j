# <img src="http://code.google.com/p/wro4j/logo"> Web Resource Optimizer for Java

wro4j is a free and Open Source Java project which will help you to [easily improve](http://alexo.github.com/wro4j) your web application page loading time. It can help you to keep your static resources (js & css) [well organized](http://code.google.com/p/wro4j/wiki/WroFileFormat), merge & minify them at [run-time](http://code.google.com/p/wro4j/wiki/Installation) (using a simple filter) or [build-time](http://code.google.com/p/wro4j/wiki/MavenPlugin) (using maven plugin) and has a [dozen of features](http://code.google.com/p/wro4j/wiki/Features) you may find useful when dealing with web resources.


# Getting Started


In order to get started with wro4j, you have to follow only 3 simple steps.


## Step 1: Add WroFilter to web.xml



		<filter>
		  <filter-name>WebResourceOptimizer</filter-name>
		  <filter-class>
			ro.isdc.wro.http.WroFilter
		  </filter-class>
		</filter>
		 
		<filter-mapping>
		  <filter-name>WebResourceOptimizer</filter-name>
		  <url-pattern>/wro/*</url-pattern>
		</filter-mapping>
		
## Step 2: Create wro.xml
		

		<groups xmlns="http://www.isdc.ro/wro">
		  <group name="all">
			<css>/asset/*.css</css>
			<js>/asset/*.js</js>
		  </group>
		</groups> 		
		
## Step 3: Use optimized resources

		<html>
		  <head>
			<title>Web Page using wro4j</title>
			<link rel="stylesheet" type="text/css" href="/wro/all.css" />
			<script type="text/javascript" src="/wro/all.js"/>
		  </head>
		  <body>
			
		  </body>
		</html>		

		
# Documentation

The documentation for this project is located on google code project page: http://code.google.com/p/wro4j/


# Issues

Found a bug? Report it to the issue tracker: http://code.google.com/p/wro4j/issues/list


# Feedback

If you have any questions or suggestions, please feel free to post a comment to the discussion group: https://groups.google.com/forum/#!forum/wro4j

[Follow me](http://twitter.com/#!/wro4j) on tweeter.


# License

This project is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).