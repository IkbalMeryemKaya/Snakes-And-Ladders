/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.util.HashMap;

/**
 *
 * @author Monster Huma H5 v4.1
 */

public class LobbyManager {
 
    // Oyuncu durumlarini tutan sabitler
    public static final String AVAILABLE = "AVAILABLE";
    public static final String PENDING   = "PENDING";
    public static final String IN_GAME   = "IN_GAME";
 
    Server server;
    HashMap<String, ClientHandler> players;   // username -> handler
    HashMap<String, String> playerStatus;     // username -> durum
    HashMap<String, String> pendingInvites;   // davet bekleyen -> daveti gonderen
 
    public LobbyManager(Server server) {
        this.server = server;
        this.players = new HashMap<>();
        this.playerStatus = new HashMap<>();
        this.pendingInvites = new HashMap<>();
    }
 
    // Yeni oyuncu giris yapinca cagrilir
    public synchronized void addPlayer(String username, ClientHandler handler) {
        players.put(username, handler);
        playerStatus.put(username, AVAILABLE);
        System.out.println(username + " lobiye katildi.");
        broadcastLobbyUpdate();
    }
 
    // Oyuncu ayrilinca cagrilir
    public synchronized void removePlayer(String username) {
        if (username == null) return;
 
        // Bu oyuncuya gelen bekleyen davet varsa iptal et
        if (pendingInvites.containsKey(username)) {
            String inviter = pendingInvites.get(username);
            setStatus(inviter, AVAILABLE);
            pendingInvites.remove(username);
        }
 
        players.remove(username);
        playerStatus.remove(username);
        System.out.println(username + " lobiden ayrildi.");
        broadcastLobbyUpdate();
    }
 
    // Davet gonderme - race condition burda onleniyor
    public synchronized boolean invitePlayer(String fromUsername, String toUsername) {
        String toStatus = playerStatus.get(toUsername);
 
        // Hedef mevcut ve musait degil mi?
        if (toStatus == null || !toStatus.equals(AVAILABLE)) {
            ClientHandler from = players.get(fromUsername);
            if (from != null) {
                from.sendMessage("ERROR|Oyuncu musait degil: " + toUsername);
            }
            return false;
        }
 
        // Her iki oyuncuyu da PENDING yap
        setStatus(toUsername, PENDING);
        setStatus(fromUsername, PENDING);
        pendingInvites.put(toUsername, fromUsername); // kim davet etti
 
        // Hedefe davet mesaji gonder
        ClientHandler toHandler = players.get(toUsername);
        if (toHandler != null) {
            toHandler.sendMessage("INVITE_REQUEST|" + fromUsername);
        }
 
        broadcastLobbyUpdate();
        return true;
    }
 
    // Davet kabul edildi
    public synchronized void acceptInvite(String toUsername) {
        String fromUsername = pendingInvites.get(toUsername);
        if (fromUsername == null) return;
 
        // Her ikisini de IN_GAME yap
        setStatus(toUsername, IN_GAME);
        setStatus(fromUsername, IN_GAME);
        pendingInvites.remove(toUsername);
 
        ClientHandler p1 = players.get(fromUsername);
        ClientHandler p2 = players.get(toUsername);
 
        if (p1 != null && p2 != null) {
            // GameRoom olustur ve baslat
            GameRoom room = new GameRoom(p1, p2, server);
            p1.gameRoom = room;
            p2.gameRoom = room;
            room.startGame();
        }
 
        broadcastLobbyUpdate();
    }
 
    // Davet reddedildi
    public synchronized void rejectInvite(String toUsername) {
        String fromUsername = pendingInvites.get(toUsername);
        if (fromUsername == null) return;
 
        // Her ikisini de tekrar AVAILABLE yap
        setStatus(toUsername, AVAILABLE);
        setStatus(fromUsername, AVAILABLE);
        pendingInvites.remove(toUsername);
 
        // Daveti gonderene bildir
        ClientHandler from = players.get(fromUsername);
        if (from != null) {
            from.sendMessage("ERROR|" + toUsername + " daveti reddetti.");
        }
 
        broadcastLobbyUpdate();
    }
 
    // Oyun bitince oyunculari tekrar AVAILABLE yap
    public synchronized void gameEnded(String username1, String username2) {
        setStatus(username1, AVAILABLE);
        setStatus(username2, AVAILABLE);
        broadcastLobbyUpdate();
    }
 
    // Tum lobiye guncel oyuncu listesini gonder
    // Ornek: "LOBBY_UPDATE|ali:AVAILABLE|veli:IN_GAME"
    private void broadcastLobbyUpdate() {
        StringBuilder msg = new StringBuilder("LOBBY_UPDATE");
        for (String uname : players.keySet()) {
            msg.append("|").append(uname).append(":").append(playerStatus.get(uname));
        }
        for (ClientHandler handler : players.values()) {
            handler.sendMessage(msg.toString());
        }
    }
 
    private void setStatus(String username, String status) {
        if (playerStatus.containsKey(username)) {
            playerStatus.put(username, status);
        }
    }
 
    public String getStatus(String username) {
        return playerStatus.getOrDefault(username, null);
    }
 
}
