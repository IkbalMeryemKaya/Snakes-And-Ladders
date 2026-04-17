/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Monster Huma H5 v4.1
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class Server extends Thread {

    ServerSocket ssocket;
    boolean isRunning;
    ArrayList<ClientHandler> clientList;
    LobbyManager lobbyManager;

    public Server(int port) throws IOException {
        this.ssocket = new ServerSocket(port);
        this.isRunning = false;
        this.clientList = new ArrayList<>();
        this.lobbyManager = new LobbyManager(this);
    }

    public void startServer() {
        this.isRunning = true;
        this.start();
        System.out.println("Sunucu baslatildi. Port: " + ssocket.getLocalPort());
    }

    public void stopServer() throws IOException {
        this.isRunning = false;
        this.ssocket.close();
        System.out.println("Sunucu durduruldu.");
    }

    public void sendBroadcast(String msg) throws IOException {
        for (ClientHandler client : clientList) {
            client.sendMessage(msg);
        }
    }

    public void sendToClient(String username, String msg) {
        for (ClientHandler client : clientList) {
            if (username.equals(client.username)) {
                client.sendMessage(msg);
                return;
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clientList.remove(client);
        lobbyManager.removePlayer(client.username);
    }

    @Override
    public void run() {
        while (this.isRunning) {
            try {
                Socket csocket = ssocket.accept(); // blocking
                System.out.println("Yeni baglanti: " + csocket.getInetAddress());

                ClientHandler newClient = new ClientHandler(csocket, this);
                clientList.add(newClient);
                newClient.startListening();

            } catch (IOException ex) {
                if (this.isRunning) {
                    System.err.println("Baglanti hatasi: " + ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(5000);
            server.startServer();
        } catch (IOException ex) {
            System.err.println("Sunucu baslatma hatasi: " + ex.getMessage());
        }
    }

}