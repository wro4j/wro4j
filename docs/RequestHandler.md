# Introduction
RequestHandler is a new concept available since 1.4.7 version. It is very similar to !HttpServlet. The only difference is that it has a slightly different interface and it can access all the wro4j related details, like: model, processors, configuration, etc. 

The purpose of RequestHandler is to process a HTTP request. A typical usage  is to provide an API functionality for wro4j runtime solution (WroFilter). RequestHandler's are applied before the processing in the !WroFilter, and only one handler can be applied. If one handler is applied, other handlers are skipped and processing is not performed.

# Available RequestHandler's 
By default wro4j provide several !RequestHandlers:

  * **ReloadCacheRequestHandler** - responsible for handling ```/wro/wroAPI/reloadCache``` request, which will trigger the clear cache operation. Thus, any cached content will be removed and any subsequent call for some resource will require a new processing cycle. This handler is available only in debug (DEVELOPMENT) mode.
  * **ReloadModelRequestHandler** - responsible for handling ```/wro/wroAPI/reloadModel``` request, which will trigger the destroy model operation. Thus, any subsequent call will ask !WroModelFactory to create the model again. This RequestHandler is also availale only in debug mode.
  * **ResourceProxyResourceHandler** - responsible for handling request for any particular resource referenced by the model or a css through !WroFilter. The idea is make it possible wro4j to stream the content for resources which are not accessible by any other means. An example is an image referenced by a css resource located in the classpath. This kind of image cannot be accessed by default. This RequestHandler will act as a proxy for streaming the content of the image from its original location. This RequestHandler is always enabled.
  * **ModelAsJsonRequestHandler** - responsible for handling ```/wro/wroAPI/model``` request. The result is a JSON representation of the model built by **WroModelFactory** together with any other modification performed by ModelTransformer's applied during processing. This RequestHandler is available only in debug mode and only when wro4j-extensions module is added as dependency to the classpath. It can be useful to visualise the internal representation of the model.
  

# Details 
Any RequestHandler should implement the interface with the same name. This is how the interface looks like:

```java
public interface RequestHandler {
  
  /**
   * Handle the given request, generating a response.
   * 
   * @param request
   *          current HTTP request
   * @param response
   *          current HTTP response
   * @throws IOException
   *           in case of I/O errors
   */
  void handle(HttpServletRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * Determines if current request can be handled by this requestHandler
   * @param request current HTTP request
   * @return true if this requestHandler should handle this request
   */
  boolean accept(HttpServletRequest request);

  /**
   * Used to determine if the RequestHandler is enabled
   */
  boolean isEnabled();
}
```

## Creating custom RequestHandler
In order to create a custom RequestHandler you have to implement RequestHandler interface. There is also a default implementation **RequestHandlerSupport** which implements all the methods, leaving you the freedom to implement only the methods you are interested in.

When creating a new RequestHandler, you should be aware that once a handler accepts the request and it is enabled, !WroFilter won't process anything else and won't chain that request further. So, it acts as a filter inside filter.

A custom RequestHandler implementation can easily access any wro4j related informations, like locatorFactory, processorFactory, modelFactory, configuration, etc. 

Acessing any of these is possible using **@Inject** annotation, example:

```java
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private WroConfiguration config;
  @Inject
  private UriLocatorFactory locatorFactory;
```

The **ModelAsJsonRequestHandler** uses the modelFactory to get the model representation and serialize it into JSON which is sent to the response.

## Adding Custom RequestHandler to WroFilter
The next step after creating a new RequestHandler implementation is to add it to instruct the filter to use it. In order to do that, you'll have to extend !WroFilter class and invoke the following method:

```java
  public void setRequestHandlerFactory(final RequestHandlerFactory requestHandlerFactory) {}
```

The filter expects a !RequestHandlerFactory which is an interface responsible for creating a collection of RequestHandler objects. There is an implementation of this interface available: **SimpleRequestHandlerFactory**, which holds a list of hanlders. And adding a handler is as simple as: 

```java
  RequestHandlerFactory handlerFactory = new SimpleRequestHandlerFactory().addHandler(new CustomRequestHandler());
```

If you would like to reuse existing handlers and just add you custom RequestHandler on top of them, use: 
```java
  RequestHandlerFactory handlerFactory = new DefaultRequestHandlerFactory().addHandler(new CustomRequestHandler());
```

**DefaultRequestHandlerFactory** is a class which extends **SimpleRequestHandlerFactory** and adds all available handlers.