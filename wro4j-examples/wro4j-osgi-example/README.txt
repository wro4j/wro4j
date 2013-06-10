Overview
========

The main goal of this example is to show how wro4j can be used inside
OSGi.

In addition, this example also shows that it's possible to use wro4j with
webjars and also how to configure wro4j filter using Spring.


What is OSGi?
=============

Essentially, the OSGi framework provides a mechanism for managing
dependencies and life cyles of "bundles" (which are really just jar
and war files with some extra metadata inside a MANIFEST.MF file).
Using OSGi, you can define the specific dependencies between bundles
and stop, update and restart bundles without bringing down the entire
application.

If you search around, you can find more info.

This example shows how to create a war that has the necessary
OSGi meta data so that it can be installed into a OSGi container.

How it works
======================

The maven-bundle-plugin defined inside this project's pom.xml
generates the necessary OSGi metadata. During the maven build, it creates a valid
"META-INF/MANIFEST.MF" which is included in the final war.

Take a look at the <configuration> section inside the
<maven-bundle-plugin> section in this project's pom. The
<Import-package> lists the packages this webapp requires. The packages
listed there are expected to be provided by the OSGi container. For
example, the first entry in <Import-package> is:

`javax.servlet; version="[2.5.0, 4.0.0)"`

which  means that this webapp will not be allowed to run inside a OSGi
container, unless that OSGi container has a bundle installed that
makes the `javax.servlet` package (version 2.5.0 to 4.0.0) available.
If that package is not provided by any of the bundles installed to the
OSGi container, then this webapp will not start.

How to Install and Use
======================

To run this webapp outside of osgi, simply build it using maven and
deploy to your favorite servlet container. Or, you can also use maven
to run jetty:

mvn install jetty:run-war

Browse to http://localhost:8080/wro4j-osgi to see the app in action.

To build webapp so that it is configured to work inside OSGi, use the
maven "osgi" profile like so:

mvn -Posgi install

Here are the steps to install and run inside Apache Karaf which uses
the Apache Felix OSGi implementation by default:

Step 1) download karaf from here:

http://karaf.apache.org/index/community/download.html#Karaf2.3.1

Step 2) Uncompress the archive, and cd into the `apache-karaf-2.3.1` directory.

Step 3) Start karaf using the following command:

./bin/karaf

Step 4) Install this webapp bundle to karaf

Let's try installing this webapp bundle into OSGi. One way to do that
inside Karaf is to use this command:

karaf@root> install -s mvn:ro.isdc.wro4j/wro4j-osgi-example/1.6.4-SNAPSHOT/war

This will throw an error message, something like:

Error executing command: Error installing bundles:
	Unable to start bundle mvn:ro.isdc.wro4j/wro4j-osgi-example/1.6.4-SNAPSHOT/war: Unresolved constraint in bundle ro.isdc.wro4j.osgi-example [264]: Unable to resolve 264.0: missing requirement [264.0] osgi.wiring.package; (&(osgi.wiring.package=ro.isdc.wro.cache)(version>=1.6.0)(!(version>=2.0.0)))

This is because this webapp bundle expects OSGi to supply all required
dependencies. At this point, we could install each dependency
individually, but since that's a bit tedious, Karaf provides a
mechanism called "features" that allows to easily deploy an app along
with all necessary dependencies.

So, let's uninstall the webapp for now, and we'll use features in the
next step.

karaf@root> uninstall <bundleid>

Step 5) Deploy features.xml

The features.xml file that is build during the mvn install can be
found here:

target/classes/features.xml

Make a copy of that file and copy it into the "deploy/" directory of
your karaf installation.

Step 6) Install the features

Install and start all bundles, using the following command:

karaf@root> features:install wro4j-osgi-example

Browse to http://localhost:8181/wro4j-osgi and you should see the app
running inside OSGI!

Webjars
=======

This project also makes use of webjars to pull in some thirdparty css
and javascript from projects such as bootstrap, jquery and angular.js

Webjars are great because they make it possible to use specific
versions of javascript and css libraries without cluttering your
webapp directory.

Take a look at the dependencies section of this project's pom and
notice there are several dependencies with groupid of org.webjar.

See http://webjars.org for more.

Note that inside osgi, webjars need to be included inside the war so
that the resources can be found on the classpath by wro4j.
