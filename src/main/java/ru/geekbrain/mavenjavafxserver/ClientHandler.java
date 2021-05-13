package ru.geekbrain.mavenjavafxserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private String login;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLogin() {
        return login;
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
                        login = msg.split("\\s")[1];
                        String usernameFromSQL = server.getDataBase().getNickByLogin(login);

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
                        if (msg.startsWith("/exit")) {
                            break;
                        }
                        executeCommand(msg);
                        System.out.println(this.username + "Написал сообщение: " + msg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                    System.out.println("Сервер разослал сообщение " + username + " всем участникам");
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
        if (cmd.startsWith("/change_nike")) {
            String tokens = cmd.split("\\s")[1];
            server.changeUsernameOfClient(this.username, tokens, this.login);
            return;
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            System.out.println("Сообщение от " + username + " ушло.");
        } catch (IOException e) {
            disconnect();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}