# Introduction 
[Hogan.js](http://twitter.github.com/hogan.js/) is a 2.5k JS templating engine developed at Twitter. Use it as a part of your asset packager to compile templates ahead of time or include it in your browser to handle dynamic templates.

It is very similar to [HandlebarsJsProcessor](HandlebarsJsProcessor) & [DustJsProcessor](DustJsProcessor).

# Configuring with wro4j 
Using HoganJsProcessor is as simple as adding the following line in ```wro.properties```:


```
preProcessors=hoganJs
```

or 

```
preProcessors=hoganJs.template
```
if you want to process only resources with ```template``` extension.