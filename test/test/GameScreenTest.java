package test;

/*
 * Test amaçli - GameScreen'in gorunumunu kontrol etmek icin
 * Gercek GameClient olmadan sahte verilerle calisir
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import javax.swing.*;

/**
 *
 * @author furkan
 */
public class GameScreenTest extends JFrame {

    static final Color COLOR_BG = new Color(255, 248, 240);
    static final Color COLOR_PRIMARY = new Color(255, 107, 53);
    static final Color COLOR_CELL_LIGHT = new Color(255, 241, 220);
    static final Color COLOR_CELL_DARK = new Color(255, 209, 102);
    static final Color COLOR_GRID = new Color(210, 180, 140);
    static final Color COLOR_P1 = new Color(255, 107, 53);
    static final Color COLOR_P2 = new Color(17, 138, 178);

    static final int BOARD_SIZE = 10;
    static final int CELL_SIZE = 60;
    static final int BOARD_PIXEL = BOARD_SIZE * CELL_SIZE;

    static final HashMap<Integer, Integer> SNAKES = new HashMap<>();
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

    // Test icin sahte pozisyonlar
    int pos1 = 34;
    int pos2 = 21;

    BoardPanel boardPanel;
    JButton btn_rollDice;
    JLabel lbl_turnInfo;
    JLabel lbl_diceResult;
    JLabel lbl_myPos;
    JLabel lbl_opponentPos;
    DefaultListModel<String> logModel;

    public GameScreenTest() {
        initFrame();
        initComponents();

        // Test: birkac log satiri ekle
        logModel.add(0, "Ali: zar 4 -> Kare 34");
        logModel.add(0, "Veli: yilan! 36->6");
        logModel.add(0, "Ali: merdiven! 4->17");
        logModel.add(0, "Veli: zar 3 -> Kare 21");
    }

