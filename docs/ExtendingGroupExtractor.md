# Introduction

Sometimes, you might run into cases where your group names might follow a pattern that take the current locale into consideration (to be able to resolve locale specific css into your groups). A typical issue that you might run into in these situations might be a request to a group that does not exist in your wro model (i.e., wro.xml) -- which results in 404 errors. In such cases, you will need to extend the DefaultGroupExtractor class to provide custom behavior to parse and return the group name. 

# Example 
Consider the following example.

If you request for a group a/b/c/d/some_group_name_es_us.css, but you have a group "some_group_name_es" but not "some_group_name_es_us", you would still want to be able to send back the css content for the next best group that makes sense in that particular case (Similar to how java property files are retrieved). The following steps explain how you can do that.

### Extend the BaseWroManagerFactory class and override the newGroupExtractor method to return your custom group extractor 

```java
@Override
public final GroupExtractor newGroupExtractor() {
  return new MyCustomGroupExtractor();
}
```

### Configure the filter with init param to specify your custom manager

```xml
<filter>
  <filter-name>WebResourceOptimizer</filter-name>
  <filter-class>ro.isdc.wro.http.WroFilter</filter-class>
  <init-param>
    <param-name>managerFactoryClassName</param-name>
    <param-value>path.to.my.custom.extended.manager</param-value>
  </init-param>
</filter>
```


Within the custom group extractor that extends the DefaultGroupExtractor, override the getGroupName method to do your custom group name parsing logic. 

You might need to add the following in your custom group extractor to be able to know if the group in question exists in the model. If not, you can proceed to look for the next best group name in the model 

```java
@Inject
private WroModelFactory modelFactory;
```

you will need to call the create() method on the model 

```java
WroModel wroModel = this.getModelFactory().create();
```

and 

```java
wroModel.getGroupByName("some_group_name_es_us")
```

to see if a group is returned or if a InvalidGroupNameException is thrown. 