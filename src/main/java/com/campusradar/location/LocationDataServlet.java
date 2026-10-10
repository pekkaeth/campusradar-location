package com.campusradar.location;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location/data")
public class LocationDataServlet extends HttpServlet {
    private final LocationDAO dao = new LocationDAO();

    // makes text safe inside a JSON string
    private String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append(' '); break;
                case '\r': sb.append(' '); break;
                case '\t': sb.append(' '); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        if (req.getSession().getAttribute("user") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().print("{\"ok\":false,\"message\":\"Please log in again.\"}");
            return;
        }
        try {
            String category = req.getParameter("category");
            List<Place> places;
            Double lat = parse(req.getParameter("lat"));
            Double lng = parse(req.getParameter("lng"));
            if (lat != null && lng != null && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                places = dao.listNear(category, lat, lng);
            } else {
                places = dao.list(category);
            }
            StringBuilder sb = new StringBuilder("{\"ok\":true,\"places\":[");
            for (int i = 0; i < places.size(); i++) {
                Place p = places.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(p.getId())
                  .append(",\"name\":\"").append(esc(p.getName())).append("\"")
                  .append(",\"category\":\"").append(esc(p.getCategory())).append("\"")
                  .append(",\"lat\":").append(p.getLatitude())
                  .append(",\"lng\":").append(p.getLongitude())
                  .append(",\"km\":");
                if (p.getDistanceKm() < 0) sb.append("null");
                else sb.append(Math.round(p.getDistanceKm() * 1000) / 1000.0);
                sb.append("}");
            }
            sb.append("]}");
            res.getWriter().print(sb.toString());
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().print("{\"ok\":false,\"message\":\"Could not load the places.\"}");
        }
    }

    private Double parse(String s) {
        try {
            return (s == null || s.isEmpty()) ? null : Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}