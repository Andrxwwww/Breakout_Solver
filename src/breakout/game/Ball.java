package breakout.game;

public class Ball extends Sprite {

    int xdir;   // -1 left, 0 straight, 1 right
    int ydir;   // -1 up, 1 down

    Ball(int x, int y, int xdir) {
        super(x, y, Commons.BALL_SIZE, Commons.BALL_SIZE);
        this.xdir = xdir;
        this.ydir = -1;   // starts going up
    }

    // Moves one pixel and bounces off the walls and the ceiling.
    void move() {
        x += xdir;
        y += ydir;
        if (x <= 0) {
            xdir = 1;
        }
        if (x >= Commons.WIDTH - width) {
            xdir = -1;
        }
        if (y <= 0) {
            ydir = 1;
        }
    }
}
