package breakout;


import utils.*;


public class MainBreakout {

    //BEST SEEDS: 478 (2079757) , 9 

    private static final int SEED = 9;

    public static void main(String[] args) {
        BreakoutGeneticAlgorithm geneticAlgorithm = new BreakoutGeneticAlgorithm();
        NeuralNetwork bestNetwork = geneticAlgorithm.evolve(SEED);

        NNBreakoutGameController nnGameController = new NNBreakoutGameController();
        nnGameController.setNeuralNetwork(bestNetwork);

        Breakout board = new Breakout(nnGameController , SEED);
    }
    
}

