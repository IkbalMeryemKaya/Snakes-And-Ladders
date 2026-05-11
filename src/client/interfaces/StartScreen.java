/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client.interfaces;

import client.GameClient;
import client.interfaces.LobbyScreen;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class StartScreen extends JFrame {

    // Colors - cartoon style
    static final Color COLOR_BG        = new Color(255, 248, 240);
    static final Color COLOR_PRIMARY    = new Color(255, 107, 53);  // orange
    static final Color COLOR_BTN_TEXT   = Color.WHITE;
    static final Color COLOR_FIELD_BG   = Color.WHITE;

    CardLayout cardLayout;
    JPanel mainPanel;
    public GameClient gameClient;
    public LobbyScreen lobbyScreen;

    // Start panel components
    JTextField txt_username;
    JButton btn_connect;
    JLabel lbl_status;

    public StartScreen() {
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Snakes & Ladders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null); // open in the center of the screen
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BG);
        add(mainPanel);
    }

    private void initComponents() {
        // --- START PANEL ---
        JPanel startPanel = new JPanel(new GridBagLayout());
        startPanel.setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel lbl_logo = new JLabel();
        lbl_logo.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            File logoFile = new File("assets/images/logo.png");
            if (!logoFile.exists()) {
                logoFile = new File(System.getProperty("user.dir")
                        + File.separator + "assets/images/logo1.png");
            }
            Image logoImg = ImageIO.read(logoFile);
            // Set width to 300px, calculate height proportionally
            int logoW = 300;
            int logoH = (int) ((double) logoImg.getHeight(null)
                    / logoImg.getWidth(null) * logoW);
            lbl_logo.setIcon(new ImageIcon(
                    logoImg.getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH)));
        } catch (Exception ex) {
            // Fallback to text title if logo cannot be loaded
            lbl_logo.setText("Snakes & Ladders");
            lbl_logo.setFont(new Font("Arial", Font.BOLD, 34));
            lbl_logo.setForeground(COLOR_PRIMARY);
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 20, 5, 20);
        startPanel.add(lbl_logo, gbc);

        // Subtitle
        JLabel lbl_sub = new JLabel("2-Player Network Game", SwingConstants.CENTER);
        lbl_sub.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl_sub.setForeground(new Color(150, 120, 100));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 30, 20);
        startPanel.add(lbl_sub, gbc);

        // Username label
        JLabel lbl_username = new JLabel("Username:");
        lbl_username.setFont(new Font("Arial", Font.PLAIN, 15));
        lbl_username.setForeground(new Color(80, 60, 50));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 40, 4, 10);
        startPanel.add(lbl_username, gbc);

        // Username input field
        txt_username = new JTextField();
        txt_username.setFont(new Font("Arial", Font.PLAIN, 15));
        txt_username.setPreferredSize(new Dimension(200, 38));
        txt_username.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 180), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 40, 16, 40);
        startPanel.add(txt_username, gbc);

        // Connect button
        btn_connect = new JButton("Connect");
        btn_connect.setFont(new Font("Arial", Font.BOLD, 16));
        btn_connect.setBackground(COLOR_PRIMARY);
        btn_connect.setForeground(COLOR_BTN_TEXT);
        btn_connect.setFocusPainted(false);
        btn_connect.setBorderPainted(false);
        btn_connect.setPreferredSize(new Dimension(200, 46));
        btn_connect.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 40, 12, 40);
        startPanel.add(btn_connect, gbc);

        // Status label (for errors or "Connecting..." message)
        lbl_status = new JLabel("", SwingConstants.CENTER);
        lbl_status.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_status.setForeground(new Color(200, 80, 60));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 20, 20, 20);
        startPanel.add(lbl_status, gbc);

        // Server info label
        JLabel lbl_server = new JLabel("Server: " + GameClient.SERVER_IP + ":" + GameClient.SERVER_PORT,
                SwingConstants.CENTER);
        lbl_server.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl_server.setForeground(new Color(180, 160, 140));
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 20, 20, 20);
        startPanel.add(lbl_server, gbc);

        // Button action
        btn_connect.addActionListener(e -> connectToServer());

        // Also connect on Enter key
        txt_username.addActionListener(e -> connectToServer());

        // Add start panel
        mainPanel.add(startPanel, "START");

        // Add lobby panel (LobbyScreen will populate it)
        lobbyScreen = new LobbyScreen(this);
        mainPanel.add(lobbyScreen.getPanel(), "LOBBY");
    }

    private void connectToServer() {
        String username = txt_username.getText().trim();

        if (username.isEmpty()) {
            lbl_status.setText("Please enter a username!");
            return;
        }

        if (username.length() < 3) {
            lbl_status.setText("The username must be at least 3 characters long!");
            return;
        }

        lbl_status.setText("Connecting...");
        btn_connect.setEnabled(false);

        // Connect in a separate thread to avoid freezing the UI
        new Thread(() -> {
            try {
                gameClient = new GameClient(username, StartScreen.this);
                // Successfully connected, switch to lobby
                javax.swing.SwingUtilities.invokeLater(() -> {
                    setTitle("Snakes & Ladders - " + username);
                    cardLayout.show(mainPanel, "LOBBY");
                });
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    lbl_status.setText("Connection error: the server may be down");
                    btn_connect.setEnabled(true);
                });
            }
        }).start();
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public void showLobby() {
        cardLayout.show(mainPanel, "LOBBY");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true);
        });
    }

}