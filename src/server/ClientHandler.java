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
 * @author İkbal Meryem Kaya
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
            System.err.println("Failed to send message: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (this.isListening) {
            try {
                int bsize = input.read(); // blocking
                if (bsize == -1) {
                    break;
                }
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
            case "GAME_ENDED":
                if (gameRoom != null) {
                    gameRoom = null;
                }
                server.lobbyManager.setPlayerAvailable(username);
                break;
            default:
                System.out.println("Unknown message type: " + type);
        }
    }

    private void handleLogin(String uname) {
        this.username = uname;
        server.lobbyManager.addPlayer(username, this);
        System.out.println(username + " logged in.");
    }

    private void handleInvite(String targetUsername) {
        server.lobbyManager.invitePlayer(username, targetUsername);
    }

    private void handleInviteResponse(String response) {
        if (response.equals("ACCEPT")) {
            server.lobbyManager.acceptInvite(username);
        } else {
            server.lobbyManager.rejectInvite(username);
        }
    }

    private void handleRollRequest() {
        if (gameRoom != null) {
            gameRoom.handleRollRequest(username);
        }
    }

    private void cleanup() {
        try {
            // Notify the opponent if a game is in progress
            if (gameRoom != null) {
                gameRoom.handlePlayerDisconnected(username);
            }
            server.removeClient(this);
            if (!socket.isClosed()) {
                socket.close();
            }
            System.out.println(username + " connection closed.");
        } catch (IOException ex) {
            System.err.println("Cleanup error: " + ex.getMessage());
        }
    }

}