package com.campusradar.location;

import com.campusradar.common.User;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location/manage")
public class LocationAdminServlet extends HttpServlet {
    private final LocationDAO dao = new LocationDAO();

    // returns true if the request may continue; otherwise it has already answered
    private boolean allowed(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/location/devlogin");
            return false;
        }
        if (!"ADMIN".equals(user.getRole())) {
            res.sendError(403, "Only admins can manage campus places.");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!allowed(req, res)) return;
        try {
            req.setAttribute("places", dao.list("All"));
        } catch (Exception e) {
            req.setAttribute("error", "Could not load places: " + e.getMessage());
        }
        req.setAttribute("categories", LocationDAO.CATEGORIES);
        req.getRequestDispatcher("/WEB-INF/views/location/manage.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (!allowed(req, res)) return;
        String msg;
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                dao.delete(Integer.parseInt(req.getParameter("id")));
                msg = "Place deleted.";
            } else {
                String name = req.getParameter("name");
                String category = req.getParameter("category");
                double lat = Double.parseDouble(req.getParameter("latitude"));
                double lng = Double.parseDouble(req.getParameter("longitude"));
                if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
                    msg = "The name must be 2 to 100 characters.";
                } else if (category == null || !LocationDAO.CATEGORIES.contains(category)) {
                    msg = "Choose a category.";
                } else if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                    msg = "Latitude must be between -90 and 90, and longitude between -180 and 180.";
                } else if ("update".equals(action)) {
                    dao.update(Integer.parseInt(req.getParameter("id")), name.trim(), category, lat, lng);
                    msg = "Place updated.";
                } else {
                    dao.add(name.trim(), category, lat, lng);
                    msg = "Place added.";
                }
            }
        } catch (NumberFormatException e) {
            msg = "Please enter valid numbers for the coordinates.";
        } catch (Exception e) {
            msg = "Something went wrong: " + e.getMessage();
        }
        res.sendRedirect(req.getContextPath() + "/location/manage?msg=" + URLEncoder.encode(msg, "UTF-8"));
    }
}