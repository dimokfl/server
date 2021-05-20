package ru.geekbrain.mavenjavafxserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private int port;
    private List<ClientHandler> clients;
    private DataBaseClients dataBase;
    private ExecutorService executorService;
    private final Logger LOG_SERVER = LogManager.getLogger(Server.class.getName());


    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        LOG_SERVER.info("Сервер подключился");
        executorService = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            dataBase = new DataBaseClients();
            dataBase.start();
            System.out.println("Сервер запущен на порту " + port);
            while (true) {
                System.out.println("Ждем нового клиента..");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
                LOG_SERVER.info("Подключился новый клиент.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG_SERVER.error("error message: " + e.getMessage());
            LOG_SERVER.fatal("fatal message: " + e.getMessage());
        } finally {
            dataBase.stop();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вошел в чат");
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вышел из чата");
        broadcastClientsList();
    }

    public synchronized void broadcastMessage(String message)  {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(receiverUsername)) {
                c.sendMessage("От: " + sender.getUsername() + " Сообщение: " + message);
                sender.sendMessage("Пользователю: " + receiverUsername + " Сообщение: " + message);
                return;
            }
        }
        sender.sendMessage("Невозможно отправить сообщение пользователю: " + receiverUsername + ". Такого пользователя нет в сети.");
    }

    public synchronized boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            stringBuilder.append(c.getUsername()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(clientsList);
        }
    }

    public synchronized void changeUsernameOfClient(String oldUsername, String newUsername, String login){
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(oldUsername)){
                String nu = getDataBase().changeUserName(c.getUsername(), newUsername, login);
                if (nu.equals(oldUsername)){
                    c.sendMessage("Имя " + newUsername + " уже занято. Выберите другое имя.");
                    continue;
                }
                c.setUsername(nu);
                broadcastMessage("Клиент " + oldUsername + " поменял ник на " + newUsername);
                broadcastClientsList();
            }
        }
    }

    public DataBaseClients getDataBase() {
        return dataBase;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}