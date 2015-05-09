# Introduction
FallbackCssDataUriProcessor is an implementation of [CssDataUriProcessor](Base64DataUriSupport) which aims to allow using cssDataUri also with older versions of browsers (like IE6 or IE7). This processor is available since wro4j-1.4.7 release.


# Details 
The fallback feature of cssDataUri is possible if both versions of background images are present: encoded and original. This way, the newer browsers which know how to render base64 encoded data will use it, while older ones will skip this rule and will use the next one.

Example:

Given,

```css
#element {
    background-image: url("/path/to/myImage" );
}
```
The processor will transform it into:
```css
#element {
    background-image: url(data:image/png;base64,iVBORw0KG)     
    background-image: url("/path/to/myImage" );
}
```

Notice how the same css rule is specified twice. The first one uses base64 encoded image, while the second is the original value. 

This processor applies the same default limitation of 32KB for base64 encoding. In other words, if the image has more than 32KB it won't be changed. This limitation can be changed by overriding a method.

# Using FallbackCssDataUriProcessor with wro4j 
When using with ConfigurableWroManagerFactory, add the following line to wro.properties:

```
preProcessors=fallbackCssDataUri
```