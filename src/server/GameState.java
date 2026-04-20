/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author Monster Huma H5 v4.1
 */
public class GameState {
      String player1;       // birinci oyuncunun username'i
    String player2;       // ikinci oyuncunun username'i
    int pos1;             // birinci oyuncunun pozisyonu (1-100)
    int pos2;             // ikinci oyuncunun pozisyonu (1-100)
    String currentPlayer; // simdi sira kimde
    boolean isGameOver;
    String winner;
 
    public GameState(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.pos1 = 0;  // baslangicta tahta disinda
        this.pos2 = 0;
        this.currentPlayer = player1; // ilk sira her zaman p1'de
        this.isGameOver = false;
        this.winner = null;
    }
 
    // Belirtilen oyuncunun pozisyonunu guncelle
    public void setPosition(String username, int newPos) {
        if (username.equals(player1)) {
            this.pos1 = newPos;
        } else {
            this.pos2 = newPos;
        }
    }
 
    // Belirtilen oyuncunun pozisyonunu getir
    public int getPosition(String username) {
        if (username.equals(player1)) {
            return pos1;
        } else {
            return pos2;
        }
    }
 
    // Sirayi diger oyuncuya gec
    public void switchTurn() {
        if (currentPlayer.equals(player1)) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
    }
 
    public boolean isCurrentPlayer(String username) {
        return currentPlayer.equals(username);
    }
 
    public void setGameOver(String winner) {
        this.isGameOver = true;
        this.winner = winner;
    }
 
}
