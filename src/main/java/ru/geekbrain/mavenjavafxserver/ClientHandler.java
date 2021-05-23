package ru.geekbrain.mavenjavafxserver;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private String login;
    private final Logger LOG_CLIENT = LogManager.getLogger(ClientHandler.class.getName());


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
        server.getExecutorService().submit(() -> {
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
                        LOG_CLIENT.info("Клиент " + username + " подключился к серверу.");
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
                    LOG_CLIENT.info("Клиент " + username + " написал сообщение");
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG_CLIENT.error("Ошибка в цикле общения у клиента " + username);
                LOG_CLIENT.throwing(Level.ERROR, e);
            } finally {
                disconnect();
            }
        });
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
            LOG_CLIENT.error("Ошибка при отправке сообщения у клиента " + username);
            LOG_CLIENT.throwing(Level.ERROR, e);
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOG_CLIENT.error("Ошибка при закрытии соединения у клиента " + username);
                LOG_CLIENT.throwing(Level.ERROR, e);
            }
        }
    }
}











//        server.getExecutorService().shutdown();

//        new Thread(() -> {
//            try {
//                while (true) { // Цикл авторизации
//                    String msg = in.readUTF();
//                    if (msg.startsWith("/login ")) {
//                        // login Bob
//                        login = msg.split("\\s")[1];
//                        String usernameFromSQL = server.getDataBase().getNickByLogin(login);
//
//                        if (server.isUserOnline(usernameFromSQL)) {
//                            sendMessage("/login_failed Данный никнэйм уже занят");
//                            continue;
//                        }
//                        if (usernameFromSQL == null) {
//                            sendMessage("/login_failed Такого пользователя не существует");
//                            continue;
//                        }
//                        username = usernameFromSQL;
//                        sendMessage("/login_ok " + username);
//                        server.subscribe(this);
//                        break;
//                    }
//                }
//
//                while (true) { // Цикл общения с клиентом
//                    String msg = in.readUTF();
//                    if (msg.startsWith("/")) {
//                        if (msg.startsWith("/exit")) {
//                            break;
//                        }
//                        executeCommand(msg);
//                        System.out.println(this.username + "Написал сообщение: " + msg);
//                        continue;
//                    }
//                    server.broadcastMessage(username + ": " + msg);
//                    System.out.println("Сервер разослал сообщение " + username + " всем участникам");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                disconnect();
//            }
//        }).start();