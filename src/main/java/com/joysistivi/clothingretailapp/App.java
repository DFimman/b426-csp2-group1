package com.joysistivi.clothingretailapp;

import com.joysistivi.clothingretailapp.config.DbConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {
    public static void main(String[] args) {
        DbConnection dbConnection = new DbConnection();

        try (Connection connection = dbConnection.connect();) {
            System.out.println("Connected Successfully!");
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
    }
}