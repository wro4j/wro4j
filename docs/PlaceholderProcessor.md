# Introduction
The PlaceholderProcessor was created as a result of this thread discussion: [https://groups.google.com/forum/#!topic/wro4j/9d91oA4FhSs Handling expression language in CSS]. 

The main idea is to be able to handle expressions in web resources dynamically. 

# Details
An expression is referenced with: **${}** characters.

Here are some examples of web resource with experssions: 

```html
body {
  color: ${BODY_COLOR}
}
```

or

```javascript
  function notify() {
    alert('${NOTIFY_MESSAGE}');
  }
```

There are multiple use-cases for this feature. Below you'll find how to implement it.

## Creating PlaceholderProcessor
```java
    //1. create properties object
    final Properties properties = new Properties();
    properties.setProperty("prop1", "value1");
    //2. Create properties Factory
    final ObjectFactory<Properties> propertiesFactory = new ObjectFactory<Properties>() {
      public Properties create() {
        return properties;
      }
    };
    //3. instantiate PlaceholderProcessor
    final ResourcePreProcessor processor = new PlaceholderProcessor().setPropertiesFactory(propertiesFactory).setIgnoreMissingVariables(false);
```

Notice the following in the above code:
  * Creates a Property object containing values to interpolate. This can be dynamically created from some properties file based on your environment configurations.
  *  **ObjectFactory** allows **PlaceholderProcessor** to retrieve each time the resource is parsed the fresh instance of the Properties object by calling the create method. Using **ObjectFactory** is a flexible approach for dynamic interpolation.
  * The **PlaceholderProcessor** is created with propertiesFactory set, along with the flag indicating whether the missing variables should be ignored or not. If this value is set to false, a runtime exception will be thrown when a expression defined in web resource doesn't have a correspondent value in supplied Properties object.