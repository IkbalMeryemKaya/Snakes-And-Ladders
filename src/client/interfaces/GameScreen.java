/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client.interfaces;

import client.GameClient;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author İkbal Meryem Kaya
 */
public class GameScreen extends JFrame {

    // Colors
    static final Color COLOR_BG         = new Color(255, 248, 240);
    static final Color COLOR_PRIMARY     = new Color(255, 107, 53);
    static final Color COLOR_CELL_LIGHT  = new Color(255, 241, 220);
    static final Color COLOR_CELL_DARK   = new Color(255, 209, 102);
    static final Color COLOR_GRID        = new Color(210, 180, 140);
    static final Color COLOR_P1          = new Color(255, 107, 53);  // orange
    static final Color COLOR_P2          = new Color(17, 138, 178);  // blue

    // Board dimensions
    static final int BOARD_SIZE  = 10;
    static final int CELL_SIZE   = 60;
    static final int BOARD_PIXEL = BOARD_SIZE * CELL_SIZE; // 600px

    // Snake and ladder positions (same as GameRoom.BOARD)
    static final HashMap<Integer, Integer> SNAKES  = new HashMap<>();
    static final HashMap<Integer, Integer> LADDERS = new HashMap<>();

    static {
        SNAKES.put(36, 6);
        SNAKES.put(56, 18);
        SNAKES.put(68, 31);
        SNAKES.put(81, 40);
        SNAKES.put(84, 65);

        LADDERS.put(4, 17);
        LADDERS.put(10, 29);
        LADDERS.put(22, 43);
        LADDERS.put(45, 58);
        LADDERS.put(49, 72);
        LADDERS.put(66, 87);
        LADDERS.put(78, 97);
    }

    GameClient gameClient;
    String myUsername;
    String opponentUsername;
    String myOrder; // FIRST or SECOND

    float animX1, animY1;  // P1's actual pixel position on screen
    float animX2, animY2;  // P2's actual pixel position on screen
    int displayPos1, displayPos2;
    boolean isAnimating = false;
    javax.swing.Timer animTimer;
    public String pendingGameOver = null;

    // Game state
    int pos1 = 0;  // FIRST player's position
    int pos2 = 0;  // SECOND player's position
    int lastDice = 0;

    // UI components
    BoardPanel boardPanel;
    JButton btn_rollDice;
    JLabel lbl_turnInfo;
    JLabel lbl_diceResult;
    JLabel lbl_myPos;
    JLabel lbl_opponentPos;
    DefaultListModel<String> logModel;
    JList<String> lst_log;

    // Images: "snake1" -> Image
    HashMap<String, Image> images = new HashMap<>();

    public GameScreen(GameClient gameClient, String myUsername,
            String opponentUsername, String order) {
        this.gameClient       = gameClient;
        this.myUsername       = myUsername;
        this.opponentUsername = opponentUsername;
        this.myOrder          = order;
        this.displayPos1      = 0;
        this.displayPos2      = 0;
        this.animX1           = -100;
        this.animY1           = -100; // start off-screen
        this.animX2           = -100;
        this.animY2           = -100;

        loadImages();
        initFrame();
        initComponents();

        // Dice button disabled until the game state is received
        btn_rollDice.setEnabled(false);
        lbl_turnInfo.setText("Game starting...");
    }

