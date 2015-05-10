# Introduction
Default implementation of the *WroModelFactory* builds the WroModel after an xml is parsed. This solution is good enough when you know a priori how your model looks like and what are the resources your system should use. But what should you do when you know how the model looks like only in the runtime?


# Details 
Every WroModel is built using a factory called *WroModelFactory*. This is how the interface looks like:

```java
public interface WroModelFactory extends ObjectFactory<WroModel> {
  /**
   * Called to indicate that the factory is being taken out of service.
   */
  void destroy();
}
```

The *!ObjectFactory* interface defines a single method (create): 

```java
public interface ObjectFactory<T> {
  T create();
}
```

The create method is called each time the fresh version of WroModel is required by *WroManager*. You can easily create a custom implementation of *WroModelFactory* and build the WroModel using the runtime knowledge.

Example:

```java
public class MyWroModelFactory implements WroModelFactory {
  public WroModel create() {
    return new WroModel().addGroup(new Group("g1").addResource(Resource.create("/path/to/resource.js", ResourceType.JS)));
  }

  public void destroy() {
    //do some clean-up if required.
  }
}
```