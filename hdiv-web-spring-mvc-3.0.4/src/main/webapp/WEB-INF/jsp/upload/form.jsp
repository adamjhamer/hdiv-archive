<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>File upload sample</h2>
<form:form method="post" enctype="multipart/form-data">
  <table>
    <tr>
      <th>
        <input type="file" name="file"/>
      </th>
    </tr>
    <tr>
      <td>
         <p class="submit"><input type="submit" value="Upload"/></p>
      </td>
    </tr>
  </table>
</form:form>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
