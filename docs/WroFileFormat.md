# WroModel as XML
Xml was the first DSL used to build WroModel and it is used by default by WroManager.

The xml specifies the groups of files that you wish to aggregate and minimize.  Its format is fairly simple, consisting of a list of groups.  Each group in turn contains a list of CSS and !JavaScript files that belong to that group.

Here is an example `wro.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<groups xmlns="http://www.isdc.ro/wro">
  <group name='core'>
    <css>/static/css/lib/global-whitespace-reset.css</css>
    <js>/static/js/lib/site.js</js>
  </group>
</groups>
```

## Resource

### Resource attribute

Each resource tag can have the following attributes:

| Attribute Name | Description | 
| minimize | (Optional) Boolean value indicating if the minimization should be applied on this resource. This value is true by default. Note: This is ONLY applicable to pre-processors! |


```xml
  <group name="g1">
    <css minimize="true">/path/to/style.css</css>
    <js minimize="false">/path/to/script.js</js>
  </group>
```
This attribute will inform wro4j to not apply minification on resources which have minimize flag set to false. By default this attribute is true.


The example above uses resource paths that are relative to the servlet context. There are three other ways that you can specify the path to a CSS or JavaScript resource; from the class path, with a file URL, or with an external URL.

#### Relative to servlet context
```xml
<css>/static/css/style.css</css>
<css>/static/css/style.css</css>
<css>/static/css/*.css</css>
<css>/static/css/*.cs?</css>
<css>/static/css/**</css>
```

Notice that the resource url starts with the */* character.


#### From classpath:
```xml
<js>classpath:file.js</js>
<css>classpath:ro/isdc/resources/file.css</css>
<js>classpath:com/mycompany/resources/*.js</js>
<js>classpath:com/mycompany/resources/**.js</js>
```

#### From a file URL:
```xml
<css>file:c:/temp/file.css</css>
<css>file:c:/temp/*.css</css>
<css>file:c:/temp/**</css>
```

#### From an external URL:
```xml
<css>http://www.site.com/static/style.css</css>
```

### Wildcard characters

Resource specifications can include wildcards.  The '{{{*}}}' wildcard will match any part of a single path element, while the '{{{**}}}' wildcard will match a series of directories and file names.  The '?' wildcard will match a single character.  Wild card characters do not work in external URLs, but they do work in file URLs.  [WildcardSupport More info on wildcard support.]

## Groups 

A group is responsible for "grouping" resources into logical units. Each group is identified by a name. 
A group can have the following attributes:

| Attribute Name | Description | 
| name | (Required) Unique (across the model) identifier of a group |
| abstract | (Optional) False by default. Available since 1.4.9. Similar to abstract bean in spring framework. An abstract group won't be visible as a group in the model. It exist solely for being referred by other groups |

## Group References 
A group can contain a reference to one or more other groups.  This works as an 'include', so that the referring group will also be aggregated with the contents of the referenced group. 

The group reference is defined with group-ref tag:

```xml
  <group name="g1">
     <group-ref>g2</group-ref>
  </group>
```

This is useful when you want group named g1 to have the same resource used by group named g2. Here is an example:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<groups xmlns="http://www.isdc.ro/wro">

  <group name='admin-site'>
    <css>/static/css/lib/admin.css</css>
    <js>/static/js/lib/admin.js</js>
    
    <group-ref>global</group-ref>
  </group>

  <group name='global'>
    <css>/static/css/lib/global-whitespace-reset.css</css>
    <css>/static/css/lib/tools.css</css>
    <js>/static/js/lib/core.js</js>
  </group>
</groups>
```


When using build-time solution, this will build the files `admin-site.css` and `admin-site.js`.  `admin-site.js` will contain the files `admin.css`, `global-whitespace-reset.css`, and `tools.css`.  `admin-site.js` will include the files `admin.js` & `core.js`.

## Import directive 
The wro model can be created by aggregation of several xml files. This can be done with import tag:

```xml
<import>classpath:com/path/to/another.xml</import>
```

As a result, the groups defined in another.xml and in wro.xml are combined. 

Currently import directive works only with classpath resources and absolute path locations (such as http://www.site.com/another.xml or file:/var/path/to/another.xml). Import of relative xml is not supported yet.