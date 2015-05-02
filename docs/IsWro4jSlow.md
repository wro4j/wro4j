# Introduction 
One of the most important concerns raised by users is about wro4j performance. How fast is wro4j when dealing with a large number of resources applying various processors? This problem can be a crucial one for your environment which may lead to a decision whether wro4j is appropriate tool for your application or not.

The short answer to this question is: that depends. 
Wro4j by itself is very lightweight and fast, but its speed depends mainly on the time spent on pre/post processing resources:
```
TotalTime = Sum(preProcessors) + Sum(postProcessors)
```

In other words, if the time spent on processing is slow, the total time used to process a request is slow.

## Slow Processors  
Most of the processors are fast (All processors from the **wro4j-core** module are fast), like jsMin, cssMin, cssUrlRewriting. 

There are also slow processors. It is important to understand which of them are slow and can cause a performance penalty. The **wro4j-extensions** module contains a [dozen of additional processors](AvailableProcessors), some of them being slow. In this category falls the following:

* Processors based on Rhino
--* lessCss
--* sassCss
--* coffeeScript
--* uglifyJs
--* jsLint
--* jsHint
--* cssLint
* Processors based on Ruby
--* rubySassCss

It is also possible that a custom processor can be a bottleneck if it performs some very complex or innefficient logic. 
In theory, all processors based on rhino can be replaced with a similar implementation based on java (if exist) or based on V8 javascript engine. This replacement is one of the future challenges.

It is important to mention that there were [RhinoPerformanceImprovement some progress] toward making rhino based processors to perform faster, but the results are not considerable when comparing to V8 javascript engine, because rhino is not running javascript natively.
   
----
A [ blog post](http://axelhzf.com/blog/2012-05-28-grunt-vs-wro4j.html) comparing grunt with wro4j