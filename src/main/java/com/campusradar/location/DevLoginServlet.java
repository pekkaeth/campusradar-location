package com.campusradar.location;

import com.campusradar.common.DBUtil;
import com.campusradar.common.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location/devlogin")
public class DevLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int id = 1;
        String p = req.getParameter("id");
        if (p != null) id = Integer.parseInt(p);
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name, email, role FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
                    req.getSession(true).setAttribute("user", u);
                    res.sendRedirect(req.getContextPath() + "/location/map");
                    return;
                }
            }
            res.getWriter().println("No user with that id.");
        } catch (Exception e) {
            res.getWriter().println("Error: " + e.getMessage());
        }
    }
}