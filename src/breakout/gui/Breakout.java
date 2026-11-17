package breakout.gui;

import breakout.ai.BreakoutGeneticAlgorithm;
import breakout.game.BreakoutBoard;
import breakout.game.Commons;
import breakout.game.GameController;
import breakout.game.Sprite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// The window. It only DRAWS a BreakoutBoard; the rules are all in game/BreakoutBoard.
// When a game ends, the next one starts after a short pause.
public class Breakout extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GameController player;
    private long seed;
    private BreakoutBoard board;
    private int pause;   // frames left before the next game starts

    private final Image ballImage = new ImageIcon("src/resources/ball.png").getImage();
    private final Image paddleImage = new ImageIcon("src/resources/paddle.png").getImage();
    private final Image brickImage = new ImageIcon("src/resources/brick.png").getImage();

    public Breakout(GameController player, long seed) {
        this.player = player;
        this.seed = seed;
        this.board = new BreakoutBoard(seed);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        panel.setPreferredSize(new Dimension(Commons.WIDTH, Commons.HEIGHT));
        add(panel);

        setTitle("Breakout");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        new Timer(Commons.PERIOD, e -> {
            update();
            panel.repaint();
        }).start();
    }

    private void update() {
        boolean over = !board.isInGame() || board.getTime() >= BreakoutGeneticAlgorithm.MAX_TIME;
        if (!over) {
            board.step(player.nextMove(board.getState()));
        } else if (++pause > 300) {   // about 1.5 seconds
            pause = 0;
            board = new BreakoutBoard(++seed);
        }
    }

    private void draw(Graphics g) {
        drawSprite(g, ballImage, board.getBall(), Color.BLUE);
        drawSprite(g, paddleImage, board.getPaddle(), Color.DARK_GRAY);
        drawSprite(g, brickImage, board.getBrick(), Color.ORANGE);

        g.setColor(Color.BLACK);
        g.drawString("Game " + seed + "   Bricks: " + board.getKills(), 10, 15);
        if (!board.isInGame()) {
            g.drawString("Ball lost", Commons.WIDTH / 2 - 25, Commons.HEIGHT / 2);
        }
    }

    // Draws the image, or a plain rectangle if the image file was not found.
    private void drawSprite(Graphics g, Image image, Sprite s, Color fallback) {
        if (image.getWidth(null) > 0) {
            g.drawImage(image, s.getX(), s.getY(), s.getWidth(), s.getHeight(), null);
        } else {
            g.setColor(fallback);
            g.fillRect(s.getX(), s.getY(), s.getWidth(), s.getHeight());
        }
    }
}
