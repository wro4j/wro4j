# Introduction 
The WroModel can be created using JSON (Java script object notation). This is an alternative to XML representation. 

# Details 
Here is an example of how wroModel could look like in JSON format:
```javascript
{
  groups: [{
    name: "g1",
    resources: [{
      type: "JS",
      uri: "/static/app.js",
      minimize: true
    }, {
      type: "CSS",
      uri: "/static/app.css",
      minimize: true
    }]
  }, {
    name: "g2",
    resources: [{
      type: "JS",
      uri: "classpath:com/application/static/app.js",
      minimize: true
    }, {
      type: "CSS",
      uri: "http://www.site.com/static/app.css",
      minimize: true
    }]
  }]
}
```

The class handling the creation of wroModel from a JSON object is called JsonModelFactory. This class is located in wro4j-extensions project, because it requires *gson.jar* dependency (from google). 

In order to switch from xml model creation, to json model creation you have to override the *newModelFactory* method from *BaseWroManagerFactory* class:

```java
public CustomWroManagerFactory extends BaseWroManagerFactory() {
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new JsonModelFactory() {
      @Override
      protected InputStream getWroModelStream() throws IOException {
        return //the stream of the json object describing the model.
      }
    };
  }
}   
```

The above code creates an extension of BaseWroManagerFactory and inform the WroManager to use a new factory for creating WroModel object based on JSON.
