package breakout.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

// A small neural network: inputs -> one hidden layer (tanh) -> outputs.
//
// Changes from the first version:
//  - biases: without them a neuron can only draw lines through zero, which limits what it can learn
//  - tanh instead of sigmoid in the hidden layer: centred on 0, works better with inputs from -1 to 1
//  - no activation on the output: we only pick the biggest output anyway
//  - getGenes / the genes constructor: the genetic algorithm sees the network as one list of numbers
//  - save / load, so you train once and keep the result
public class NeuralNetwork {

    final int inputSize;
    final int hiddenSize;
    final int outputSize;

    final double[][] weightsInputHidden;    // [input][hidden]
    final double[] biasHidden;
    final double[][] weightsHiddenOutput;   // [hidden][output]
    final double[] biasOutput;

    // A new network with random weights (biases start at 0).
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, Random random) {
        this(inputSize, hiddenSize, outputSize);
        randomize(weightsInputHidden, inputSize, hiddenSize, random);
        randomize(weightsHiddenOutput, hiddenSize, outputSize, random);
    }

    // A network built from a list of genes (see getGenes for the order).
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize, double[] genes) {
        this(inputSize, hiddenSize, outputSize);
        int g = 0;
        for (int j = 0; j < hiddenSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                weightsInputHidden[i][j] = genes[g++];
            }
        }
        for (int j = 0; j < hiddenSize; j++) {
            biasHidden[j] = genes[g++];
        }
        for (int k = 0; k < outputSize; k++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsHiddenOutput[j][k] = genes[g++];
            }
        }
        for (int k = 0; k < outputSize; k++) {
            biasOutput[k] = genes[g++];
        }
    }

    private NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        weightsInputHidden = new double[inputSize][hiddenSize];
        biasHidden = new double[hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        biasOutput = new double[outputSize];
    }

    // Random weights in a range that depends on the layer size ("Xavier" initialisation),
    // so the signals don't blow up or fade out when they go through the network.
    private static void randomize(double[][] weights, int in, int out, Random random) {
        double limit = Math.sqrt(6.0 / (in + out));
        for (int j = 0; j < out; j++) {
            for (int i = 0; i < in; i++) {
                weights[i][j] = (random.nextDouble() * 2 - 1) * limit;
            }
        }
    }

    public double[] forward(double[] input) {
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biasHidden[j];
            for (int i = 0; i < inputSize; i++) {
                sum += input[i] * weightsInputHidden[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        double[] output = new double[outputSize];
        for (int k = 0; k < outputSize; k++) {
            double sum = biasOutput[k];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * weightsHiddenOutput[j][k];
            }
            output[k] = sum;
        }
        return output;
    }

    // All weights and biases in one list: hidden weights, hidden biases, output weights, output biases.
    public double[] getGenes() {
        double[] genes = new double[inputSize * hiddenSize + hiddenSize + hiddenSize * outputSize + outputSize];
        int g = 0;
        for (int j = 0; j < hiddenSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                genes[g++] = weightsInputHidden[i][j];
            }
        }
        for (int j = 0; j < hiddenSize; j++) {
            genes[g++] = biasHidden[j];
        }
        for (int k = 0; k < outputSize; k++) {
            for (int j = 0; j < hiddenSize; j++) {
                genes[g++] = weightsHiddenOutput[j][k];
            }
        }
        for (int k = 0; k < outputSize; k++) {
            genes[g++] = biasOutput[k];
        }
        return genes;
    }

    // File format: first line the sizes ("11 16 3"), second line all the genes.
    public void save(Path file) throws IOException {
        StringBuilder genes = new StringBuilder();
        for (double gene : getGenes()) {
            genes.append(gene).append(' ');
        }
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());   // create the models/ folder if it doesn't exist
        }
        Files.write(file, List.of(inputSize + " " + hiddenSize + " " + outputSize, genes.toString().trim()));
    }

    public static NeuralNetwork load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        String[] sizes = lines.get(0).trim().split(" ");
        String[] values = lines.get(1).trim().split(" ");
        double[] genes = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            genes[i] = Double.parseDouble(values[i]);
        }
        return new NeuralNetwork(Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]), Integer.parseInt(sizes[2]), genes);
    }
}
