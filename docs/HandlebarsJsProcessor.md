# Introduction 
Web apps are using JavaScript to create dynamic interfaces now more than ever before, and that's not a trend that will change any time soon. DOM manipulation is great for simpler JavaScript apps, but what do you do when you're changing huge chunks of the document with each change of the view? That's where JavaScript templating comes into play.

Handlebars is a javascript library which provides the power necessary to let you build semantic templates effectively with no frustration. The basic idea behind Handlebars, is to compile a html template into a function which can be easily used to build dynamic content.

Basic example:

```javascript
//Given a JSON Data
var data = { 
  items: [{text: “Hello World”}, ... ]
};

//Normal JS
var div = document.createElement(“div”),    
    items = data.item;
for(var i = 0; i < data.item.length) {  
  div.append(items[i].text);
}

//With Handlebars.js
var html = “<div>{{#each items}}{{text}}{{/each}}</div>”;
var template = Handlebars.compile(html);template.html(data);
```

More details about handlebars can be found at the following links:
  * [Handlebars Project Home](http://handlebarsjs.com/precompilation.html)
  * [Getting started with Handlebars](http://thinkvitamin.com/code/getting-started-with-handlebars-js/)

Besides Handlebars, there are many more similar libraries on the market, like: [mustache.js](https://github.com/janl/mustache.js/), [jQote2](http://aefxx.com/jquery-plugins/jqote2/), [pure](http://beebole.com/pure/), [eco](https://github.com/sstephenson/eco/), [Underscore.js](http://documentcloud.github.com/underscore/), [jTemplates](http://jtemplates.tpython.com/) & others. 

# Handlebars.js Processor 
The HandlebarsJsProcessor is available since wro4j-1.4.7 and uses the [handlebars.js](https://github.com/wycats/handlebars.js/) library written in javascript. The processor uses Rhino to interpret the javascript into Java. The processor can be used as both: pre & post processor. 

The processor can be applied on resources of type javascript, though the actual content of the resources is not a javascript, it is actually an html template.

An example of resource expected by handlebarsJs processor:

```html
<h1>Todos</h1>

<div class="row-fluid">
  <input type="text" id="add-todo" placeholder="What do you need to do?" />
</div>
<div class="row-fluid">
  <div class="row-fluid span5">
    <input type="checkbox" id="mark-all" /><label for="mark-all"
      class="span4"><strong>Mark all as complete</strong></label>
  </div>

  <ul id="todo-list"></ul>

  <div id="todo-status" class="row-fluid"></div>
</div>
```

After this resource is processed, the following output is produced:

```javascript
(function() { var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};templates['null'] = template(function (Handlebars,depth0,helpers,partials,data) {
  helpers = helpers || Handlebars.helpers;
  var foundHelper, self=this;


  return "<h1>Todos</h1>\n\n<div class=\"row-fluid\">\n  <input type=\"text\" id=\"add-todo\" placeholder=\"What do you need to do?\" />\n</div>\n<div class=\"row-fluid\">\n  <div class=\"row-fluid span5\">\n    <input type=\"checkbox\" id=\"mark-all\" /><label for=\"mark-all\"\n      class=\"span4\"><strong>Mark all as complete</strong></label>\n  </div>\n\n  <ul id=\"todo-list\"></ul>\n\n  <div id=\"todo-status\" class=\"row-fluid\"></div>\n</div>";} ); })();
```

## How to use Handlbars with wro4j
Using HandlebarsJsProcessor is very similar to using any other processor with wro4j. 

Given the following WroModel:

```groovy
groups {
  groupWithTemplates {
    js("/templates/*.handlebars")
  }
  anotherGroup {
    js("/static/*.js")
    js("/static/*.css")
  }
}
```


### Using ConfigurableWroManagerFactory
The simplest way to instruct wro4j to use HandlebarsJsProcessor is with **ConfigurableWroManagerFactory** which allows processors configuration using a property file (wro.properties). Add the following to **wro.properties**:

```
preProcessors=handlebarsJs
```

The above configuration will use HandlebarsJsProcessor as a pre processor, meaning that each resource will be processed individually by processor before it is merged. It is possible to use it as post processor:

```
postProcessors=handlebarsJs
```

As mentioned earlier, the HandlebarsJsProcessor is applied only on resources of type js (marked as js resource in wro model). This can be a problem when your model contains mixed type of resources (handlebars templates and valid javascript). If you would like to apply HandlebarsJsProcessor only on resources with certain extension, you should update the configuration as below:

```
preProcessors=handlebarsJs.handlebars
```

Using this type of pattern: **handlebarsJs.<extension>** allows simplified configuration which performs the following under the hood:
Decorates the configured processor with ExtensionsAwareProcessorDecorator. In other words, it allows a processor to be applied only when the processed resource has the configured extension. 

### Using custom managerFactory 
Another option of using HandlebarsJsProcessor with wro4j is by implementing custom [WroManagerFactory](WroManager):

```java
public class CustomWroManagerFactory
    extends BaseWroManagerFactory {
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    //similar to preProcessors=handlebarsJs
    factory.addPreProcessor(new HandlebarsJsProcessor());
    
    //or use extension aware decorator.
    //similar to preProcessors=handlebarsJs.handlebars 
    factory.addPreProcessor(ExtensionsAwareProcessorDecorator.decorate(new HandlebarsJsProcessor()).addExtension("handlebars"));
    return factory;
  }
}
```