# Introduction =
!ServletContext locator is responsible for locating resources relative to servletContext. In other words, it can retrieve resources hosted by your web app or those generated dynamically by a servlet of your application (ex: DWR). 

## Details 
  * The !ServletContextUriLocator handles only resources which starts with "/" (Example: "/path/to/script.js"). These resources are considered to be hosted by the server where the application is running.
  * The servletContext relative resources can be resolved with two different strategies:
  --* DISPATCHER_FIRST - try to locate resource via the dispatcher stream locator. This will include dynamic resources produced by servlet's or JSP's.
  --* SERVLET_CONTEXT_FIRST - try to locate resource using servletContext.getResourceAsStream(uri).
  --* SERVLET_CONTEXT_ONLY - uses servlet context and will not use dispatcher strategy as a fallback when no resources is available.
  * By default !ServletContextUriLocator uses DISPATCHER_FIRST strategy, because this allows you to include also resources served by jsp or similar. 
  * The DISPATCHER_FIRST strategy performs the following steps to locate a resource:
  --* Uses !RequestDispatcher to get the resource stream ( request.getRequestDispatcher(location)).
  --* If there is no dispatcher available for that resource, create a HTTP request to get the resource.


### Difference between DISPATCHER_FIRST and SERVLET_CONTEXT_FIRST strategies
To understand what are the difference between those two strategies, the best approach is an example. 

Given a jsp file: 
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String color = "red";
%>
body {
  background: <%= color%>;
}
```

When the DISPATCHER_FIRST strategy is used, the located content is:
```
body {
  background: red;
}
```

When the SERVLET_CONTEXT_FIRST strategy is used, the located content the raw content of the jsp:
```
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String color = "red";
%>
body {
  background: <%= color%>;
}
```

The DISPATCHER_FIRST strategy is useful when you have resources which are generated dynamically, while the SERVLET_CONTEXT_FIRST when you want all your resources to be retrieved in its raw version. 

Note: it is important to mention that when the DISPATCHER_FIRST is used and there is no dynamic resource found, the locator will use the SERVLET_CONTEXT_FIRST strategy as a fallback. The fallback behavior works also when the SERVLET_CONTEXT_FIRST strategy is configured.