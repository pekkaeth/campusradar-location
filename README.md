# CampusRadar: Campus Map

Module 4 of CampusRadar. An interactive map of campus places, with a category filter and a "Near me" sort.

## Features

- Leaflet map with OpenStreetMap tiles (no API key), one pin per campus place
- Category filter: Hostel, Academic, Sports, Food, Events, Landmark, Other
- "Near me" uses the browser's location and lists places nearest first, with distances
- Clicking a place in the list zooms the map to it
- Admins can add, edit and delete places

## How it works

- **Distance:** `LocationDAO.distanceKm` implements the Haversine formula (Earth radius 6371 km). `listNear` sorts the places by it.
- **JSON endpoint:** `LocationDataServlet` returns the places as JSON. An `esc()` method escapes quotes and backslashes so a place name cannot break the JSON. The page sets text with `textContent`, so a name cannot inject HTML.
- **Validation:** invalid latitude or longitude values in the URL are ignored, and the admin form rejects bad names, categories and coordinate ranges.
- **Admin only:** `LocationAdminServlet` checks for the role ADMIN on GET and POST. Everyone else gets HTTP 403.
- The place list is also used by the Live Feed's place dropdown.

## Files

```
src/main/java/com/campusradar/location/
  Place.java, LocationDAO.java
  LocationMapServlet.java, LocationDataServlet.java, LocationAdminServlet.java
  DevLoginServlet.java        (temporary, for testing this module alone)
src/main/webapp/WEB-INF/views/location/
  map.jsp, manage.jsp
```

`DBUtil.java` and `User.java` in the `common` package are shared with the other modules.

## Database

```sql
CREATE TABLE locations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(40),
  latitude DOUBLE,
  longitude DOUBLE
);
```

The project loads 21 campus places into this table with an `INSERT`. If MySQL Workbench shows error 1175 (safe-update mode) on a `DELETE`, include a key column in the `WHERE`, for example `WHERE id > 0 AND latitude IS NULL`.

## Run this module alone

```
$env:DB_URL  = "jdbc:mysql://HOST:PORT/campusradar?sslMode=REQUIRED"
$env:DB_USER = "your-db-user"
$env:DB_PASS = "your-db-password"
mvn package cargo:run
```

Open `http://localhost:8080/campusradar/location/devlogin?id=<admin id>`. The user must be an ADMIN to open the manage page. Database credentials are read from environment variables and must never be committed.

## Test checklist

- [ ] All places appear as pins and in the list
- [ ] The Sports filter shows only sports places
- [ ] "Near me" sorts by distance after you allow the location permission, and shows a clear message if you block it
- [ ] A student opening `/location/manage` gets 403
- [ ] As admin, add, edit and delete a place; latitude `200` is refused

## Integration

In the combined project, `DevLoginServlet` is removed and `AuthFilter` protects `/location/*`.

## Team and ownership

| Role | Person | Notes |
|---|---|---|
| Original development and integration | Himanshu (`pekkaeth`) | Repository owner |
| Module lead (study, testing, documentation, extension) | Bhuvnesh (`bhsharma-06`) | Collaborator once added |

**Module lead responsibilities:** understand the Haversine distance, the JSON endpoint and the admin-only place management; test every flow in the checklist above; keep this README accurate; build one extension.

| Contribution | Status | Commits |
|---|---|---|
| Test checklist completed and results recorded | Planned | |
| Extension: "within 200 m" filter using the existing distance method | Planned | |
