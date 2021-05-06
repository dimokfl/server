package ru.geekbrain.mavenjavafxserver;

import java.sql.*;

public class DataBaseClients {

    /*
    Вроде все работает, но есть косяк который не пойму как исправить.
    При закрытии клиентсого приложения не обновляется клиентский лист (тех кто онлайн), остается прежним.
    И вообще как я понял поток не останавливается, почему так? Как это исправить?


     */

    private static final String DATABASE_NAME = "//localhost:5432/geekbrains-chat";
    private static final String LOGIN_DATA = "postgres";
    private static final String PASS_DATA = "Cnheyf555";
    private Connection postgresConnection;

    public void start() {
        try {
            this.connect(DATABASE_NAME, LOGIN_DATA, PASS_DATA);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect(String database, String logdata, String passdata) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        String url = "jdbc:postgresql:" + database;

        this.postgresConnection = DriverManager.getConnection(url, logdata, passdata);

    }

    public String getNickByLogin(String login) {
        try {
            PreparedStatement ps = postgresConnection.prepareStatement("SELECT username FROM clients WHERE login = ? LIMIT 1");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stop() {
        disconnect();
    }

    private void disconnect() {
        try {
            this.postgresConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}