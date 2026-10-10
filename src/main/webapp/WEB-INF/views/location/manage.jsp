<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/common/header.jspf" %>
<div class="container mt-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">⚙️ Manage campus places</h3>
    <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/location/map">&larr; Back to map</a>
  </div>

  <c:if test="${not empty param.msg}"><div class="alert alert-info"><c:out value="${param.msg}"/></div></c:if>
  <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

  <div class="card shadow-sm mb-4">
    <div class="card-header">Add a place</div>
    <div class="card-body">
      <form method="post" action="${pageContext.request.contextPath}/location/manage" class="row g-2">
        <input type="hidden" name="action" value="add">
        <div class="col-md-4"><input type="text" name="name" class="form-control" placeholder="Name" minlength="2" maxlength="100" required></div>
        <div class="col-md-2">
          <select name="category" class="form-select">
            <c:forEach items="${categories}" var="cat"><option><c:out value="${cat}"/></option></c:forEach>
          </select></div>
        <div class="col-md-2"><input type="number" step="any" name="latitude" class="form-control" placeholder="Latitude" required></div>
        <div class="col-md-2"><input type="number" step="any" name="longitude" class="form-control" placeholder="Longitude" required></div>
        <div class="col-md-2"><button class="btn btn-success w-100">Add</button></div>
      </form>
      <p class="small text-muted mt-2 mb-0">Tip: in Google Maps, right-click a spot and click the two numbers to copy its latitude and longitude.</p>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-header">All places (${places.size()})</div>
    <div class="table-responsive">
      <table class="table align-middle mb-0">
        <thead><tr><th>Name</th><th>Category</th><th>Latitude</th><th>Longitude</th><th></th></tr></thead>
        <tbody>
          <c:forEach items="${places}" var="p">
            <tr>
              <form method="post" action="${pageContext.request.contextPath}/location/manage"></form>
              <td colspan="5">
                  <input type="hidden" name="action" value="update">
                  <input type="hidden" name="id" value="${p.id}">
                  <div class="col-md-3"><input type="text" name="name" class="form-control form-control-sm" value="<c:out value='${p.name}'/>" minlength="2" maxlength="100" required></div>
                  <div class="col-md-2">
                    <select name="category" class="form-select form-select-sm">
                      <c:forEach items="${categories}" var="cat"><option ${cat == p.category ? 'selected' : ''}><c:out value="${cat}"/></option></c:forEach>
                    </select></div>
                  <div class="col-md-2"><input type="number" step="any" name="latitude" class="form-control form-control-sm" value="${p.latitude}" required></div>
                  <div class="col-md-2"><input type="number" step="any" name="longitude" class="form-control form-control-sm" value="${p.longitude}" required></div>
                  <div class="col-md-3 d-flex gap-2">
                    <button class="btn btn-sm btn-outline-primary">Save</button>
                    <button class="btn btn-sm btn-outline-danger" type="submit" name="action" value="delete"
                            onclick="return confirm('Delete this place?');" formnovalidate>Delete</button>
                  </div>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/common/footer.jspf" %>