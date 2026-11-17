package breakout.ai;

import breakout.game.BreakoutBoard;
import breakout.game.GameController;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

// Trains the network by evolution: no one tells it the right move, it only gets a score.
//
// Every generation:
//   1. every network plays the same 32 games (NEW games every generation, so it can't memorise them)
//   2. the 4 best are copied unchanged into the next generation
//   3. the rest are children: two good parents are picked, their genes are mixed and a few are changed
//   4. the best network also plays 200 fixed "validation" games; the one that does best there is kept
//
// The first version trained and tested on ONE single game, so it just memorised that game.
public class BreakoutGeneticAlgorithm {

    private static final int POPULATION_SIZE = 100;
    private static final int GENERATIONS = 400;
    private static final int GAMES_PER_GENERATION = 32;
    private static final int VALIDATION_GAMES = 200;
    private static final int ELITES = 4;              // best networks kept unchanged
    private static final int TOURNAMENT_SIZE = 3;     // a parent is the best of 3 random networks
    private static final double CROSSOVER_RATE = 0.5; // chance that a child has two parents instead of one
    private static final double MUTATION_RATE = 0.1;  // chance that each gene gets changed
    private static final double MUTATION_SIZE = 0.2;  // how big a change is at the start...
    private static final double MUTATION_DECAY = 0.995; // ...it gets a bit smaller every generation
    public static final int MAX_TIME = 20000;                // ticks per game
    private static final int STALL_LIMIT = 3000;      // stop a game if no brick for this long (ball stuck in a loop)

    static final long FIRST_TRAIN_SEED = 1000;
    static final long FIRST_VALIDATION_SEED = 100000;

    private final Random random = new Random(1);   // fixed seed: the same run every time

    public NeuralNetwork evolve() {
        double[][] population = new double[POPULATION_SIZE][];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = newNetwork(random).getGenes();
        }

        NeuralNetwork best = null;
        double bestValidation = -1;
        double mutationSize = MUTATION_SIZE;

        for (int generation = 0; generation < GENERATIONS; generation++) {
            // 1. Score everyone on this generation's games (in parallel, it's the slow part).
            long firstSeed = FIRST_TRAIN_SEED + (long) generation * GAMES_PER_GENERATION;
            double[][] genes = population;
            double[] fitness = IntStream.range(0, POPULATION_SIZE).parallel()
                .mapToDouble(i -> averageFitness(genes[i], firstSeed))
                .toArray();

            // Sort from best to worst.
            Integer[] order = new Integer[POPULATION_SIZE];
            for (int i = 0; i < POPULATION_SIZE; i++) {
                order[i] = i;
            }
            Arrays.sort(order, Comparator.comparingDouble((Integer i) -> fitness[i]).reversed());
            double[][] sorted = new double[POPULATION_SIZE][];
            double[] sortedFitness = new double[POPULATION_SIZE];
            for (int rank = 0; rank < POPULATION_SIZE; rank++) {
                sorted[rank] = population[order[rank]];
                sortedFitness[rank] = fitness[order[rank]];
            }

            // 4. Keep the generation's winner if it also does best on the validation games.
            NeuralNetwork champion = fromGenes(sorted[0]);
            double validation = averageBricks(champion, FIRST_VALIDATION_SEED, VALIDATION_GAMES);
            if (validation > bestValidation) {
                bestValidation = validation;
                best = champion;
            }
            if (generation % 10 == 0 || generation == GENERATIONS - 1) {
                System.out.printf("Generation %d: best fitness %.1f, bricks per game on validation %.2f (best so far %.2f)%n",
                    generation, sortedFitness[0], validation, bestValidation);
            }

            // 2 + 3. Build the next generation.
            double[][] next = new double[POPULATION_SIZE][];
            for (int i = 0; i < ELITES; i++) {
                next[i] = sorted[i];
            }
            for (int i = ELITES; i < POPULATION_SIZE; i++) {
                double[] parent1 = pickParent(sorted, sortedFitness);
                double[] parent2 = pickParent(sorted, sortedFitness);
                double[] child = random.nextDouble() < CROSSOVER_RATE ? crossover(parent1, parent2) : parent1.clone();
                mutate(child, mutationSize);
                next[i] = child;
            }
            population = next;
            mutationSize *= MUTATION_DECAY;
        }
        return best;
    }

    // Tournament selection: take 3 random networks, the best one becomes a parent.
    // Good networks have children more often, but weaker ones still get a chance.
    private double[] pickParent(double[][] population, double[] fitness) {
        int best = random.nextInt(POPULATION_SIZE);
        for (int i = 1; i < TOURNAMENT_SIZE; i++) {
            int challenger = random.nextInt(POPULATION_SIZE);
            if (fitness[challenger] > fitness[best]) {
                best = challenger;
            }
        }
        return population[best];
    }

    // Each gene comes from one parent or the other, 50/50.
    private double[] crossover(double[] parent1, double[] parent2) {
        double[] child = new double[parent1.length];
        for (int i = 0; i < child.length; i++) {
            child[i] = random.nextBoolean() ? parent1[i] : parent2[i];
        }
        return child;
    }

    // Changes about 10% of the genes by a small random amount.
    private void mutate(double[] genes, double size) {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                genes[i] += random.nextGaussian() * size;
            }
        }
    }

    private static double averageFitness(double[] genes, long firstSeed) {
        GameController player = new NNBreakoutGameController(fromGenes(genes));
        double total = 0;
        for (int g = 0; g < GAMES_PER_GENERATION; g++) {
            BreakoutBoard board = new BreakoutBoard(firstSeed + g);
            board.runSimulation(player, MAX_TIME, STALL_LIMIT);
            total += board.getFitness(MAX_TIME);
        }
        return total / GAMES_PER_GENERATION;
    }

    // Average bricks per game over games firstSeed, firstSeed+1, ... (no stall limit here).
    static double averageBricks(NeuralNetwork network, long firstSeed, int games) {
        GameController player = new NNBreakoutGameController(network);
        double total = 0;
        for (int g = 0; g < games; g++) {
            total += new BreakoutBoard(firstSeed + g).runSimulation(player, MAX_TIME, 0);
        }
        return total / games;
    }

    static NeuralNetwork newNetwork(Random random) {
        return new NeuralNetwork(NNBreakoutGameController.INPUTS, NNBreakoutGameController.HIDDEN,
                                 NNBreakoutGameController.OUTPUTS, random);
    }

    static NeuralNetwork fromGenes(double[] genes) {
        return new NeuralNetwork(NNBreakoutGameController.INPUTS, NNBreakoutGameController.HIDDEN,
                                 NNBreakoutGameController.OUTPUTS, genes);
    }
}
