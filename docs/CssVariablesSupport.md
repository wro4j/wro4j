---
title: Css Variables
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: "This page describe the css variables feature and how it can be used with wro4j. The css custom syntax required for css variables was inspired from: http://disruptive-innovations.com/zoo/cssvariables/"
---


The css variables support is achieved by the processor named CssVariablesProcessor . As its name states, this processor works only with CSS resources and it does the following: 

  * parse the css and search for variables declaration (@variables token) and variable usage.  
  * replace all declared variables with declared variable values.
  * remove variable declaration, making the result a valid css. 

The processor can be used as both: pre processor & post processor. 

  * When used as a pre processor, the scope of the declared variables is reduced to a single css file. 
  * When used as a post processor, the scope of the declared variables is the 'group' level (all css resources from the processed group).

With this feature you can write a css like this:

```css
@variables {
  mainBackground: red;
  mainColor: #fff;
  mainBorder: solid black 2px; 
  contentHeight: 100px;
  alt1-background: yellow; 
  alt2-background: #de5500;
}
#variablesHolder {
  background: var(mainBackground);
  height: var(contentHeight);
  border: var(mainBorder);
}
#variablesHolder em {
  background: var(alt1-background);
  border: var(mainBorder);
}
#variablesHolder span {
  background: var(alt2-background);
  border: var(mainBorder);
  color: var(mainColor);
}
```

which will be processed and will produce the following result:

```css
#variablesHolder {
  background:red;
  border:2px solid black;
  height:100px;
}
#variablesHolder em {
  background:yellow;
  border:2px solid black;
}
#variablesHolder span {
  background:#DE5500;
  border:2px solid black;
  color:#FFFFFF;
}
```