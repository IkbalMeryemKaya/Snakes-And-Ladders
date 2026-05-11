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
import javax.swing.SwingConstants;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class EndScreen extends JFrame {

    static final Color COLOR_BG      = new Color(255, 248, 240);
    static final Color COLOR_PRIMARY  = new Color(255, 107, 53);
    static final Color COLOR_WIN      = new Color(6, 214, 160);   // winner color
    static final Color COLOR_LOSE     = new Color(17, 138, 178);  // loser color

    GameClient gameClient;
    String myUsername;
    String winner;
    int turnCount;

    public EndScreen(GameClient gameClient, String myUsername,
            String winner, int turnCount) {
        this.gameClient  = gameClient;
        this.myUsername  = myUsername;
        this.winner      = winner;
        this.turnCount   = turnCount;

        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Snakes & Ladders - Game Over");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        boolean iWon = winner.equals(myUsername);

        // --- TOP PANEL: result header ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(iWon ? COLOR_WIN : COLOR_LOSE);
        topPanel.setPreferredSize(new Dimension(0, 140));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 20, 4, 20);

        // Win/lose icon image
        JLabel lbl_icon = new JLabel();
        lbl_icon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            String imgName = iWon ? "win.png" : "fail.png";
            File f = new File("assets/images/" + imgName);
            if (!f.exists()) {
                f = new File(System.getProperty("user.dir")
                        + File.separator + "assets/images/" + imgName);
            }
            Image img = ImageIO.read(f);
            lbl_icon.setIcon(new ImageIcon(
                    img.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        } catch (Exception ex) {
            // Fallback to text if image cannot be loaded
            lbl_icon.setText(iWon ? "★" : "✗");
            lbl_icon.setFont(new Font("Arial", Font.BOLD, 52));
            lbl_icon.setForeground(Color.WHITE);
        }
        gbc.gridy = 0;
        topPanel.add(lbl_icon, gbc);

        // Result message
        JLabel lbl_result = new JLabel(
                iWon ? "Congratulations!" : "Good try!",
                SwingConstants.CENTER
        );
        lbl_result.setFont(new Font("Cooper Black", Font.PLAIN, 26));
        lbl_result.setForeground(Color.WHITE);
        gbc.gridy = 1;
        topPanel.add(lbl_result, gbc);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: game info ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(COLOR_BG);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(24, 40, 16, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(6, 0, 6, 0);

        // Winner label
        JLabel lbl_winner = new JLabel(
                (iWon ? "You Won!" : winner + " Won"),
                SwingConstants.CENTER
        );
        lbl_winner.setFont(new Font("Arial", Font.BOLD, 30));
        lbl_winner.setForeground(iWon ? COLOR_WIN : COLOR_LOSE);
        c.gridx = 0;
        c.gridy = 0;
        centerPanel.add(lbl_winner, c);

        // Divider line
        JPanel divider = new JPanel();
        divider.setBackground(new Color(220, 200, 180));
        divider.setPreferredSize(new Dimension(0, 1));
        c.gridy = 1;
        c.insets = new Insets(12, 0, 12, 0);
        centerPanel.add(divider, c);

        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: buttons ---
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 24, 40));
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.HORIZONTAL;
        b.weightx = 1;
        b.insets = new Insets(6, 0, 6, 0);

        // Return to lobby button
        JButton btn_lobby = new JButton("Play Again");
        btn_lobby.setFont(new Font("Arial", Font.BOLD, 15));
        btn_lobby.setBackground(COLOR_PRIMARY);
        btn_lobby.setForeground(Color.WHITE);
        btn_lobby.setFocusPainted(false);
        btn_lobby.setBorderPainted(false);
        btn_lobby.setPreferredSize(new Dimension(0, 46));
        btn_lobby.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_lobby.addActionListener(e -> {
            dispose(); // Close EndScreen, StartScreen remains open showing the lobby
        });
        b.gridx = 0;
        b.gridy = 0;
        bottomPanel.add(btn_lobby, b);

        // Exit button
        JButton btn_exit = new JButton("Exit");
        btn_exit.setFont(new Font("Arial", Font.PLAIN, 14));
        btn_exit.setForeground(new Color(150, 100, 80));
        btn_exit.setFocusPainted(false);
        btn_exit.setPreferredSize(new Dimension(0, 40));
        btn_exit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_exit.addActionListener(e -> {
            if (gameClient != null) {
                gameClient.disconnect();
            }
            System.exit(0);
        });
        b.gridy = 1;
        bottomPanel.add(btn_exit, b);

        add(bottomPanel, BorderLayout.SOUTH);
    }

}