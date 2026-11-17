package breakout.game;

public class Brick extends Sprite {

    Brick() {
        super(0, 0, Commons.BRICK_WIDTH, Commons.BRICK_HEIGHT);
    }

    void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
