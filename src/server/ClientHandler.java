/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Monster Huma H5 v4.1
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class ClientHandler extends Thread {

    Socket socket;
    InputStream input;
    OutputStream output;
    boolean isListening;
    String username;
    Server server;
    GameRoom gameRoom;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.isListening = false;
        this.username = null;
        this.gameRoom = null;
    }

    public void startListening() {
        this.isListening = true;
        this.start();
    }

    public void sendMessage(String msg) {
        try {
            byte[] data = (msg + "\n").getBytes();
            output.write(data.length);
            output.write(data);
        } catch (IOException ex) {
            System.err.println("Mesaj gonderilemedi: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (this.isListening) {
            try {
                int bsize = input.read(); // blocking
                if (bsize == -1) break;   // baglanti kapandi
                byte[] buffer = new byte[bsize];
                input.read(buffer);
                String message = new String(buffer).trim();
                System.out.println("[" + username + "] --> " + message);
                processMessage(message);

            } catch (IOException ex) {
                this.isListening = false;
            }
        }
        cleanup();
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        switch (type) {
            case "LOGIN":
                handleLogin(parts[1]);
                break;
            case "INVITE":
                handleInvite(parts[1]);
                break;
            case "INVITE_RESPONSE":
                handleInviteResponse(parts[1]);
                break;
            case "ROLL_REQUEST":
                handleRollRequest();
                break;
            case "DISCONNECT":
                this.isListening = false;
                break;
            default:
                System.out.println("Bilinmeyen mesaj: " + type);
        }
    }

    private void handleLogin(String uname) {
        this.username = uname;
        System.out.println(username + " giris yapti.");
        // TODO: LobbyManager'a kaydet, LOBBY_UPDATE broadcast et
    }

    private void handleInvite(String targetUsername) {
        System.out.println(username + " -> " + targetUsername + " davet gonderdi.");
        // TODO: LobbyManager uzerinden hedefe INVITE_REQUEST ilet
    }

    private void handleInviteResponse(String response) {
        System.out.println(username + " daveti " + response + " etti.");
        // TODO: ACCEPT ise GameRoom olustur, REJECT ise gondericiye bildir
    }

    private void handleRollRequest() {
        System.out.println(username + " zar atmak istiyor.");
        // TODO: GameRoom'a ilet
    }

    private void cleanup() {
        try {
            server.removeClient(this);
            if (!socket.isClosed()) socket.close();
            System.out.println(username + " baglantisi kapatildi.");
        } catch (IOException ex) {
            System.err.println("Kapatma hatasi: " + ex.getMessage());
        }
    }

}