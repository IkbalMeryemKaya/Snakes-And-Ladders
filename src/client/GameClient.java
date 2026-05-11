/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import client.interfaces.GameScreen;
import client.interfaces.StartScreen;
import client.interfaces.LobbyScreen;
import javax.swing.JOptionPane;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class GameClient extends Thread {

    public static final String SERVER_IP = "51.21.250.119"; // Replace with AWS IP
    public static final int SERVER_PORT = 1234;

    Socket csocket;
    InputStream cinput;
    OutputStream coutput;
    boolean isConnected;
    public String username;
    StartScreen startScreen; // reference for UI updates

    public GameClient(String username, StartScreen startScreen) throws IOException {
        this.username = username;
        this.startScreen = startScreen;
        this.csocket = new Socket(SERVER_IP, SERVER_PORT);
        this.cinput = csocket.getInputStream();
        this.coutput = csocket.getOutputStream();
        this.isConnected = true;

        this.start();                       // start listening thread
        sendMessage("LOGIN|" + username);   // log in
    }

    public void sendMessage(String msg) {
        try {
            byte[] data = (msg + "\n").getBytes();
            coutput.write(data.length);
            coutput.write(data);
        } catch (IOException ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (this.isConnected) {
            try {
                int bsize = cinput.read();
                if (bsize == -1) {
                    break;
                }
                byte[] buffer = new byte[bsize];
                cinput.read(buffer);
                String message = new String(buffer).trim();
                System.out.println("[Server] --> " + message);
                processMessage(message);
            } catch (IOException ex) {
                this.isConnected = false;
                System.out.println("Connection to server lost.");
            }
        }
        // Close StartScreen when connection is fully lost
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (startScreen != null) {
                startScreen.dispose();
            }
        });
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        // All Swing UI updates must be done on the EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            switch (type) {
                case "LOBBY_UPDATE":
                    // parts[1..n] -> "username:STATUS" format
                    if (startScreen.lobbyScreen != null) {
                        startScreen.lobbyScreen.updatePlayerList(parts);
                    }
                    break;
                case "INVITE_REQUEST":
                    // parts[1] -> username of the sender
                    if (startScreen.lobbyScreen != null) {
                        startScreen.lobbyScreen.showInviteDialog(parts[1]);
                    }
                    break;
                case "GAME_START":
                    // parts[1] -> opponent, parts[2] -> FIRST/SECOND
                    System.out.println("GAME_START received: " + message);
                    openGameScreen(parts[1], parts[2]);
                    break;
                case "GAME_STATE":
                    // parts[1]=dice, parts[2]=pos1, parts[3]=pos2, parts[4]=currentPlayer
                    if (gameScreen != null) {
                        gameScreen.updateBoard(
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]),
                                parts[4]
                        );
                    }
                    break;
                case "GAME_OVER":
                    // parts[1] -> winner's username
                    if (gameScreen != null) {
                        gameScreen.pendingGameOver = parts[1];
                    }
                    break;
                case "ERROR":
                    if (gameScreen != null) {
                        // Game screen is open - handle as connection lost
                        gameScreen.showConnectionLost(parts[1]);
                    } else {
                        JOptionPane.showMessageDialog(null, parts[1], "Info",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                default:
                    System.out.println("Unknown message type: " + type);
            }
        });
    }

    // Open the game screen
    GameScreen gameScreen;

    private void openGameScreen(String opponent, String order) {
        gameScreen = new GameScreen(this, username, opponent, order);
        gameScreen.setVisible(true);
    }

    public void disconnect() {
        sendMessage("DISCONNECT");
        this.isConnected = false;
        try {
            csocket.close();
        } catch (IOException ex) {
        }
    }

}