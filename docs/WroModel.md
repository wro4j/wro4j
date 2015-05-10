# Introduction
Wro4j Model is a data structure containing information about client resources and how they are grouped. 

## Details 
Wro4j model is used by **WroManager** to identify which resources should be merged and processed. The class holding the model is called [WroModel](https://github.com/wro4j/wro4j/blob/v1.7.7/wro4j-core/src/main/java/ro/isdc/wro/model/WroModel.java). The creation of the model is a responsibility of a factory interface called [WroModelFactory](https://github.com/wro4j/wro4j/blob/v1.7.7/wro4j-core/src/main/java/ro/isdc/wro/model/factory/WroModelFactory.java).

There are a couple of implementations of this factory: [XmlModelFactory](https://github.com/alexo/wro4j/blob/v1.3.7/wro4j-core/src/main/java/ro/isdc/wro/model/factory/XmlModelFactory.java) & [JsonModelFactory](https://github.com/alexo/wro4j/blob/v1.3.7/wro4j-extensions/src/main/java/ro/isdc/wro/extensions/model/factory/JsonModelFactory.java). Each of these implementation builds a model using some kind of DSL (domain specific language), more specificaly for XML and JSON.

As a result a model can be described by an xml or a json file. If you need a different DSL support (ex: groovy), you can easily create a new factory (*!GroovyModelFactory*) which would parse a groovy file and would create a [WroModel](https://github.com/alexo/wro4j/blob/v1.3.7/wro4j-core/src/main/java/ro/isdc/wro/model/WroModel.java) based on it.

[![Model Class Diagram](img/uml/Model%20Class%20Diagram.png)](img/uml/Model%20Class%20Diagram.png)



The **WroModelFactoryDecorator** and its descendants are used to enhance the model with new features. 

For instance, **ScheduledWroModelFactory** handles the model scheduled update, by running periodically a thread which invalidates the model from the cache and reload it by reading the latest version of the model.

Similarly, **FallbackAwareWroModelFactory** - is a decorator which handles the situation when the wro model cannot be loaded at some point (resource cannot be located or the model is invalid). It holds the last known good model and reuse it until a new valid instance of model is available. This can be useful in situations when you store your model in a different location than the server where wro4j is deployed. This approach allows your application to work flawlessly even when the storage holding the model becomes unavailable at some point in time.