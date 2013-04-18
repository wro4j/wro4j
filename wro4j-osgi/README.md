# Overview

The wro4j library uses
[spi](http://en.wikipedia.org/wiki/Service_provider_interface) to find
classes at runtime. There are some issues with using SPI inside osgi.
This project makes it possible to use wro4j inside osgi by temporarily
setting the thread context class loader (tccl).  

# Usage

First, create the wro4j-osgi bundle by building this module using
`mvn install`. 

Then install the bundle into your osgi container. For karaf, we used: 

    install mvn: mvn:ro.isdc.wro4j/wro4j-osgi/1.6.2

All processors inside wro4j-core should work in osgi. 

We also needed a less compiler in osgi. So, this bundle also includes
the relevant parts from wro4j-extensions in order to use either the
less4j or the lessCss compiler processor.

To use less4j inside osgi, it's also required to install bundles for
less4j and anltr to satisfy dependencies. Here's the syntax for karaf:

    install wrap:mvn:org.antlr/antlr-runtime/3.4
    install wrap:mvn:com.github.sommeri/less4j/1.0.3-SNAPSHOT
    
To use lessCss inside osgi, it's also required to install bundles for
rhinojs and commons-pool. Here's the syntax for karaf: 

    install mvn:commons-pool/commons-pool/1.6
    install wrap:mvn:org.mozilla/rhino/1.7R1

After that, wro4j core + less compilers will be available inside osgi!
