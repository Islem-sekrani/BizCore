package com.gestion.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton utility class for managing MySQL database connections
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bizcore";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get a connection to the database
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie!");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL introuvable: " + e.getMessage());
                throw new SQLException("Driver MySQL introuvable", e);
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    /**
     * Test the database connection
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
}
