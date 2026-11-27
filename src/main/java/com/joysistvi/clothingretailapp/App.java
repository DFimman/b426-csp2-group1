package com.joysistvi.clothingretailapp;

import com.joysistvi.clothingretailapp.config.DbConnection;

import java.sql.Connection;

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