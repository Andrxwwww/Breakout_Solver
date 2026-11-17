# Breakout AI

A small neural network that learns to play Breakout. No libraries, just plain Java.

There's no training data here. The network never gets told which move is right. It just plays, gets a score at the end, and a genetic algorithm keeps the better networks and mixes them into new ones. After a few hundred generations it plays pretty well.

## How good is it?

I tested it on 500 games it had never seen:

| Player | Bricks per game |
|---|---|
| **My network** | **11.4** |
| Simple rule (follow the ball) | 5.8 |
| Random moves | 0.5 |

So it scores about 2x the simple rule, and it never lost the ball in those 500 games.

## Project structure

```
breakout-new/
├── src/
│   ├── breakout/
│   │   ├── MainBreakout.java          start here: play, train or test
│   │   │
│   │   ├── game/                      the game itself (no AI, no window)
│   │   │   ├── BreakoutBoard.java     the rules: moving, bouncing, scoring
│   │   │   ├── Ball.java
│   │   │   ├── Paddle.java
│   │   │   ├── Brick.java
│   │   │   ├── Sprite.java            what ball, paddle and brick have in common
│   │   │   ├── Commons.java           sizes and positions
│   │   │   └── GameController.java    anything that can play (network, rule, random)
│   │   │
│   │   ├── ai/                        the neural network and its training
│   │   │   ├── NeuralNetwork.java
│   │   │   ├── NNBreakoutGameController.java   turns the game into network inputs, picks a move
│   │   │   └── BreakoutGeneticAlgorithm.java   the training
│   │   │
│   │   └── gui/
│   │       └── Breakout.java          the window (it only draws the board)
│   │
│   └── resources/                     the images
│
├── models/best.txt                    the trained network, so you don't have to train again
└── portfolio/index.html               a small web page where the network plays in the browser
```

The idea: `game` doesn't know about the AI or the window, so the same game runs fast during training and is drawn on screen when you watch.

## Running it

Java 17+. Open a terminal **in the `breakout-new` folder** (the one with `src` and `models` in it):

```bash
javac -d out src/breakout/*.java src/breakout/*/*.java

java -cp out breakout.MainBreakout          # watch it play
java -cp out breakout.MainBreakout test     # run the test above
java -cp out breakout.MainBreakout train    # train a new one
```

The first line compiles everything into an `out` folder. You only need to run it again after you change the code.

In Eclipse or IntelliJ: mark `src` as the source folder, run `MainBreakout`, and keep the project folder as the working directory so it finds `models/` and `src/resources/`. To train, put `train` in the program arguments.

## Training it yourself

1. **Back up `models/best.txt` first** if you want to keep the current network. Training replaces it.
2. Run `java -cp out breakout.MainBreakout train`. It takes 45 to 60 minutes and uses all your CPU cores.
3. Every 10 generations it prints a line. The number to watch is "bricks per game on validation": it starts around 0.5, and the current network ended around 11.
4. At the end it saves the new network and tests it automatically.

All the training settings are constants at the top of `ai/BreakoutGeneticAlgorithm.java`. For a quick try, lower `GENERATIONS`.

## How it works

1. **The game** gives the network a few numbers: where the ball is, where it's going, where the paddle and the brick are. I also tell it where the ball is going to land, because that makes learning a lot faster.
2. **The network** is small: 11 inputs, 16 hidden neurons, 3 outputs (stay, left, right). It picks the biggest output.
3. **Training:** 100 networks each play 32 random games. The best ones survive, the rest are replaced by mixed and slightly mutated copies of the good ones. That repeats 400 times.

The trick to scoring is aiming: where the ball hits the paddle decides where it flies next. The network learned to use that to hit the brick.
