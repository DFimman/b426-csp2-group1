package com.joysistivi.clothingretailapp.config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class DbConnection {

    public final static String URL = "jdbc:mysql://localhost:3306/clothing_retail_db";
    public final static String USERNAME = "root";
    public final static String PASSWORD = "";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