    private void initFrame() {
        setTitle("Snakes & Ladders - Ali vs Veli [TEST]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout(0, 0));
    }

    private void initComponents() {
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(BOARD_PIXEL, BOARD_PIXEL));
        boardPanel.setBorder(BorderFactory.createLineBorder(COLOR_GRID, 2));
        add(boardPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(COLOR_BG);
        rightPanel.setPreferredSize(new Dimension(220, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);

        lbl_turnInfo = new JLabel("Sira: Senin!", SwingConstants.CENTER);
        lbl_turnInfo.setFont(new Font("Arial", Font.BOLD, 15));
        lbl_turnInfo.setForeground(COLOR_PRIMARY);
        lbl_turnInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(lbl_turnInfo, gbc);

        lbl_diceResult = new JLabel("Zar: 4", SwingConstants.CENTER);
        lbl_diceResult.setFont(new Font("Arial", Font.BOLD, 28));
        lbl_diceResult.setForeground(new Color(80, 60, 50));
        gbc.gridy = 1;
        gbc.insets = new Insets(12, 0, 4, 0);
        rightPanel.add(lbl_diceResult, gbc);

        btn_rollDice = new JButton("Zar At");
        btn_rollDice.setFont(new Font("Arial", Font.BOLD, 16));
        btn_rollDice.setBackground(COLOR_PRIMARY);
        btn_rollDice.setForeground(Color.WHITE);
        btn_rollDice.setFocusPainted(false);
        btn_rollDice.setBorderPainted(false);
        btn_rollDice.setPreferredSize(new Dimension(180, 46));
        btn_rollDice.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        // Test: butona tiklayinca pozisyonlari degistir
        btn_rollDice.addActionListener(e -> {
            pos1 = (int) (Math.random() * 100) + 1;
            pos2 = (int) (Math.random() * 100) + 1;
            int dice = (int) (Math.random() * 6) + 1;
            lbl_diceResult.setText("Zar: " + dice);
            lbl_myPos.setText("Ali: Kare " + pos1);
            lbl_opponentPos.setText("Veli: Kare " + pos2);
            logModel.add(0, "Ali: zar " + dice + " -> Kare " + pos1);
            boardPanel.repaint();
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 16, 0);
        rightPanel.add(btn_rollDice, gbc);

        lbl_myPos = new JLabel("Ali: Kare " + pos1, SwingConstants.LEFT);
        lbl_myPos.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_myPos.setForeground(COLOR_P1);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 2, 0);
        rightPanel.add(lbl_myPos, gbc);

        lbl_opponentPos = new JLabel("Veli: Kare " + pos2, SwingConstants.LEFT);
        lbl_opponentPos.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl_opponentPos.setForeground(COLOR_P2);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 0, 16, 0);
        rightPanel.add(lbl_opponentPos, gbc);

        JLabel lbl_logTitle = new JLabel("Hareket Gunlugu:");
        lbl_logTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lbl_logTitle.setForeground(new Color(80, 60, 50));
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 4, 0);
        rightPanel.add(lbl_logTitle, gbc);

        logModel = new DefaultListModel<>();
        JList<String> lst_log = new JList<>(logModel);
        lst_log.setFont(new Font("Arial", Font.PLAIN, 11));
        lst_log.setBackground(Color.WHITE);
        JScrollPane logScroll = new JScrollPane(lst_log);
        logScroll.setPreferredSize(new Dimension(180, 200));
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 180), 1));
        gbc.gridy = 6;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(logScroll, gbc);

        add(rightPanel, BorderLayout.EAST);
    }

    private int[] cellToPixel(int cellNumber) {
        int n = cellNumber - 1;
        int row = n / BOARD_SIZE;
        int col = n % BOARD_SIZE;
        if (row % 2 == 1) {
            col = BOARD_SIZE - 1 - col;
        }
        int px = col * CELL_SIZE + CELL_SIZE / 2;
        int py = (BOARD_SIZE - 1 - row) * CELL_SIZE + CELL_SIZE / 2;
        return new int[]{px, py};
    }

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

        private void drawCells(Graphics2D g2) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    Color cellColor = (row + col) % 2 == 0 ? COLOR_CELL_LIGHT : COLOR_CELL_DARK;
                    g2.setColor(cellColor);
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g2.setColor(COLOR_GRID);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                    int displayRow = BOARD_SIZE - 1 - row;
                    int displayCol = displayRow % 2 == 0 ? col : BOARD_SIZE - 1 - col;
                    int cellNum = displayRow * BOARD_SIZE + displayCol + 1;

                    // Numara - ortalanmis, eglenceli font
                    String numStr = String.valueOf(cellNum);
                    g2.setFont(new Font("Cooper Black", Font.BOLD, 20));
                    g2.setColor(new Color(120, 80, 50));

                    // FontMetrics ile tam ortaya yerlestir
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(numStr);
                    int textH = fm.getAscent();
                    int textX = x + (CELL_SIZE - textW) / 2;
                    int textY = y + (CELL_SIZE + textH) / 2 - 2;
                    g2.drawString(numStr, textX, textY);
                }
            }
        }

        private void drawSnakesAndLadders(Graphics2D g2) {
            // Resim yokken renkli cizgi ciz (placeholder)
            for (java.util.Map.Entry<Integer, Integer> e : SNAKES.entrySet()) {
                drawLine(g2, e.getKey(), e.getValue(), new Color(220, 60, 60), 4f);
                drawDot(g2, e.getKey(), new Color(220, 60, 60), "Y");
            }
            for (java.util.Map.Entry<Integer, Integer> e : LADDERS.entrySet()) {
                drawLine(g2, e.getKey(), e.getValue(), new Color(30, 160, 100), 4f);
                drawDot(g2, e.getKey(), new Color(30, 160, 100), "M");
            }
        }

        private void drawLine(Graphics2D g2, int from, int to, Color color, float width) {
            int[] f = cellToPixel(from);
            int[] t = cellToPixel(to);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(f[0], f[1], t[0], t[1]);
        }

        private void drawDot(Graphics2D g2, int cell, Color color, String letter) {
            int[] p = cellToPixel(cell);
            g2.setColor(color);
            g2.fillOval(p[0] - 10, p[1] - 10, 20, 20);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString(letter, p[0] - 4, p[1] + 4);
        }

        private void drawPlayers(Graphics2D g2) {
            if (pos1 > 0) {
                int[] p = cellToPixel(pos1);
                drawPawn(g2, p[0] - 10, p[1], COLOR_P1, "A");
            }
            if (pos2 > 0) {
                int[] p = cellToPixel(pos2);
                drawPawn(g2, p[0] + 10, p[1], COLOR_P2, "V");
            }
        }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameScreenTest().setVisible(true);
        });
    }
}
