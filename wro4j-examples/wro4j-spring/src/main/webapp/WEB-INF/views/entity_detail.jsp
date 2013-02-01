<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page session="false" %>

<tags:template>

	<jsp:attribute name="breadcrumb"><a href="..">Home</a> / <a href="./new">MyEntity</a> / ${entity.name}</jsp:attribute>
	<jsp:body>
		<form:form commandName="entity">
		<table>
			<tr>
				<td>Name:</td><td><form:input size="40" path="name"/></td>
			</tr>
		</table>
		<p/>
		<input type="submit" value="Save"/>
		</form:form>
	</jsp:body>
	
</tags:template>
