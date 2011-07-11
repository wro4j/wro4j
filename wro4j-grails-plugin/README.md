Wro4j Grails Plugin
====================

This version is a working SNAPSHOT. You will have to install the plugin using the ZIP archive full path:

    wget http://xxx/grails-wro4j-1.3.8.zip
    grails install-plugin grails-wro4j-1.3.8.zip


TODO : create a grails-app/conf/Wro.groovy file in order to configure via a ConfigSlurper or a DSL the wro.xml

TODO : make Wro4J configurable via grails-app/conf/Config.groovy


Web Resource Optimizer for Grails
----------------------------------


In order to get started with wro4j, you have to follow only 3 simple steps.


Step 1: Install plugin wro4j

    grails install-plugin wro4j



Step 2: Create web-app/WEB-INF/wro.xml

    <groups xmlns="http://www.isdc.ro/wro">
      <group name="all">
        <css>/css/*.css</css>
        <js>/js/*.js</js>
      </group>
    </groups>


Step 3: Use optimized resource

    <html>
      <head>
        <title>Web Page using wro4j</title>
        <wro:css group="all"/>
        <wro:js group="all"/>
      </head>
      <body>

      </body>
    </html>
