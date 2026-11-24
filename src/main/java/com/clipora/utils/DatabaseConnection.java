package com.clipora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static Connection getConnection() throws SQLException {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new SQLException("Set DB_URL, DB_USER and DB_PASSWORD before starting Clipora.");
        }

        // Load the driver explicitly when running inside Tomcat.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver is missing.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
