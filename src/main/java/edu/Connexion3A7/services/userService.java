package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.interfaces.IUserService;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;

public class userService implements IUserService {

    private Connection cnx;

    public userService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public user authenticate(String email, String password) throws SQLException {
        // Re-fetch connection in case it was null at construction time
        if (cnx == null || cnx.isClosed()) {
            cnx = MyConnection.getInstance().getCnx();
        }
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user u = new user();
            u.setId_user(rs.getInt("id_user"));
            u.setEmail(rs.getString("email"));
            u.setMdp(rs.getString("password"));
            u.setRole(rs.getString("role"));
            return u;
        }
        return null;
    }
}
