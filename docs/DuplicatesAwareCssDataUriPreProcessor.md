# Introduction
DuplicatesAwareCssDataUriPreProcessor is a custom implementation of [Base64DataUriSupport](Base64DataUriSupport) [CssDataUriPreProcessor](CssDataUriPreProcessor) which instead of replacing a url blindly with dataUri, is is smart enough to detect duplicated image url and avoid replacing it with dataUri, thus avoiding situations when the css grows more than necessary because of cssDataUri duplication.

# Using DuplicatesAwareCssDataUriPreProcessor with wro4j
When using with ConfigurableWroManagerFactory, add the following line to wro.properties:

```
preProcessors=duplicateAwareCssDataUri
```