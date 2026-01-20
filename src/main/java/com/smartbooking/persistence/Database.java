package com.smartbooking.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String url;

    public Database(String url) {
        this.url = url;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
