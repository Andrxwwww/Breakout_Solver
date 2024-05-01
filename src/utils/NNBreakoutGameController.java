package utils;

import breakout.BreakoutBoard;

public class NNBreakoutGameController implements GameController {

    private NeuralNetwork neuralNetwork;

    public NNBreakoutGameController() {
        int inputSize = Commons.BREAKOUT_STATE_SIZE;
        int outputSize = Commons.BREAKOUT_NUM_ACTIONS;

        this.neuralNetwork = new NeuralNetwork(inputSize, outputSize);
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setNeuralNetwork(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    @Override
    public int nextMove(int[] currentState) {
        double[] input = new double[currentState.length];
        //System.out.println("Input: " + Arrays.toString(currentState));
        for (int i = 0; i < currentState.length; i++) {
            input[i] = currentState[i];
        }

        double[] output = neuralNetwork.forward(input);

        //System.out.println("Output: " + Arrays.toString(output));

        if (output[0] > output[1]) {
            return BreakoutBoard.RIGHT; // RIGHT move
        } else {
            return BreakoutBoard.LEFT; // LEFT move
        }

    }
}

