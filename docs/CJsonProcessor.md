# Introduction
CJsonProcessor performs the following: compress/uncompress JSON content using using [http://stevehanov.ca/blog/index.php?id=104 CJson compression algorithm]. 

# Details 
This processor should be applied only on JS resources, and especially on resources which content is a JSON only, otherwise it won't work as expected. When a non JSON content is processed, the processor won't affect the output. This processor was created mostly for experimental usage and was used for a [http://www.dzone.com/links/json_compression_algorithms.html small research about JSON compression algorithms]

## CJsonProcessor usage
The processor provides a factory method for creating processor.
This is how you create an instance responsible for packing JSON:
```java
ResourcePreProcessor packProcessor = CJsonProcessor.packProcessor();
```
This is how you create an instance responsible for unpacking JSON:
```java
ResourcePreProcessor unpackProcessor = CJsonProcessor.unpackProcessor();
```
In order to integrate the processor into wro4j chain of processor, follow [these instructions](ProcessorsManagement).


