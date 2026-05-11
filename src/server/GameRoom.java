/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class GameRoom extends Thread {

    // Board: snake heads and ladder bottoms -> destination square
    // Both snakes and ladders are stored in a single map
    static final HashMap<Integer, Integer> BOARD = new HashMap<>();

    static {
        // Ladders (bottom -> top)
        BOARD.put(4, 17);
        BOARD.put(10, 29);
        BOARD.put(22, 43);
        BOARD.put(45, 58);
        BOARD.put(49, 72);
        BOARD.put(66, 87);
        BOARD.put(78, 97);
        // Snakes (head -> tail)
        BOARD.put(36, 6);
        BOARD.put(56, 18);
        BOARD.put(68, 31);
        BOARD.put(81, 40);
        BOARD.put(84, 65);
    }

    ClientHandler player1;
    ClientHandler player2;
    Server server;
    GameState state;
    Random random;
    boolean isRunning;

    public GameRoom(ClientHandler player1, ClientHandler player2, Server server) {
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.state = new GameState(player1.username, player2.username);
        this.random = new Random();
        this.isRunning = false;
    }

    public void startGame() {
        this.isRunning = true;
        this.start();
    }

    @Override
    public void run() {
        System.out.println("Game started: " + player1.username + " vs " + player2.username);

        // Notify both players that the game has started
        // GAME_START|opponent_username|FIRST or SECOND
        player1.sendMessage("GAME_START|" + player2.username + "|FIRST");
        player2.sendMessage("GAME_START|" + player1.username + "|SECOND");

        // Send initial game state
        broadcastGameState(0);
    }

    // Handle a roll request from a ClientHandler
    // synchronized: ensures messages from both players are processed one at a time
    public synchronized void handleRollRequest(String username) {
        if (state.isGameOver) {
            return;
        }

        // Is it this player's turn?
        if (!state.isCurrentPlayer(username)) {
            ClientHandler requester = getHandler(username);
            if (requester != null) {
                requester.sendMessage("ERROR|It is not your turn!");
            }
            return;
        }

        // Roll the dice (1-6)
        int diceValue = random.nextInt(6) + 1;
        System.out.println(username + " rolled: " + diceValue);

        // Calculate new position
        int currentPos = state.getPosition(username);
        int newPos = currentPos + diceValue;

        // Reached or passed 100 -> wins
        if (newPos >= 100) {
            newPos = 100;
            state.setPosition(username, newPos);
            state.setGameOver(username);
            broadcastGameState(diceValue);
            endGame(username);
            return;
        }

        // Check for snake or ladder
        if (BOARD.containsKey(newPos)) {
            int destination = BOARD.get(newPos);
            System.out.println(username + ": " + newPos + " -> " + destination
                    + (destination > newPos ? " (ladder!)" : " (snake!)"));
            newPos = destination;
        }

        // Update position and switch turn
        state.setPosition(username, newPos);
        state.switchTurn();

        // Send updated state to both players
        broadcastGameState(diceValue);
    }

    // Send the current game state to both players
    // Format: GAME_STATE|dice|pos1|pos2|currentPlayer
    private void broadcastGameState(int diceValue) {
        String msg = "GAME_STATE|" + diceValue + "|" + state.pos1 + "|" + state.pos2 + "|" + state.currentPlayer;
        player1.sendMessage(msg);
        player2.sendMessage(msg);
    }

    private void endGame(String winner) {
        System.out.println("Game over! Winner: " + winner);

        // Notify both players
        player1.sendMessage("GAME_OVER|" + winner);
        player2.sendMessage("GAME_OVER|" + winner);

        // Return both players to the lobby
        server.lobbyManager.gameEnded(player1.username, player2.username);
        player1.gameRoom = null;
        player2.gameRoom = null;

        this.isRunning = false;
    }

    public synchronized void handlePlayerDisconnected(String username) {
        if (state.isGameOver) {
            return;
        }

        ClientHandler disconnected = getHandler(username);
        ClientHandler opponent = (disconnected == player1) ? player2 : player1;

        // Notify the opponent
        if (opponent != null) {
            opponent.sendMessage("ERROR|" + username + " disconnected. The game has ended.");
        }

        // End the game and return players to lobby
        state.setGameOver(opponent != null ? opponent.username : "");
        server.lobbyManager.gameEnded(player1.username, player2.username);
        player1.gameRoom = null;
        player2.gameRoom = null;
        this.isRunning = false;
    }

    private ClientHandler getHandler(String username) {
        if (player1.username.equals(username)) {
            return player1;
        }
        if (player2.username.equals(username)) {
            return player2;
        }
        return null;
    }

}