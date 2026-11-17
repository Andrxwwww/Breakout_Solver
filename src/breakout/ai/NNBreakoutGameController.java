package breakout.ai;

import breakout.game.Commons;
import breakout.game.GameController;

// Lets the neural network play: game state -> 11 inputs -> network -> move.
public class NNBreakoutGameController implements GameController {

    public static final int INPUTS = 11;
    public static final int HIDDEN = 16;
    public static final int OUTPUTS = 3;   // STAY, LEFT, RIGHT

    private final NeuralNetwork network;

    public NNBreakoutGameController(NeuralNetwork network) {
        this.network = network;
    }

    @Override
    public int nextMove(int[] state) {
        double[] output = network.forward(inputs(state));
        int best = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[best]) {
                best = i;
            }
        }
        return best;   // 0 = STAY, 1 = LEFT, 2 = RIGHT
    }

    // Turns the game state into numbers the network can work with.
    // In the first version the raw pixel positions (0 to 390) went straight in, so every neuron
    // was always fully "on" or "off". Now everything is scaled to small numbers around 0.
    static double[] inputs(int[] state) {
        int ballX = state[0], ballY = state[1], ballYDir = state[2];
        int paddleX = state[3];
        int brickX = state[5], brickY = state[6];
        int ballXDir = state[7];
        int timeSinceBrick = state[8];

        // Where the ball will be when it comes down to the paddle. Telling the network this
        // directly makes learning much faster: it can focus on aiming instead of on geometry.
        double landing = landingX(ballX, ballY, ballXDir, ballYDir) + Commons.BALL_SIZE / 2.0;
        double paddleCentre = paddleX + Commons.PADDLE_WIDTH / 2.0;
        double brickCentre = brickX + Commons.BRICK_WIDTH / 2.0;
        double half = Commons.WIDTH / 2.0;

        return new double[] {
            scale(ballX, Commons.WIDTH),
            scale(ballY, Commons.HEIGHT),
            ballXDir,
            ballYDir,
            scale(paddleX, Commons.WIDTH),
            scale(brickX, Commons.WIDTH),
            scale(brickY, Commons.HEIGHT),
            scale(landing, Commons.WIDTH),
            (landing - paddleCentre) / half,   // how far the paddle is from where the ball lands
            (brickCentre - landing) / half,    // where the brick is compared to that point (for aiming)
            2.0 * Math.min(1.0, timeSinceBrick / 3000.0) - 1.0   // "I've been stuck for a while"
        };
    }

    // 0..max  ->  -1..1
    private static double scale(double value, double max) {
        return 2.0 * value / max - 1.0;
    }

    // The ball's x position when it reaches the paddle, counting bounces off the side walls.
    // The ball moves 1 pixel per tick on both axes, so after t ticks it has moved t pixels sideways.
    static int landingX(int ballX, int ballY, int xdir, int ydir) {
        int maxX = Commons.WIDTH - Commons.BALL_SIZE;
        int paddleLine = Commons.PADDLE_Y - Commons.BALL_SIZE + 1;
        int ticks = ydir > 0 ? Math.max(0, paddleLine - ballY) : ballY + paddleLine;  // going up: to the ceiling and back
        // Bouncing between two walls is like moving on a line that is folded back and forth.
        int x = Math.floorMod(ballX + xdir * ticks, 2 * maxX);
        return x > maxX ? 2 * maxX - x : x;
    }
}
