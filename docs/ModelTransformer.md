# Introduction
ModelTransformer is tightly coupled with Model concept and as its name states, handles the Model transformation. Transformation can mean anything, starting with adding/removing resources and/or groups or changing their types or values. 

# Work Flow
A ModelTransformer is applied only after the model is built using !WroModelFactory. Once the model is created, it is transformed by ModelTransformer and the transformed model is used as a result for all subsequent operations. 

It is possible to apply a list of model transformers, not only one. When multiple ModelTransformer's are defined, all of them perform a transformation in the order in which these are defined. 

The flow is described with the below image:
[http://wiki.wro4j.googlecode.com/git/img/sketch/ModelTransformers.png]

[![ModelTransformers](img/sketch/ModelTransformers.png)](img/sketch/ModelTransformers.png)


# Wildcard Expansion ModelTransformer
The most important use-case (the one for which ModelTransformer was created) is wildcard expansion feature. The purpose of this feature is to detect all resources containing wildcards and expand them by adding all matching resources to the same group. In other words, given the following model:

```groovy
groups {
  g1 {
    js("/path/to/js/*.js")
  }
}
```

after applying wildcard expansion ModelTransformer, it becomes:

```groovy
groups {
  g1 {
    js("/path/to/js/a.js")
    js("/path/to/js/b.js")
    js("/path/to/js/c.js")
    js("/path/to/js/d.js")
  }
}
```

Notice that the resulting model contains 3 more resources of the same type as the original one. Also, the resulted resources are ordered alphabetically. 

The feature is very useful, because it allows to catch early duplicated resources. When a duplicate resource is found, it won't be included. 
Another side-effect of this feature, is improved error messages for processors performing static code analysis like: !CssLint, !JsLint or !JsHint. This ModelTransformer is implemented by !WildcardExpanderModelTransformer class and it is added by default if no model transformers are set. 

# Implementation Details 
Any model transformer can be created by implementing the following interface:
```java
public interface Transformer<T> {
  T transform(T input) throws Exception;
}
```
The interface is defined in a generic way, because it can be used for other purposes too. Here is a sample implementation which leaves the WroModel unchanged:

```
public class NoOpModelTransformer implements Transformer<WroModel> {
  public WroModel transform(WroModel input) throws Exception {
    return input;
  }
}
```

The above implementation is not very useful, since it doesn't mutate the initial state of the model. But it can be updated to suit any needs.

In order to add a custom ModelTransformer, the following options are available:

### Add a new transformer to existing list of transformers:
```java
  new BaseWroManagerFactory().addModelTransformer(transformer);
```

### Define new list of transformers
```java
  List<Transformer<WroModel>> transformerList = // create transformers list.
  new BaseWroManagerFactory().setModelTransformers(transformerList);
```