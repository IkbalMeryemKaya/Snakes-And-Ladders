/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

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

public class GameClient extends Thread {

    public static final String SERVER_IP = "52.14.0.0"; // AWS IP buraya
    public static final int SERVER_PORT = 5000;

    Socket csocket;
    InputStream cinput;
    OutputStream coutput;
    boolean isConnected;
    String username;

    public GameClient(String username) throws IOException {
        this.username = username;
        this.csocket = new Socket(SERVER_IP, SERVER_PORT);
        this.cinput = csocket.getInputStream();
        this.coutput = csocket.getOutputStream();
        this.isConnected = true;

        this.start(); // dinlemeye basla
        sendMessage("LOGIN|" + username);
    }

    public void sendMessage(String msg) {
        try {
            byte[] data = (msg + "\n").getBytes();
            coutput.write(data.length);
            coutput.write(data);
        } catch (IOException ex) {
            System.err.println("Mesaj gonderilemedi: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (this.isConnected) {
            try {
                int bsize = cinput.read(); // blocking
                if (bsize == -1) break;
                byte[] buffer = new byte[bsize];
                cinput.read(buffer);
                String message = new String(buffer).trim();
                System.out.println("[Sunucu] --> " + message);
                processMessage(message);

            } catch (IOException ex) {
                this.isConnected = false;
                System.out.println("Sunucu baglantisi koptu.");
            }
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        switch (type) {
            case "LOBBY_UPDATE":
                // TODO: LobbyScreen'i guncelle
                break;
            case "INVITE_REQUEST":
                // TODO: Davet dialogu goster
                break;
            case "GAME_START":
                // TODO: GameScreen'e gec
                break;
            case "GAME_STATE":
                // TODO: Tahta guncelle, zar butonunu ayarla
                break;
            case "GAME_OVER":
                // TODO: EndScreen'e gec
                break;
            case "ERROR":
                // TODO: Hata mesaji goster
                System.out.println("Hata: " + parts[1]);
                break;
            default:
                System.out.println("Bilinmeyen mesaj: " + type);
        }
    }

    public void disconnect() {
        sendMessage("DISCONNECT");
        this.isConnected = false;
        try {
            csocket.close();
        } catch (IOException ex) {
            System.err.println("Kapatma hatasi: " + ex.getMessage());
        }
    }

}