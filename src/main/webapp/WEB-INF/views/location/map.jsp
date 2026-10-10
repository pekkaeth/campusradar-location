<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/common/header.jspf" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">
<style>
  #map { height: 480px; border-radius: .5rem; }
  #placeList { max-height: 480px; overflow-y: auto; }
  @media (max-width: 767px) { #map { height: 340px; } #placeList { max-height: 300px; } }
</style>

<div class="container mt-4">
  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
    <h3 class="mb-0">📍 Campus map</h3>
    <div class="d-flex gap-2">
      <button id="nearBtn" class="btn btn-primary">📡 Near me</button>
      <c:if test="${sessionScope.user.role == 'ADMIN'}">
        <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/location/manage">Manage places</a>
      </c:if>
    </div>
  </div>

  <div id="notice" class="alert d-none" role="alert"></div>

  <div class="d-flex flex-wrap gap-2 mb-3" id="filters">
    <button class="btn btn-sm btn-dark" data-cat="All">All</button>
    <c:forEach items="${categories}" var="cat">
      <button class="btn btn-sm btn-outline-dark" data-cat="${cat}"><c:out value="${cat}"/></button>
    </c:forEach>
  </div>

  <div class="row g-3">
    <div class="col-md-8"><div id="map"></div></div>
    <div class="col-md-4">
      <div class="card shadow-sm">
        <div class="card-header" id="listTitle">Places</div>
        <div class="list-group list-group-flush" id="placeList"></div>
      </div>
    </div>
  </div>
  <p class="small text-muted mt-2">Distances are straight-line (as the crow flies). Map data &copy; OpenStreetMap contributors.</p>
</div>

<script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"></script>
<script>
  var base = '${pageContext.request.contextPath}';
  var category = 'All';
  var myPos = null;
  var meMarker = null;
  var markers = {};
  var notice = document.getElementById('notice');
  var listBox = document.getElementById('placeList');

  var map = L.map('map').setView([27.0295, 75.8933], 17);
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);
  var layer = L.layerGroup().addTo(map);

  function showNotice(ok, text) {
    notice.className = 'alert ' + (ok ? 'alert-success' : 'alert-warning');
    notice.textContent = text;
    setTimeout(function () { notice.className = 'alert d-none'; }, 5000);
  }

  function fmtDist(km) {
    if (km === null || km === undefined) return '';
    return km < 1 ? Math.round(km * 1000) + ' m away' : km.toFixed(2) + ' km away';
  }

  // text is always set with textContent, so a place name can never inject HTML
  function render(places, fit) {
    layer.clearLayers();
    markers = {};
    listBox.innerHTML = '';
    document.getElementById('listTitle').textContent =
      (myPos ? 'Nearest first' : 'Places') + ' (' + places.length + ')';
    var bounds = [];

    places.forEach(function (p) {
      var pop = document.createElement('div');
      var nm = document.createElement('strong');
      nm.textContent = p.name;
      pop.appendChild(nm);
      pop.appendChild(document.createElement('br'));
      pop.appendChild(document.createTextNode(p.category + (p.km !== null ? ' · ' + fmtDist(p.km) : '')));

      var m = L.marker([p.lat, p.lng]).bindPopup(pop);
      layer.addLayer(m);
      markers[p.id] = m;
      bounds.push([p.lat, p.lng]);

      var item = document.createElement('button');
      item.type = 'button';
      item.className = 'list-group-item list-group-item-action';
      var t = document.createElement('div');
      t.className = 'fw-semibold';
      t.textContent = p.name;
      var s = document.createElement('small');
      s.className = 'text-muted';
      s.textContent = p.category + (p.km !== null ? ' · ' + fmtDist(p.km) : '');
      item.appendChild(t);
      item.appendChild(s);
      item.addEventListener('click', function () {
        map.setView([p.lat, p.lng], 18);
        m.openPopup();
      });
      listBox.appendChild(item);
    });

    if (!places.length) {
      var empty = document.createElement('div');
      empty.className = 'list-group-item text-muted';
      empty.textContent = 'No places in this category yet.';
      listBox.appendChild(empty);
    }
    if (fit && bounds.length) map.fitBounds(bounds, { padding: [30, 30], maxZoom: 18 });
  }

  function load(fit) {
    var url = base + '/location/data?category=' + encodeURIComponent(category);
    if (myPos) url += '&lat=' + myPos.lat + '&lng=' + myPos.lng;
    fetch(url, { credentials: 'same-origin' })
      .then(function (r) {
        if (r.status === 401) { location.reload(); return null; }
        return r.json();
      })
      .then(function (d) {
        if (!d) return;
        if (!d.ok) { showNotice(false, d.message); return; }
        render(d.places, fit);
      })
      .catch(function () { showNotice(false, 'Could not load the places. Please try again.'); });
  }

  document.getElementById('filters').addEventListener('click', function (e) {
    var b = e.target.closest('button[data-cat]');
    if (!b) return;
    category = b.dataset.cat;
    document.querySelectorAll('#filters button').forEach(function (x) {
      x.className = 'btn btn-sm ' + (x === b ? 'btn-dark' : 'btn-outline-dark');
    });
    load(true);
  });

  document.getElementById('nearBtn').addEventListener('click', function () {
    if (!navigator.geolocation) {
      showNotice(false, 'Your browser cannot share its location.');
      return;
    }
    var btn = this;
    btn.disabled = true;
    navigator.geolocation.getCurrentPosition(function (pos) {
      btn.disabled = false;
      myPos = { lat: pos.coords.latitude, lng: pos.coords.longitude };
      if (meMarker) map.removeLayer(meMarker);
      meMarker = L.circleMarker([myPos.lat, myPos.lng], { radius: 9, color: '#0d6efd', fillOpacity: 0.8 })
        .addTo(map).bindPopup('You are here');
      showNotice(true, 'Sorted by distance from you.');
      load(false);
    }, function (err) {
      btn.disabled = false;
      showNotice(false, err.code === 1
        ? 'Location permission was blocked. Allow it in your browser settings to use Near me.'
        : 'Could not find your location. Try again outdoors or check your connection.');
    }, { enableHighAccuracy: true, timeout: 10000 });
  });

  load(true);
</script>
<%@ include file="/WEB-INF/common/footer.jspf" %>