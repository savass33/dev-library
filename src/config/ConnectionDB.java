package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/devlibrary";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Sua senha MYSQL

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar ao banco: " + e.getMessage());
        }
    }
}