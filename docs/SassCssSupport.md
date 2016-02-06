---
title: Sass Support
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: ""
---

Starting with version 1.3.0, SassCss resource processor was added. 
It supports the following features:

## Comments
```css
// foo
body
  // bar
  a
    :color #fff
```

compiles to

```css
body a {
  color: #fff;}
```
		
## Variables
```css
!red = #ff0000
body
  :color !red
and

red: #ff0000
body
  :color !red
```

compiles to

```css
body {
  color: #ff0000;}
```
## Selector Continuations
```css
a
  :color #fff
  &:hover
    :color #000
  &.active
    :background #888
    &:hover
      :color #fff
```

compiles to

```css
a {
  color: #fff;}

a:hover {
  color: #000;}

a.active {
  background: #888;}

a.active:hover {
  color: #fff;}
```

## Literal Javascript 
```css
type: "solid"
size: 1
input
  :border { parseInt(size) + 1 }px {type} #000
```

compiles to

```css
input {
  border: 2px "solid" #000;}
```

## Property Expansions 
```css
div
  =border-radius 5px
```

compiles to

```css
div {
  -webkit-border-radius: 5px;
  -moz-border-radius: 5px;}
```

## Mixins 
```css
 +large
   :font-size 15px
 +striped
   tr
     :background #fff
     +large
     &:odd
       :background #000
 table
   +striped
   :border none
```

compiles to

```css
table {
  border: none;}
table tr {
  background: #fff;}
table tr {
  font-size: 15px;}
table tr:odd {
  background: #000;}
```

# Configuration
In order to use SassCss feature, you have to follow the following steps:

## Add wro4j-extensions dependency
Add to the pom.xml of your project wro4j-extensions dependency:
```xml
  <dependency>
    <groupId>ro.isdc.wro4j</groupId>
    <artifactId>wro4j-extensions</artifactId>
    <version>1.3.0</version>
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
      <param-value>servletContext,classpath,url</param-value>
    </init-param>
    <init-param>
      <param-name>preProcessors</param-name>
      <param-value>cssUrlRewriting,cssImport,bomStripper,semicolonAppender,sassCss</param-value>
    </init-param>
    <init-param>
      <param-name>postProcessors</param-name>
      <param-value>cssVariables,cssMinJawr,jsMin</param-value>
    </init-param>
  </filter>
```

Notice the *sassCss* in param-value tag of preProcessors. Adding this value to the list will add in the chain of preprocessors the *SassCssProcessor* which does the magic.
Another alternative to configure the filter, is by extending ExtensionsConfigurableWroManagerFactory, setting inside the implementation the processors to use and their order and using your implementation as a value for *managerFactoryClassName* init-param.

More details about this can be found here: [ConfigurableWroManagerFactory](ConfigurableWroManagerFactory).