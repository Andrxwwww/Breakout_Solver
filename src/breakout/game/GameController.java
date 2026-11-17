package breakout.game;

// Anything that can play: the neural network, a simple rule, or random moves.
// It gets the game state and answers STAY, LEFT or RIGHT.
public interface GameController {

    int nextMove(int[] state);
}
