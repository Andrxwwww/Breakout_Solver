package breakout.game;

// Something on the screen with a position and a size: the ball, the paddle or the brick.
// (In the first version the size came from the images. Now it comes from Commons, so the game
// can run without loading any images, which makes training much faster.)
public class Sprite {

    int x;
    int y;
    int width;
    int height;

    Sprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX()      { return x; }
    public int getY()      { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    // Do the two rectangles overlap?
    boolean intersects(Sprite other) {
        return x < other.x + other.width && x + width > other.x
            && y < other.y + other.height && y + height > other.y;
    }

    // Is the point (px, py) inside this rectangle?
    boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }
}
