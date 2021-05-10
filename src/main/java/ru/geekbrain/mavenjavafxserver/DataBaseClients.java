package ru.geekbrain.mavenjavafxserver;

import java.sql.*;
import java.util.ArrayList;

public class DataBaseClients {

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

    private ArrayList<String> getUsersList() {
        ArrayList<String> users = new ArrayList<>();
        try {
            ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT username FROM clients");
            while (rs.next()){
                users.add(rs.getString("username"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return users;
    }

    private boolean busyUserName(String name){
        ArrayList<String> users = new ArrayList<>(getUsersList());
        for (String user: users) {
            if (user.equals(name)){
                return true;
            }
        }
        return false;
    }

    public String changeUserName(String oldName, String newName, String login) {
        ArrayList<String> users = new ArrayList<>(getUsersList());
        if (!busyUserName(newName)){
            try {
                for (String user : users){
                    if (user.equals(oldName)) {
                        PreparedStatement ps = postgresConnection.prepareStatement("UPDATE clients SET username=? WHERE login=?");
                        ps.setString(1, newName);
                        ps.setString(2, login);
                        int rowsAffected = ps.executeUpdate();
                        System.out.println("Записей изменено: " + rowsAffected);
                    }
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            return newName;
        } else {
            return oldName;
        }
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