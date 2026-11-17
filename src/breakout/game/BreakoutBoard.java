package breakout.game;

import java.util.Random;

// The game rules. No graphics here: the window (gui/Breakout.java) just draws this board.
// That way training can play thousands of games per second without opening any window.
public class BreakoutBoard {

    public static final int STAY = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private final Random random;
    final Ball ball;
    final Paddle paddle;
    final Brick brick = new Brick();

    private boolean inGame = true;
    private int time;              // ticks played
    private int timeSinceBrick;    // ticks since the last brick was destroyed
    private int kills;             // bricks destroyed

    // The seed decides the whole game: where the paddle starts and where every brick appears.
    // Same seed = same game, so results can be repeated.
    public BreakoutBoard(long seed) {
        random = new Random(seed);
        paddle = new Paddle(random.nextInt(Commons.WIDTH - Commons.PADDLE_WIDTH + 1));
        ball = new Ball(paddle.x + 30, Commons.BALL_START_Y, random.nextBoolean() ? 1 : -1);
        moveBrick();
    }

    // Plays until the ball is lost or the time is up. Returns the number of bricks destroyed.
    // stallLimit > 0 also stops the game if no brick was hit for that many ticks (used in training).
    public int runSimulation(GameController controller, int maxTime, int stallLimit) {
        while (inGame && time < maxTime && (stallLimit == 0 || timeSinceBrick < stallLimit)) {
            step(controller.nextMove(getState()));
        }
        return kills;
    }

    // One tick of the game.
    public void step(int move) {
        if (!inGame) {
            return;
        }
        paddle.makeMove(move);
        ball.move();
        paddle.move();
        timeSinceBrick++;
        checkCollision();
        time++;
    }

    private void checkCollision() {
        // Ball fell past the paddle.
        if (ball.y + ball.height > Commons.BOTTOM_EDGE) {
            inGame = false;
            return;
        }

        // Ball hits the paddle. WHERE it hits decides where it goes next,
        // and that is what the network learns to use to aim at the brick.
        if (ball.ydir > 0 && ball.intersects(paddle)) {
            int hitPosition = ball.x - paddle.x;
            if (hitPosition < 16) {
                ball.xdir = -1;   // left part: up-left
            } else if (hitPosition < 24) {
                ball.xdir = 0;    // middle: straight up
            } else {
                ball.xdir = 1;    // right part: up-right
            }
            ball.ydir = -1;
        }

        // Ball hits the brick: bounce off the side it touched, then a new brick appears.
        if (ball.intersects(brick)) {
            int size = Commons.BALL_SIZE;
            if (brick.contains(ball.x + size + 1, ball.y)) {
                ball.xdir = -1;
            } else if (brick.contains(ball.x - 1, ball.y)) {
                ball.xdir = 1;
            }
            if (brick.contains(ball.x, ball.y - 1)) {
                ball.ydir = 1;
            } else if (brick.contains(ball.x, ball.y + size + 1)) {
                ball.ydir = -1;
            }
            kills++;
            timeSinceBrick = 0;
            moveBrick();
        }
    }

    // Puts the brick in a random cell. Tries again if that cell is right on top of the ball
    // (in the first version this gave a free point).
    private void moveBrick() {
        do {
            brick.moveTo(random.nextInt(Commons.BRICK_COLUMNS) * Commons.BRICK_WIDTH + Commons.BRICK_GRID_X,
                         random.nextInt(Commons.BRICK_ROWS) * Commons.BRICK_HEIGHT + Commons.BRICK_GRID_Y);
        } while (ball.intersects(brick));
    }

    // What a player gets to see each tick.
    public int[] getState() {
        return new int[] {
            ball.x, ball.y, ball.ydir,
            paddle.x, paddle.y,
            brick.x, brick.y,
            ball.xdir,        // was missing in the first version: without it you can't know where the ball goes
            timeSinceBrick
        };
    }

    public Ball getBall()     { return ball; }
    public Paddle getPaddle() { return paddle; }
    public Brick getBrick()   { return brick; }

    public boolean isInGame() { return inGame; }
    public int getKills()     { return kills; }
    public int getTime()      { return time; }

    // Score used by the genetic algorithm: bricks count the most, and surviving longer adds a
    // little bit (up to 50 points), which helps at the very start when nobody hits any brick yet.
    public double getFitness(int maxTime) {
        return kills * 100.0 + 50.0 * time / maxTime;
    }
}
