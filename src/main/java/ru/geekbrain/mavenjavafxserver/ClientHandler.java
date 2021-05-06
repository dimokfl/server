package ru.geekbrain.mavenjavafxserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) { // Цикл авторизации
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        // login Bob
                        String usernameFromLogin = msg.split("\\s")[1];
                        String usernameFromSQL = server.getDataBase().getNickByLogin(usernameFromLogin);

                        if (server.isUserOnline(usernameFromSQL)) {
                            sendMessage("/login_failed Данный никнэйм уже занят");
                            continue;
                        }
                        if (usernameFromSQL == null) {
                            sendMessage("/login_failed Такого пользователя не существует");
                            continue;
                        }

                        username = usernameFromSQL;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                }

                while (true) { // Цикл общения с клиентом
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }
                    if (msg.startsWith("/exit")) {
                        break;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private void executeCommand(String cmd) {
        // /w Bob Hello, Bob!!!
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s", 3);
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        if (cmd.startsWith("/who_am_i")) {
            sendMessage("Вы:" + username);
            return;
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();

        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
                server.getDataBase().stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}