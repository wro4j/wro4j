<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%

  String wro4jParam = request.getParameter("wro4j-dev");
  boolean wro4jDev = wro4jParam != null && wro4jParam.equals("true");

%>
<!doctype html>
<html lang="en" ng-app="myApp">
<head>
  <meta charset="utf-8">
  <title>Wro4j OSGi Example</title>

  <% if(wro4jDev) {%>
  <link rel="stylesheet" href="/wro4j-osgi/wro/bootstrap.css"/>
  <link rel="stylesheet" href="/wro4j-osgi/wro/app.css"/>
  <%} else {%>
  <link rel="stylesheet" href="/wro4j-osgi/wro/thirdparty.css" type="text/css"/>
  <link rel="stylesheet" href="/wro4j-osgi/wro/application.css" type="text/css"/>
  <%}%>
</head>
<body>
<div ng-view>
  <p>Example Angular JS Application to demonstrate wro4j inside osgi using webjars and spring</p>
  <p>But, if you're reading this, something is broken (the angular js should replace this)</p>
</div>
<% if(wro4jDev) {%>
<script src="/wro4j-osgi/wro/jquery.js"></script>
<script src="/wro4j-osgi/wro/json2.js"></script>
<script src="/wro4j-osgi/wro/bootstrap.js"></script>
<script src="/wro4j-osgi/wro/angular.js"></script>

<script src="/wro4j-osgi/wro/controllers.js"></script>
<script src="/wro4j-osgi/wro/directives.js"></script>
<script src="/wro4j-osgi/wro/filters.js"></script>
<script src="/wro4j-osgi/wro/services.js"></script>
<script src="/wro4j-osgi/wro/app.js"></script>

<%} else {%>
<script src="/wro4j-osgi/wro/thirdparty.js"></script>
<script src="/wro4j-osgi/wro/application.js"></script>
<%}%>

</body>
</html>
