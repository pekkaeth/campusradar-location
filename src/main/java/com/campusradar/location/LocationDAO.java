package com.campusradar.location;

import com.campusradar.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LocationDAO {

    public static final List<String> CATEGORIES =
            Arrays.asList("Hostel", "Academic", "Sports", "Food", "Events", "Landmark", "Other");

    // Haversine formula: straight-line distance in km between two points on the Earth
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }

    private Place read(ResultSet rs) throws Exception {
        Place p = new Place();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setLatitude(rs.getDouble("latitude"));
        p.setLongitude(rs.getDouble("longitude"));
        return p;
    }

    // all places that have coordinates, optionally only one category
    public List<Place> list(String category) throws Exception {
        boolean filter = category != null && !category.isEmpty() && !category.equals("All");
        String sql = "SELECT id, name, category, latitude, longitude FROM locations "
                + "WHERE latitude IS NOT NULL AND longitude IS NOT NULL "
                + (filter ? "AND category = ? " : "")
                + "ORDER BY name";
        List<Place> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filter) ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(read(rs));
            }
        }
        return list;
    }

    // same list, but each place gets its distance from (lat, lng) and the nearest comes first
    public List<Place> listNear(String category, double lat, double lng) throws Exception {
        List<Place> list = list(category);
        for (Place p : list) {
            p.setDistanceKm(distanceKm(lat, lng, p.getLatitude(), p.getLongitude()));
        }
        list.sort(Comparator.comparingDouble(Place::getDistanceKm));
        return list;
    }

    public void add(String name, String category, double lat, double lng) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO locations (name, category, latitude, longitude) VALUES (?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, lat);
            ps.setDouble(4, lng);
            ps.executeUpdate();
        }
    }

    public void update(int id, String name, String category, double lat, double lng) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE locations SET name = ?, category = ?, latitude = ?, longitude = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, lat);
            ps.setDouble(4, lng);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM locations WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}