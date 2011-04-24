<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<h1>${hotel.name}</h1>
<address>
	${hotel.address}
	<br />
	${hotel.city}, ${hotel.state}, ${hotel.zip}
	<br />
	${hotel.country}
</address>
<!--  
<form action="booking" method="get">
-->
	<p>
		Nightly Rate:
		<spring:bind path="hotel.price">${status.value}</spring:bind>
	</p>
	<!--  
	<input type="hidden" name="hotelId" value="${hotel.id}" />
	-->
	<div>
		<!-- 
		<button type="submit">Book Hotel</button>
		-->
		
		<c:url value="/spring/hotels/booking" var="url">
			<c:param name="hotelId" value="${hotel.id}"/>
		</c:url>
		<a class="button" href="${url}">Book Hotel</a>
	</div>
<!-- 
</form>
-->
