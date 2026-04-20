/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Monster Huma H5 v4.1
 */
public class GameRoom extends Thread{
    // Tahta: yilan baslari ve merdiven dipleri -> gidilecek kare
    // Hem yilanlar hem merdivenler tek map'te tutulur
    static final HashMap<Integer, Integer> BOARD = new HashMap<>();
    static {
        // Merdivenler (alt -> ust)
        BOARD.put(4,  17);
        BOARD.put(10, 29);
        BOARD.put(22, 43);
        BOARD.put(45, 58);
        BOARD.put(49, 72);
        BOARD.put(66, 87);
        BOARD.put(78, 97);
        // Yilanlar (bas -> kuyruk)
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
        System.out.println("Oyun basladi: " + player1.username + " vs " + player2.username);
 
        // Her iki oyuncuya oyun basladi mesaji gonder
        // GAME_START|rakip_username|FIRST veya SECOND
        player1.sendMessage("GAME_START|" + player2.username + "|FIRST");
        player2.sendMessage("GAME_START|" + player1.username + "|SECOND");
 
        // Baslangic durumunu gonder
        broadcastGameState(0);
    }
 
    // ClientHandler'dan gelen zar istegini isle
    // synchronized: iki oyuncu ayni anda mesaj gonderirse siralenir
    public synchronized void handleRollRequest(String username) {
        if (state.isGameOver) return;
 
        // Sira bu oyuncuda degil mi?
        if (!state.isCurrentPlayer(username)) {
            ClientHandler requester = getHandler(username);
            if (requester != null) {
                requester.sendMessage("ERROR|Sira sende degil!");
            }
            return;
        }
 
        // Zar at (1-6)
        int diceValue = random.nextInt(6) + 1;
        System.out.println(username + " zar atti: " + diceValue);
 
        // Yeni pozisyonu hesapla
        int currentPos = state.getPosition(username);
        int newPos = currentPos + diceValue;
 
        // 100'e ulasti veya gecti -> kazandi
        if (newPos >= 100) {
            newPos = 100;
            state.setPosition(username, newPos);
            state.setGameOver(username);
            broadcastGameState(diceValue);
            endGame(username);
            return;
        }
 
        // Yilan veya merdiven var mi?
        if (BOARD.containsKey(newPos)) {
            int destination = BOARD.get(newPos);
            System.out.println(username + ": " + newPos + " -> " + destination +
                    (destination > newPos ? " (merdiven!)" : " (yilan!)"));
            newPos = destination;
        }
 
        // Pozisyonu guncelle ve sirayi gec
        state.setPosition(username, newPos);
        state.switchTurn();
 
        // Her iki oyuncuya guncel durumu gonder
        broadcastGameState(diceValue);
    }
 
    // Her iki oyuncuya guncel oyun durumunu gonder
    // Format: GAME_STATE|zar|pos1|pos2|siradakiOyuncu
    private void broadcastGameState(int diceValue) {
        String msg = "GAME_STATE|" + diceValue + "|" +
                state.pos1 + "|" + state.pos2 + "|" + state.currentPlayer;
        player1.sendMessage(msg);
        player2.sendMessage(msg);
    }
 
    private void endGame(String winner) {
        System.out.println("Oyun bitti! Kazanan: " + winner);
 
        // Her iki oyuncuya bitis mesaji gonder
        player1.sendMessage("GAME_OVER|" + winner);
        player2.sendMessage("GAME_OVER|" + winner);
 
        // Oyunculari lobiye geri al
        server.lobbyManager.gameEnded(player1.username, player2.username);
        player1.gameRoom = null;
        player2.gameRoom = null;
 
        this.isRunning = false;
    }
 
    private ClientHandler getHandler(String username) {
        if (player1.username.equals(username)) return player1;
        if (player2.username.equals(username)) return player2;
        return null;
    }
}
