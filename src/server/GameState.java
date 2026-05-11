/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class GameState {

    String player1;       // first player's username
    String player2;       // second player's username
    int pos1;             // first player's position (1-100)
    int pos2;             // second player's position (1-100)
    String currentPlayer; // whose turn it is
    boolean isGameOver;
    String winner;

    public GameState(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.pos1 = 0;  // starting off the board
        this.pos2 = 0;
        this.currentPlayer = player1; // first turn always belongs to p1
        this.isGameOver = false;
        this.winner = null;
    }

    // Update the position of the given player
    public void setPosition(String username, int newPos) {
        if (username.equals(player1)) {
            this.pos1 = newPos;
        } else {
            this.pos2 = newPos;
        }
    }

    // Get the position of the given player
    public int getPosition(String username) {
        if (username.equals(player1)) {
            return pos1;
        } else {
            return pos2;
        }
    }

    // Switch the turn to the other player
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