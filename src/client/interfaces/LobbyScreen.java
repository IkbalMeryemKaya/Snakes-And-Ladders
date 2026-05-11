/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client.interfaces;

import client.GameClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class LobbyScreen {

    // Colors - same theme as StartScreen
    static final Color COLOR_BG        = new Color(255, 248, 240);
    static final Color COLOR_PRIMARY    = new Color(255, 107, 53);
    static final Color COLOR_AVAILABLE  = new Color(6, 214, 160);   // green
    static final Color COLOR_INGAME     = new Color(150, 150, 150); // gray
    static final Color COLOR_PENDING    = new Color(255, 193, 7);   // yellow

    StartScreen startScreen;
    GameClient gameClient;
    JPanel lobbyPanel;

    // Components
    JLabel lbl_welcome;
    JPanel pnl_playerList;  // player rows will be added here
    JLabel lbl_playerCount;

    public LobbyScreen(StartScreen startScreen) {
        this.startScreen = startScreen;
        initComponents();
    }

    private void initComponents() {
        lobbyPanel = new JPanel(new BorderLayout(0, 0));
        lobbyPanel.setBackground(COLOR_BG);

        // --- TOP PANEL: title ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(COLOR_PRIMARY);
        topPanel.setPreferredSize(new Dimension(0, 70));

        JLabel lbl_title = new JLabel("Lobby", SwingConstants.CENTER);
        lbl_title.setFont(new Font("Arial", Font.BOLD, 22));
        lbl_title.setForeground(Color.WHITE);

        lbl_welcome = new JLabel("", SwingConstants.CENTER);
        lbl_welcome.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_welcome.setForeground(new Color(255, 220, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        topPanel.add(lbl_title, gbc);
        gbc.gridy = 1;
        topPanel.add(lbl_welcome, gbc);

        lobbyPanel.add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: player list ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_BG);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        lbl_playerCount = new JLabel("Online players:");
        lbl_playerCount.setFont(new Font("Arial", Font.BOLD, 14));
        lbl_playerCount.setForeground(new Color(80, 60, 50));
        lbl_playerCount.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerPanel.add(lbl_playerCount, BorderLayout.NORTH);

        // Panel where player rows will be added
        pnl_playerList = new JPanel();
        pnl_playerList.setLayout(new BoxLayout(pnl_playerList, BoxLayout.Y_AXIS));
        pnl_playerList.setBackground(COLOR_BG);

        JScrollPane scrollPane = new JScrollPane(pnl_playerList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 180), 1));
        scrollPane.setBackground(COLOR_BG);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        lobbyPanel.add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: exit button ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        JButton btn_exit = new JButton("Exit");
        btn_exit.setFont(new Font("Arial", Font.PLAIN, 13));
        btn_exit.setForeground(new Color(150, 100, 80));
        btn_exit.setFocusPainted(false);
        btn_exit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_exit.addActionListener(e -> {
            if (gameClient != null) gameClient.disconnect();
            System.exit(0);
        });
        bottomPanel.add(btn_exit);

        lobbyPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    // Called when a LOBBY_UPDATE message is received from the server
    // parts: ["LOBBY_UPDATE", "alice:AVAILABLE", "bob:IN_GAME", ...]
    public void updatePlayerList(String[] parts) {
        this.gameClient = startScreen.getGameClient();
        String myUsername = gameClient != null ? gameClient.username : "";

        // Update welcome text
        lbl_welcome.setText("Welcome, " + myUsername);

        // Clear and rebuild the player list
        pnl_playerList.removeAll();

        int onlineCount = 0;
        for (int i = 1; i < parts.length; i++) {
            String[] info = parts[i].split(":");
            if (info.length < 2) continue;

            String uname  = info[0];
            String status = info[1];
            onlineCount++;

            // Do not show the current player's own row
            if (uname.equals(myUsername)) continue;

            pnl_playerList.add(createPlayerRow(uname, status));
            pnl_playerList.add(javax.swing.Box.createVerticalStrut(6));
        }

        lbl_playerCount.setText("Online players (" + onlineCount + "):");

        // Show a message if no other players are online
        if (pnl_playerList.getComponentCount() == 0) {
            JLabel lbl_empty = new JLabel("No other players online, waiting...",
                    SwingConstants.CENTER);
            lbl_empty.setFont(new Font("Arial", Font.ITALIC, 13));
            lbl_empty.setForeground(new Color(180, 160, 140));
            lbl_empty.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            pnl_playerList.add(javax.swing.Box.createVerticalStrut(20));
            pnl_playerList.add(lbl_empty);
        }

        pnl_playerList.revalidate();
        pnl_playerList.repaint();
    }

    // Create a row panel for a single player
    private JPanel createPlayerRow(String username, String status) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 200, 180), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Left: player name
        JLabel lbl_name = new JLabel(username);
        lbl_name.setFont(new Font("Arial", Font.BOLD, 14));
        lbl_name.setForeground(new Color(60, 40, 30));
        row.add(lbl_name, BorderLayout.WEST);

        // Center: status label
        JLabel lbl_status = new JLabel(getStatusText(status));
        lbl_status.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl_status.setForeground(getStatusColor(status));
        lbl_status.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(lbl_status, BorderLayout.CENTER);

        // Right: play button (only enabled if AVAILABLE)
        JButton btn_play = new JButton("Play");
        btn_play.setFont(new Font("Arial", Font.BOLD, 12));
        btn_play.setBackground(COLOR_PRIMARY);
        btn_play.setForeground(Color.WHITE);
        btn_play.setFocusPainted(false);
        btn_play.setBorderPainted(false);
        btn_play.setPreferredSize(new Dimension(70, 30));
        btn_play.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_play.setEnabled(status.equals("AVAILABLE"));

        btn_play.addActionListener(e -> {
            if (gameClient != null) {
                gameClient.sendMessage("INVITE|" + username);
            }
        });

        row.add(btn_play, BorderLayout.EAST);
        return row;
    }

    // Show an invite dialog when an invitation is received
    public void showInviteDialog(String fromUsername) {
        int result = JOptionPane.showConfirmDialog(
            lobbyPanel,
            fromUsername + " is inviting you to a game!\nDo you accept?",
            "Game Invitation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (gameClient == null) return;

        if (result == JOptionPane.YES_OPTION) {
            gameClient.sendMessage("INVITE_RESPONSE|ACCEPT");
        } else {
            gameClient.sendMessage("INVITE_RESPONSE|REJECT");
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "AVAILABLE": return "Available";
            case "IN_GAME":   return "In Game";
            case "PENDING":   return "Pending";
            default:          return status;
        }
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "AVAILABLE": return COLOR_AVAILABLE;
            case "IN_GAME":   return COLOR_INGAME;
            case "PENDING":   return COLOR_PENDING;
            default:          return Color.GRAY;
        }
    }

    public JPanel getPanel() {
        return lobbyPanel;
    }

}