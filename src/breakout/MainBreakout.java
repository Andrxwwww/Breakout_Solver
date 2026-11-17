package breakout;

import breakout.ai.BreakoutGeneticAlgorithm;
import breakout.ai.NNBreakoutGameController;
import breakout.ai.NeuralNetwork;
import breakout.game.BreakoutBoard;
import breakout.game.Commons;
import breakout.game.GameController;
import breakout.gui.Breakout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

// How to use (run from the project folder):
//   javac -d out src/breakout/*.java src/breakout/*/*.java     compile first
//   java -cp out breakout.MainBreakout          watch the trained network play
//   java -cp out breakout.MainBreakout train    train a new network (about an hour) and save it
//   java -cp out breakout.MainBreakout test     compare the network with simple strategies
public class MainBreakout {

    private static final Path MODEL_FILE = Path.of("models/best.txt");
    private static final long FIRST_TEST_SEED = 1000000;   // test games: never used in training
    private static final int TEST_GAMES = 500;

    public static void main(String[] args) throws Exception {
        String command = args.length > 0 ? args[0] : "play";

        if (!command.equals("train") && !Files.exists(MODEL_FILE)) {
            System.out.println("Can't find " + MODEL_FILE.toAbsolutePath());
            System.out.println("Run this from the project folder (the one with src/ and models/ in it),");
            System.out.println("or train a new network first with:  java -cp out breakout.MainBreakout train");
            return;
        }

        if (command.equals("train")) {
            NeuralNetwork best = new BreakoutGeneticAlgorithm().evolve();
            best.save(MODEL_FILE);
            System.out.println("Saved " + MODEL_FILE);
            test(best);
        } else if (command.equals("test")) {
            test(NeuralNetwork.load(MODEL_FILE));
        } else {
            NeuralNetwork network = NeuralNetwork.load(MODEL_FILE);
            new Breakout(new NNBreakoutGameController(network), FIRST_TEST_SEED);
        }
    }

    // Plays the same 500 unseen games with each player and prints the results.
    private static void test(NeuralNetwork network) {
        System.out.println("Bricks per game over " + TEST_GAMES + " test games:");
        report("Neural network", seed -> new NNBreakoutGameController(network));
        report("Rule: follow the ball", seed -> MainBreakout::followTheBall);
        report("Random moves", seed -> {
            Random random = new Random(seed);
            return state -> random.nextInt(3);
        });
    }

    interface PlayerForGame {
        GameController create(long seed);
    }

    private static void report(String name, PlayerForGame players) {
        int bricks = 0, survived = 0;
        for (long seed = FIRST_TEST_SEED; seed < FIRST_TEST_SEED + TEST_GAMES; seed++) {
            BreakoutBoard board = new BreakoutBoard(seed);
            bricks += board.runSimulation(players.create(seed), BreakoutGeneticAlgorithm.MAX_TIME, 0);
            if (board.isInGame()) {
                survived++;
            }
        }
        System.out.printf("  %-24s %6.2f bricks   (ball never lost in %d%% of games)%n",
            name, (double) bricks / TEST_GAMES, 100 * survived / TEST_GAMES);
    }

    // A simple hand-written strategy to compare against: follow the ball, but catch it with the
    // edge of the paddle so it flies off diagonally and sweeps the screen. It never looks at the brick.
    private static int followTheBall(int[] state) {
        int ballX = state[0], paddleX = state[3], ballXDir = state[7];
        int target = ballX - (ballXDir < 0 ? 6 : 30);
        target = Math.max(0, Math.min(Commons.WIDTH - Commons.PADDLE_WIDTH, target));
        if (paddleX > target) {
            return BreakoutBoard.LEFT;
        }
        if (paddleX < target) {
            return BreakoutBoard.RIGHT;
        }
        return BreakoutBoard.STAY;
    }
}
