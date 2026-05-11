/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.util.HashMap;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class LobbyManager {

    // Constants for player status
    public static final String AVAILABLE = "AVAILABLE";
    public static final String PENDING = "PENDING";
    public static final String IN_GAME = "IN_GAME";

    Server server;
    HashMap<String, ClientHandler> players;  // username -> handler
    HashMap<String, String> playerStatus;    // username -> status
    HashMap<String, String> pendingInvites;  // invite target -> invite sender

    public LobbyManager(Server server) {
        this.server = server;
        this.players = new HashMap<>();
        this.playerStatus = new HashMap<>();
        this.pendingInvites = new HashMap<>();
    }

    // Called when a new player logs in
    public synchronized void addPlayer(String username, ClientHandler handler) {
        players.put(username, handler);
        playerStatus.put(username, AVAILABLE);
        System.out.println(username + " joined the lobby.");
        broadcastLobbyUpdate();
    }

    // Called when a player disconnects
    public synchronized void removePlayer(String username) {
        if (username == null) {
            return;
        }

        // Cancel any pending invite involving this player
        if (pendingInvites.containsKey(username)) {
            String inviter = pendingInvites.get(username);
            setStatus(inviter, AVAILABLE);
            pendingInvites.remove(username);
        }

        players.remove(username);
        playerStatus.remove(username);
        System.out.println(username + " left the lobby.");
        broadcastLobbyUpdate();
    }

    // Send an invite - race condition is prevented here with synchronized
    public synchronized boolean invitePlayer(String fromUsername, String toUsername) {
        String toStatus = playerStatus.get(toUsername);

        // Target does not exist or is not available
        if (toStatus == null || !toStatus.equals(AVAILABLE)) {
            ClientHandler from = players.get(fromUsername);
            if (from != null) {
                from.sendMessage("ERROR|Player is not available: " + toUsername);
            }
            return false;
        }

        // Set both players to PENDING
        setStatus(toUsername, PENDING);
        setStatus(fromUsername, PENDING);
        pendingInvites.put(toUsername, fromUsername); // track who sent the invite

        // Notify the target player
        ClientHandler toHandler = players.get(toUsername);
        if (toHandler != null) {
            toHandler.sendMessage("INVITE_REQUEST|" + fromUsername);
        }

        broadcastLobbyUpdate();
        return true;
    }

    // Called when an invite is accepted
    public synchronized void acceptInvite(String toUsername) {
        String fromUsername = pendingInvites.get(toUsername);
        if (fromUsername == null) {
            return;
        }

        // Set both players to IN_GAME
        setStatus(toUsername, IN_GAME);
        setStatus(fromUsername, IN_GAME);
        pendingInvites.remove(toUsername);

        ClientHandler p1 = players.get(fromUsername);
        ClientHandler p2 = players.get(toUsername);

        if (p1 != null && p2 != null) {
            // Create and start a new GameRoom
            GameRoom room = new GameRoom(p1, p2, server);
            p1.gameRoom = room;
            p2.gameRoom = room;
            room.startGame();
        }

        broadcastLobbyUpdate();
    }

    // Called when an invite is rejected
    public synchronized void rejectInvite(String toUsername) {
        String fromUsername = pendingInvites.get(toUsername);
        if (fromUsername == null) {
            return;
        }

        // Set both players back to AVAILABLE
        setStatus(toUsername, AVAILABLE);
        setStatus(fromUsername, AVAILABLE);
        pendingInvites.remove(toUsername);

        // Notify the sender
        ClientHandler from = players.get(fromUsername);
        if (from != null) {
            from.sendMessage("ERROR|" + toUsername + " rejected your invite.");
        }

        broadcastLobbyUpdate();
    }

    // Called when a game ends - set both players back to AVAILABLE
    public synchronized void gameEnded(String username1, String username2) {
        setStatus(username1, AVAILABLE);
        setStatus(username2, AVAILABLE);
        broadcastLobbyUpdate();
    }

    public synchronized void setPlayerAvailable(String username) {
        if (playerStatus.containsKey(username)) {
            setStatus(username, AVAILABLE);
            broadcastLobbyUpdate();
        }
    }

    // Broadcast the current player list to all lobby members
    // Example: "LOBBY_UPDATE|alice:AVAILABLE|bob:IN_GAME"
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
