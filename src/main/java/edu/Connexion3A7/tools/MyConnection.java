package edu.Connexion3A7.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url = "jdbc:mysql://localhost:3306/bizcore";
    private String login = "root";
    private String pwd = "";
    public static MyConnection instance;
    private Connection cnx;

    private MyConnection() {
        connect();
    }

    private void connect() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion établie");
        } catch (SQLException e) {
            System.out.println("echec de connexion " + e.getMessage());
            cnx = null;
        }
    }

    public Connection getCnx() {
        // Auto-reconnect if connection is null or closed
        try {
            if (cnx == null || cnx.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return cnx;
    }

    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }
}
