# Introduction
Starting with 1.4.0 release, it is possible to use Groovy DSL to build the WroModel. In order to use it, just create the *wro.groovy* file instead of _wro.xml_ and place it to the same location (in WEB-INF folder).

# Details
Here is an example of how wroModel built with Groovy:
```groovy
groups {
  group1 {
    css("/asset/*.css")
  }
  group2 {
    css(minimize: false, "/css/*.css")
    css "/css/*.less"
    js "/js/*.js"
    js "/js/script.js.coffee"
  }
  "just-another-group" {
    css("/WEB-INF/css/webinf.css")
  }
  all {
    group1()
    group2()
    groupRef("just-another-group")
  }
}
```

As you can see in this example, groovy DSL is very flexible and allows you specify groups & resources in multiple ways. It is less verbose than XML and can be easily used as the first choice.

The class handling the creation of wroModel using Groovy DSL is called !GroovyModelFactory. This class is located in wro4j-extensions project, because it requires groovy dependency. 

## Using Groovy DSL
If you like the groovy DSL, in order to switch from the xml, all you have to do is to create _wro.groovy_ and remove old _wro.xml_. This will work out of the box, because the default model used by !WroManagerFactory is called _SmartWroModelFactory_ which will use all existing model factories in order to build the model. It will try to build model in that order: xml, groovy, json. If none of these DSL is found, it will throw an exception. 

Another explicit way of using groovy DSL is to override the _newModelFactory_ method from _BaseWroManagerFactory_ class:

```java
public CustomWroManagerFactory extends BaseWroManagerFactory() {
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new GroovyModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() throws IOException {
        return //the stream of the groovy object describing the model.
      }
    };
  }
}   
```

The above code creates an extension of !BaseWroManagerFactory and inform the WroManager to use a new factory for creating WroModel object based on Groovy.