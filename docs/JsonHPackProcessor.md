#summary Short description about JsonHPackProcessor and its purpose.

# Introduction 
JsonHPackProcessor performs the following: compress/uncompress JSON content using using [JsonHPack compression algorithm](https://github.com/WebReflection/json.hpack). 

# Details
This processor should be applied only on JS resources, and especially on resources which content is a JSON only, otherwise it won't work as expected. When a non JSON content is processed, the processor won't affect the output and will leave it unchanged. This processor was created mostly for experimental usage and was used for a [small research about JSON compression algorithms](http://www.dzone.com/links/json_compression_algorithms.html). The algorithm used by this processor is faster and much more efficient than the one used by [CJsonProcessor](CJsonProcessor).

## JsonHPackProcessor usage
The processor provides a factory method for creating processor.
This is how you create an instance responsible for packing JSON:
```java
ResourcePreProcessor packProcessor = JsonHPackProcessor.packProcessor();
```
This is how you create an instance responsible for unpacking JSON:
```java
ResourcePreProcessor unpackProcessor = JsonHPackProcessor.unpackProcessor();
```
In order to integrate the processor into wro4j chain of processor, follow [these instructions](ProcessorsManagement).