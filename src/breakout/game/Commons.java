package breakout.game;

// All the sizes and positions of the game in one place.
public class Commons {

    public static final int WIDTH = 300;
    public static final int HEIGHT = 400;
    public static final int BOTTOM_EDGE = 390;   // if the ball goes below this, it is lost

    public static final int BALL_SIZE = 5;
    public static final int PADDLE_WIDTH = 40;
    public static final int PADDLE_HEIGHT = 10;
    public static final int BRICK_WIDTH = 40;
    public static final int BRICK_HEIGHT = 10;

    public static final int PADDLE_Y = 360;      // the paddle only moves left and right
    public static final int BALL_START_Y = 355;  // the ball starts just above the paddle

    // A new brick appears in a random cell of a 6 x 5 grid.
    public static final int BRICK_COLUMNS = 6;
    public static final int BRICK_ROWS = 5;
    public static final int BRICK_GRID_X = 30;
    public static final int BRICK_GRID_Y = 50;

    public static final int PERIOD = 5;          // milliseconds between frames in the window
}
