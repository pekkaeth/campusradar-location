package com.campusradar.location;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location/map")
public class LocationMapServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/location/devlogin");
            return;
        }
        req.setAttribute("categories", LocationDAO.CATEGORIES);
        req.getRequestDispatcher("/WEB-INF/views/location/map.jsp").forward(req, res);
    }
}