    private void initFrame() {
        setTitle("Snakes & Ladders - " + myUsername + " vs " + opponentUsername);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(860, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout(0, 0));

        // Disconnect when window is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                gameClient.disconnect();
                dispose();
            }
        });
    }

    private void initComponents() {
        // --- BOARD PANEL (left) ---
        boardPanel = new BoardPanel();
        boardPanel.setBackground(COLOR_BG);
        boardPanel.setPreferredSize(new Dimension(BOARD_PIXEL, BOARD_PIXEL));
        boardPanel.setBorder(BorderFactory.createLineBorder(COLOR_GRID, 2));
        add(boardPanel, BorderLayout.CENTER);
        

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(COLOR_BG);
        rightPanel.setPreferredSize(new Dimension(220, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);

        // Turn info label
        lbl_turnInfo = new JLabel("Turn: -", SwingConstants.CENTER);
        lbl_turnInfo.setFont(new Font("Arial", Font.BOLD, 15));
        lbl_turnInfo.setForeground(COLOR_PRIMARY);
        lbl_turnInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(lbl_turnInfo, gbc);

        // Dice result label
        lbl_diceResult = new JLabel("Dice: -", SwingConstants.CENTER);
        lbl_diceResult.setFont(new Font("Arial", Font.BOLD, 28));
        lbl_diceResult.setForeground(new Color(80, 60, 50));
        gbc.gridy = 1;
        gbc.insets = new Insets(12, 0, 4, 0);
        rightPanel.add(lbl_diceResult, gbc);

        // Roll dice button
        btn_rollDice = new JButton("Roll Dice");
        btn_rollDice.setFont(new Font("Arial", Font.BOLD, 16));
        btn_rollDice.setBackground(COLOR_PRIMARY);
        btn_rollDice.setForeground(Color.WHITE);
        btn_rollDice.setFocusPainted(false);
        btn_rollDice.setBorderPainted(false);
        btn_rollDice.setPreferredSize(new Dimension(180, 46));
        btn_rollDice.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_rollDice.setEnabled(false); // disabled at start
        btn_rollDice.addActionListener(e -> {
            gameClient.sendMessage("ROLL_REQUEST");
            btn_rollDice.setEnabled(false); // prevent double click
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 16, 0);
        rightPanel.add(btn_rollDice, gbc);

        // Position labels
        lbl_myPos = new JLabel(myUsername + ": Square 0", SwingConstants.LEFT);
        lbl_myPos.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_myPos.setForeground(COLOR_P1);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 2, 0);
        rightPanel.add(lbl_myPos, gbc);

        lbl_opponentPos = new JLabel(opponentUsername + ": Square 0", SwingConstants.LEFT);
        lbl_opponentPos.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_opponentPos.setForeground(COLOR_P2);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 0, 16, 0);
        rightPanel.add(lbl_opponentPos, gbc);

        // Move log title
        JLabel lbl_logTitle = new JLabel("Move Log:");
        lbl_logTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lbl_logTitle.setForeground(new Color(80, 60, 50));
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 4, 0);
        rightPanel.add(lbl_logTitle, gbc);

        // Move log list
        logModel = new DefaultListModel<>();
        lst_log = new JList<>(logModel);
        lst_log.setFont(new Font("Arial", Font.PLAIN, 11));
        lst_log.setBackground(Color.WHITE);
        JScrollPane logScroll = new JScrollPane(lst_log);
        logScroll.setPreferredSize(new Dimension(180, 100));
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 180), 1));
        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(logScroll, gbc);

        add(rightPanel, BorderLayout.EAST);
    }

    // Called when a GAME_STATE message is received from the server
    // Invoked via SwingUtilities.invokeLater from GameClient
    public void updateBoard(int dice, int newPos1, int newPos2, String nextTurn) {
        this.lastDice = dice;
        lbl_diceResult.setText("Dice: " + dice);
        btn_rollDice.setEnabled(false);

        boolean player1Moved = (newPos1 != pos1);
        String moverUsername = player1Moved
                ? (myOrder.equals("FIRST") ? myUsername : opponentUsername)
                : (myOrder.equals("SECOND") ? myUsername : opponentUsername);
        int targetPos = player1Moved ? newPos1 : newPos2;
        addLog(moverUsername + ": rolled " + dice + " -> Square " + targetPos);

        animatePixel(player1Moved, newPos1, newPos2, nextTurn);
    }

    private void animatePixel(boolean isP1, int finalPos1, int finalPos2, String nextTurn) {
        isAnimating = true;
        btn_rollDice.setEnabled(false);

        // Starting position
        int startPos = isP1 ? pos1 : pos2;

        // First move - place pawn at square 1 immediately
        if (startPos == 0) {
            int[] startPx = cellToPixel(1);
            if (isP1) {
                animX1 = startPx[0] - 10;
                animY1 = startPx[1];
            } else {
                animX2 = startPx[0] + 10;
                animY2 = startPx[1];
            }
        }

        // Build waypoint list: one entry per square up to the dice value
        // Example: pos=10, dice=4 -> [11, 12, 13, 14]
        // If snake/ladder: add the final destination at the end
        // Example: pos=32, dice=4 -> [33, 34, 35, 36, 6] (snake at 36)
        java.util.List<Integer> waypoints = new java.util.ArrayList<>();

        // Normal movement up to dice value
        int normalTarget = startPos + lastDice;
        if (normalTarget > 100) {
            normalTarget = 100;
        }

        for (int i = startPos + 1; i <= normalTarget; i++) {
            waypoints.add(i);
        }

        // If snake or ladder, append the final destination
        int snakeLadderTarget = isP1 ? finalPos1 : finalPos2;
        if (snakeLadderTarget != normalTarget) {
            waypoints.add(snakeLadderTarget);
        }

        animateWaypoints(isP1, waypoints, 0, finalPos1, finalPos2, nextTurn);
    }

    private void animateWaypoints(boolean isP1, java.util.List<Integer> waypoints,
            int index, int finalPos1, int finalPos2, String nextTurn) {
        if (index >= waypoints.size()) {
            // All waypoints completed
            pos1 = finalPos1;
            pos2 = finalPos2;
            boardPanel.repaint();
            finishAnimation(finalPos1, finalPos2, nextTurn);
            return;
        }

        int targetCell = waypoints.get(index);
        int[] targetPx = cellToPixel(targetCell);
        float targetX = isP1 ? targetPx[0] - 10 : targetPx[0] + 10;
        float targetY = targetPx[1];

        // Snake/ladder step moves faster
        boolean isSnakeLadderStep = (index == waypoints.size() - 1)
                && (waypoints.size() > lastDice);
        float speed = isSnakeLadderStep ? 8f : 5f;

        if (animTimer != null) {
            animTimer.stop();
        }
        animTimer = new javax.swing.Timer(16, null);
        animTimer.addActionListener(e -> {
            float cx = isP1 ? animX1 : animX2;
            float cy = isP1 ? animY1 : animY2;

            float dx   = targetX - cx;
            float dy   = targetY - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist <= speed) {
                // Reached this waypoint
                if (isP1) {
                    animX1 = targetX;
                    animY1 = targetY;
                } else {
                    animX2 = targetX;
                    animY2 = targetY;
                }

                if (isP1) displayPos1 = targetCell;
                else      displayPos2 = targetCell;

                animTimer.stop();
                boardPanel.repaint();

                // Brief pause before the snake/ladder step
                if (index == waypoints.size() - 2 && waypoints.size() > lastDice) {
                    javax.swing.Timer pauseTimer = new javax.swing.Timer(300, ev -> {
                        animateWaypoints(isP1, waypoints, index + 1,
                                finalPos1, finalPos2, nextTurn);
                    });
                    pauseTimer.setRepeats(false);
                    pauseTimer.start();
                } else {
                    animateWaypoints(isP1, waypoints, index + 1,
                            finalPos1, finalPos2, nextTurn);
                }
            } else {
                float nx = cx + (dx / dist) * speed;
                float ny = cy + (dy / dist) * speed;
                if (isP1) {
                    animX1 = nx;
                    animY1 = ny;
                } else {
                    animX2 = nx;
                    animY2 = ny;
                }
                boardPanel.repaint();
            }
        });
        animTimer.start();
    }

    private void finishAnimation(int newPos1, int newPos2, String nextTurn) {
        isAnimating = false;
        int myPos  = myOrder.equals("FIRST") ? newPos1 : newPos2;
        int oppPos = myOrder.equals("FIRST") ? newPos2 : newPos1;
        lbl_myPos.setText(myUsername + ": Square " + myPos);
        lbl_opponentPos.setText(opponentUsername + ": Square " + oppPos);

        // Check for a pending GAME_OVER
        if (pendingGameOver != null) {
            String winner = pendingGameOver;
            pendingGameOver = null;
            showGameOver(winner);
            return;
        }

        updateTurnState(nextTurn);
        boardPanel.repaint();
    }

    // Update button and label based on whose turn it is
    private void updateTurnState(String currentTurn) {
        boolean isMyTurn = currentTurn.equals(myUsername);
        btn_rollDice.setEnabled(isMyTurn);

        if (isMyTurn) {
            lbl_turnInfo.setText("Your Turn!");
            lbl_turnInfo.setForeground(COLOR_PRIMARY);
        } else {
            lbl_turnInfo.setText(opponentUsername + "'s Turn");
            lbl_turnInfo.setForeground(new Color(150, 120, 100));
        }
    }

    public void showGameOver(String winner) {
        addLog("--- GAME OVER: " + winner + " won! ---");
        boardPanel.repaint();
        btn_rollDice.setEnabled(false);

        // Open EndScreen and close this window
        EndScreen endScreen = new EndScreen(gameClient, myUsername, winner, logModel.size());
        endScreen.setVisible(true);
        dispose();
    }

    private void addLog(String text) {
        logModel.add(0, text); // add to top
        if (logModel.size() > 30) {
            logModel.remove(30);
        }
    }

    // Load images from assets/images/ folder
    private void loadImages() {
        // Snakes: head square -> image file
        loadAndStore("snake_36", "assets/images/s36-6.png");
        loadAndStore("snake_56", "assets/images/s56-18.png");
        loadAndStore("snake_68", "assets/images/s68-31.png");
        loadAndStore("snake_81", "assets/images/s81-40.png");
        loadAndStore("snake_84", "assets/images/s84-65.png");

        // Ladders: bottom square -> image file
        loadAndStore("ladder_4",  "assets/images/l4-17.png");
        loadAndStore("ladder_10", "assets/images/l10-29.png");
        loadAndStore("ladder_22", "assets/images/l22-43.png");
        loadAndStore("ladder_45", "assets/images/l45-58.png");
        loadAndStore("ladder_49", "assets/images/l49-72.png");
        loadAndStore("ladder_66", "assets/images/l66-87.png");
        loadAndStore("ladder_78", "assets/images/l78-97.png");
    }

    private void loadAndStore(String key, String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f = new File(System.getProperty("user.dir") + File.separator + path);
            }
            if (f.exists()) {
                images.put(key, ImageIO.read(f));
            } else {
                System.out.println("Image not found: " + path);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + path);
        }
    }

    private Image loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);

            String projectPath = System.getProperty("user.dir");
            f = new File(projectPath + File.separator + path);
            if (f.exists()) return ImageIO.read(f);

            System.out.println("Image not found: " + path);
            return null;
        } catch (Exception e) {
            System.out.println("Failed to load image: " + path);
            return null;
        }
    }

    // Convert a square number to pixel coordinates
    // Board starts at 1 (bottom-left) and ends at 100 (top-right)
    private int[] cellToPixel(int cellNumber) {
        int n   = cellNumber - 1;     // 0-indexed
        int row = n / BOARD_SIZE;     // 0 = bottom row
        int col = n % BOARD_SIZE;

        // Even rows go left-to-right, odd rows go right-to-left
        if (row % 2 == 1) {
            col = BOARD_SIZE - 1 - col;
        }

        int px = col * CELL_SIZE + CELL_SIZE / 2;
        int py = (BOARD_SIZE - 1 - row) * CELL_SIZE + CELL_SIZE / 2;
        return new int[]{px, py};
    }

    // ---- INNER CLASS: Board rendering ----
    class BoardPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            drawCells(g2);
            drawSnakesAndLadders(g2);
            drawPlayers(g2);
        }

        // Draw the board squares
        private void drawCells(Graphics2D g2) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    // Checkerboard color pattern
                    Color cellColor = (row + col) % 2 == 0 ? COLOR_CELL_LIGHT : COLOR_CELL_DARK;
                    g2.setColor(cellColor);
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                    // Grid line
                    //g2.setColor(COLOR_GRID);
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                    // Calculate square number
                    int displayRow = BOARD_SIZE - 1 - row; // 0 = bottom, 9 = top
                    int displayCol = displayRow % 2 == 0 ? col : BOARD_SIZE - 1 - col;
                    int cellNum = displayRow * BOARD_SIZE + displayCol + 1;

                    // Draw number - centered, Cooper Black font
                    String numStr = String.valueOf(cellNum);
                    g2.setFont(new Font("Cooper Black", Font.BOLD, 20));
                    g2.setColor(new Color(120, 80, 50));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(numStr);
                    int textH = fm.getAscent();
                    int textX = x + (CELL_SIZE - textW) / 2;
                    int textY = y + (CELL_SIZE + textH) / 2 - 2;
                    g2.drawString(numStr, textX, textY);
                }
            }
        }

        // Draw snake and ladder images
        private void drawSnakesAndLadders(Graphics2D g2) {
            drawImage(g2, "ladder_4",  200, 510, 20,  70,   0);
            drawImage(g2, "ladder_10", 530, 450, 30, 130, -30);
            drawImage(g2, "ladder_22", 110, 330, 30, 120,  30);
            drawImage(g2, "ladder_45", 190, 240, 40, 120, -60);
            drawImage(g2, "ladder_49", 495, 160, 30, 170,   0);
            drawImage(g2, "ladder_66", 345,  90, 35, 120,  27);
            drawImage(g2, "ladder_78", 160,  30, 30, 120,  27);

            drawImage(g2, "snake_36", 270, 370, 100, 190, 353);
            drawImage(g2, "snake_56", 150, 260, 120, 270,   5);
            drawImage(g2, "snake_68", 450, 200, 130, 190,   0);
            drawImage(g2, "snake_81",   7,  90,  55, 300, 357);
            drawImage(g2, "snake_84", 210,  80, 110, 130,   0);
        }

        // Draw an image centered at (x, y) with given size and rotation angle
        private void drawImage(Graphics2D g2, String key,
                               int x, int y, int w, int h, double angle) {
            Image img = images.get(key);
            if (img == null) return;

            AffineTransform old = g2.getTransform();
            // Use the center of the image as the pivot point
            g2.translate(x + w / 2, y + h / 2);
            g2.rotate(Math.toRadians(angle));
            g2.drawImage(img, -w / 2, -h / 2, w, h, null);
            g2.setTransform(old);
        }

        // Draw player pawns
        private void drawPlayers(Graphics2D g2) {
            if (pos1 > 0 || displayPos1 > 0) {
                drawPawn(g2, (int) animX1, (int) animY1, COLOR_P1,
                        myOrder.equals("FIRST")
                        ? myUsername.substring(0, 1).toUpperCase()
                        : opponentUsername.substring(0, 1).toUpperCase());
            }
            if (pos2 > 0 || displayPos2 > 0) {
                drawPawn(g2, (int) animX2, (int) animY2, COLOR_P2,
                        myOrder.equals("SECOND")
                        ? myUsername.substring(0, 1).toUpperCase()
                        : opponentUsername.substring(0, 1).toUpperCase());
            }
        }

        // Draw a single pawn (circle + letter)
        private void drawPawn(Graphics2D g2, int cx, int cy, Color color, String letter) {
            int r = 16;
            g2.setColor(color);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(letter, cx - 5, cy + 5);
        }
    }

    public void showConnectionLost(String message) {
        btn_rollDice.setEnabled(false);
        addLog("--- CONNECTION LOST ---");

        JOptionPane.showMessageDialog(
                this,
                message,
                "Connection Lost",
                JOptionPane.WARNING_MESSAGE
        );

        // Notify server to clean up the game room
        gameClient.sendMessage("GAME_ENDED");
        dispose();
    }

}