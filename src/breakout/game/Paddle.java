package breakout.game;

public class Paddle extends Sprite {

    int dx;

    Paddle(int x) {
        super(x, Commons.PADDLE_Y, Commons.PADDLE_WIDTH, Commons.PADDLE_HEIGHT);
    }

    // The paddle can now also stand still (the first version could only go left or right,
    // so it was always shaking).
    void makeMove(int move) {
        if (move == BreakoutBoard.LEFT) {
            dx = -1;
        } else if (move == BreakoutBoard.RIGHT) {
            dx = 1;
        } else {
            dx = 0;
        }
    }

    void move() {
        x += dx;
        x = Math.max(0, Math.min(Commons.WIDTH - width, x));   // stay on the screen
    }
}
