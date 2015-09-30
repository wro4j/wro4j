---
title: Less Support
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: ""
---

LessCss is supported since version 1.2.6. More details about lesscss css meta framework can be found on [lesscss.org](http://lesscss.org/)
It supports the following features:

## Variables

Variables allow you to specify widely used values in a single place, and then re-use them throughout the style sheet, making global changes as easy as changing one line of code.

```css
@brand_color: #4D926F;

#header {
  color: @brand_color;
}

h2 {
  color: @brand_color;
}
```			

## Mixins

Mixins allow you to embed all the properties of a class into another class by simply including the class name as one of its properties. It's just like variables, but for whole classes. Mixins can also behave like functions, and take arguments, as seen in the example bellow.

```css
.rounded_corners (@radius: 5px) {
  -moz-border-radius: @radius;
  -webkit-border-radius: @radius;
  border-radius: @radius;
}
#footer {
  .rounded_corners(10px);
}
```

The result for the above mixin is this:

```css
#footer {
  -moz-border-radius: 10px;
  -webkit-border-radius: 10px;
  border-radius: 10px;
}
```

## Nested Rules

Rather than constructing long selector names to specify inheritance, in Less you can simply nest selectors inside other selectors. This makes inheritance clear and style sheets shorter.

```css
#header {
  color: red;
  a {
    font-weight: bold;
    text-decoration: none;
  }
}
```

## Operations

Are some elements in your style sheet proportional to other elements? Operations let you add, subtract, divide and multiply property values and colors, giving you the power to do create complex relationships between properties.

```css
@the-border: 1px;
@base-color: #111;

#header {
  color: @base-color * 3;
  border-left: @the-border;
  border-right: @the-border * 2;
}

#footer { 
  color: (@base-color + #111) * 1.5;
}
```

# Configuration
In order to use LessCss feature, you have to follow the following steps:

## Add wro4j-extensions dependency
Add to the pom.xml of your project wro4j-extensions dependency:

```xml
  <dependency>
    <groupId>ro.isdc.wro4j</groupId>
    <artifactId>wro4j-extensions</artifactId>
    <version>1.3.8</version>
  </dependency>
```

## web.xml configuration
 configure wro4j this way in web.xml :

```xml
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>
      ro.isdc.wro.http.WroFilter
    </filter-class>
    <init-param>
      <param-name>configuration</param-name>
      <param-value>DEPLOYMENT</param-value>
    </init-param>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory</param-value>
    </init-param>
    <init-param>
      <param-name>uriLocators</param-name>
      <param-value>servletContext,classpath,uri</param-value>
    </init-param>
    <init-param>
      <param-name>preProcessors</param-name>
      <param-value>cssUrlRewriting,cssImport,semicolonAppender,lessCss</param-value>
    </init-param>
    <init-param>
      <param-name>postProcessors</param-name>
      <param-value>cssVariables,cssMinJawr,jsMin</param-value>
    </init-param>
  </filter>
```

Notice the *lessCss* in param-value tag of preProcessors. Adding this value to the list will add in the chain of preprocessors the *LessCssProcessor* which does the magic.
Another alternative to configure the filter, is by extending ExtensionsConfigurableWroManagerFactory, setting inside the implementation the processors to use and their order and using your implementation as a value for *managerFactoryClassName* init-param.

More details about this can be found here: [ConfigurableWroManagerFactory](ConfigurableWroManagerFactory).

## Updating LessCssProcessor
*NOTE* - this feature is available since 1.3.7 version.

LessCssProcessor underlying implementation uses [less.js](https://github.com/cloudhead/less.js) implementation. Less.js code base evolves independently and the release cycles of this project are not the same as the one of the wro4j. If there is a newer version of less.js available which is not yet supported by wro4j, you can easily update it by extending !LessCssProcessor. Example:

```java
public class ExtendedLessCssProcessor extends LessCssProcessor {
  protected LessCss newLessCss() {
    return new LessCss() {
      protected InputStream getScriptAsStream() {
        return //.. the stream of the different version of less.js
      }
    };
  }
}
```

The above code shows how you can create a custom processor which extends the original LessCssProcessor and provides a different version of less.js code. This way, you can easily integrate a different version of less.js into your application.