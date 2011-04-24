<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h1>Hotel Results</h1>
<p>
	<c:url value="/spring/hotels/search" var="url">
		<c:param name="searchString" value="${searchCriteria.searchString}"/>
		<c:param name="pageSize" value="${searchCriteria.pageSize}"/>
	</c:url>
	<a id="changeSearchLink" href="${url}">Change Search</a>
	<%-- 
	<a id="changeSearchLink" href="hotels/search?searchString=${searchCriteria.searchString}&pageSize=${searchCriteria.pageSize}">Change Search</a>
	--%>
	<script type="text/javascript">
		Spring.addDecoration(new Spring.AjaxEventDecoration({
			elementId: "changeSearchLink",
			event: "onclick",
			popup: true,
			params: {fragments: "searchForm"}		
		}));
	</script>
</p>
<div id="hotelResults">
<c:if test="${not empty hotelList}">
	<table class="summary">
		<thead>
			<tr>
				<th>Name</th>
				<th>Address</th>
				<th>City, State</th>
				<th>Zip</th>
				<th>Action</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="hotel" items="${hotelList}">
				<tr>
					<td>${hotel.name}</td>
					<td>${hotel.address}</td>
					<td>${hotel.city}, ${hotel.state}, ${hotel.country}</td>
					<td>${hotel.zip}</td>
					<td>
						<spring:url var="hotelsUrl" value="/spring/hotels/{id}">
							<spring:param name="id" value="${hotel.id}"/>
						</spring:url>
						<a href="${hotelsUrl}">View Hotel</a>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty hotelList}">
				<tr>
					<td colspan="5">No hotels found</td>
				</tr>
			</c:if>
		</tbody>
	</table>
	<div class="buttonGroup">
		<div class="span-3">
			<c:if test="${searchCriteria.page > 0}">
			
				<c:url value="/spring/hotels" var="urls">
					<c:param name="searchString" value="${searchCriteria.searchString}"/>
					<c:param name="pageSize" value="${searchCriteria.pageSize}"/>
					<c:param name="page" value="${searchCriteria.page - 1}"/>
				</c:url>
				<a id="prevResultsLink" href="${urls}">Previous Results</a>
				<%-- 
				<a id="prevResultsLink" href="hotels?searchString=${searchCriteria.searchString}&pageSize=${searchCriteria.pageSize}&page=${searchCriteria.page - 1}">Previous Results</a>
				--%>
				<script type="text/javascript">
					Spring.addDecoration(new Spring.AjaxEventDecoration({
						elementId: "prevResultsLink",
						event: "onclick",
						params: {fragments: "body"}
					}));
				</script>
			</c:if>
		</div>
		<div class="span-3 append-12 last">
			<c:if test="${not empty hotelList && fn:length(hotelList) == searchCriteria.pageSize}">
			
				<c:url value="/spring/hotels" var="urld">
					<c:param name="searchString" value="${searchCriteria.searchString}"/>
					<c:param name="pageSize" value="${searchCriteria.pageSize}"/>
					<c:param name="page" value="${searchCriteria.page + 1}"/>
				</c:url>
				<a id="moreResultsLink" href="${urld}">More Results</a>
				<%-- 
				<a id="moreResultsLink" href="hotels?searchString=${searchCriteria.searchString}&pageSize=${searchCriteria.pageSize}&page=${searchCriteria.page + 1}">More Results</a>
				--%>
				<script type="text/javascript">
					Spring.addDecoration(new Spring.AjaxEventDecoration({
						elementId: "moreResultsLink",
						event: "onclick",
						params: {fragments: "body"}		
					}));
				</script>
			</c:if>		
		</div>
	</div>
</c:if>
</div>	

