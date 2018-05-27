package dk.ruc.bachelor;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

/**
 * The primary function of the controller is to visualize the game and getting user input through the GUI
 */
public class Controller {

    //The logic used for anything game and training related
    Logic logic;

    //Animation timer used for the game loop
    AnimationTimer gameLoopTimer;

    //Fixed framerate
    int framesPerSecond = 60;

    //JavaFX elements
    public Canvas canvas;
    public Label labelTrained, labelSteps;
    public Button buttonTrain, buttonReset, buttonRun, buttonRandomMap;
    public RadioButton rbZero, rbRandom1, rbRandom2, rbRandom3, rbCustom1, rbCustom2, rbCustom3, rbCustom4, rbRandomLevels;
    public ToggleGroup tgMap = new ToggleGroup();
    public RadioButton rbTrain10, rbTrain100, rbTrain1000;
    public ToggleGroup tgTrainings = new ToggleGroup();

    //Booleans if the game is running or training
    boolean running = false;
    boolean training = false;

    //the current map
    int currentMap = 0;

    //Counter for training rounds done
    int trainingCount = 0;

    //Fixed number of training rounds each time an agent is trained
    int trainingRounds = 10;

    /**
     * Initializes everything and displays the game once started up
     */
    public void initialize() {

        //RadioButtons initialize
        rbZero.setToggleGroup(tgMap);
        rbRandom1.setToggleGroup(tgMap);
        rbRandom2.setToggleGroup(tgMap);
        rbRandom3.setToggleGroup(tgMap);
        rbCustom1.setToggleGroup(tgMap);
        rbCustom2.setToggleGroup(tgMap);
        rbCustom3.setToggleGroup(tgMap);
        rbCustom4.setToggleGroup(tgMap);
        rbRandomLevels.setToggleGroup(tgMap);
        rbZero.setSelected(true);

        rbZero.setOnAction(e -> changeMap(0));
        rbRandom1.setOnAction(e -> changeMap(1));
        rbRandom2.setOnAction(e -> changeMap(2));
        rbRandom3.setOnAction(e -> changeMap(3));
        rbCustom1.setOnAction(e -> changeMap(4));
        rbCustom2.setOnAction(e -> changeMap(5));
        rbCustom3.setOnAction(e -> changeMap(6));
        rbCustom4.setOnAction(e -> changeMap(7));
        rbRandomLevels.setOnAction(e -> changeMap(8));

        rbTrain10.setToggleGroup(tgTrainings);
        rbTrain100.setToggleGroup(tgTrainings);
        rbTrain1000.setToggleGroup(tgTrainings);
        rbTrain10.setSelected(true);

        rbTrain10.setOnAction(e -> trainingRounds = 10);
        rbTrain100.setOnAction(e -> trainingRounds = 100);
        rbTrain1000.setOnAction(e -> trainingRounds = 1000);

        buttonRandomMap.setDisable(true);

        //Logic object
        logic = new Logic(canvas.getWidth(), canvas.getHeight());

        //Display map
        display();

        //Set up game loop
        double framesPerNanoSecond = framesPerSecond * 1000000000;
        gameLoopTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= framesPerNanoSecond) {
                    gameLoop();
                    lastUpdate = now;
                }
            }
        };

        //Start the game loop
        gameLoopTimer.start();
    }

    /**
     * Sets the current map to the new map and displays it
     *
     * @param map New current map
     */
    void changeMap(int map) {
        currentMap = map;
        disableRandomMapButton();
        logic.setMap(map);
        display();
    }

    /**
     * Enables or disables the random map button depending on if the current map is random maps
     */
    void disableRandomMapButton() {
        if (currentMap == 8) buttonRandomMap.setDisable(false);
        else buttonRandomMap.setDisable(true);
    }

    /**
     * Disables or enables all radio buttons
     *
     * @param disable If true it disables the radio buttons and if false it enables them
     */
    void disableRadioButtons(boolean disable) {
        rbZero.setDisable(disable);
        rbRandom1.setDisable(disable);
        rbRandom2.setDisable(disable);
        rbRandom3.setDisable(disable);
        rbCustom1.setDisable(disable);
        rbCustom2.setDisable(disable);
        rbCustom3.setDisable(disable);
        rbCustom4.setDisable(disable);
        rbRandomLevels.setDisable(disable);

        rbTrain10.setDisable(disable);
        rbTrain100.setDisable(disable);
        rbTrain1000.setDisable(disable);
    }

    /**
     * When the train button is pressed it's beginning the training and stopping the training if it already trains
     */
    public void buttonTrain() {
        if (training == false) {
            training = true;
            buttonTrain.setText("Stop training");
            buttonRun.setDisable(true);
            buttonReset.setDisable(true);
            buttonRandomMap.setDisable(true);
            disableRadioButtons(true);
        } else if (training == true) {
            training = false;
            trainingCount = 0;
            labelTrained.setText("Training stopped");
            buttonTrain.setText("Train agent");
            buttonRun.setDisable(false);
            buttonReset.setDisable(false);
            disableRandomMapButton();
            disableRadioButtons(false);
        }
    }

    /**
     * Reset button resets everything back to start including all training done
     */
    public void buttonReset() {
        labelTrained.setText("Agent not trained");
        buttonTrain.setText("Train agent");
        buttonRun.setText("Play");
        buttonRun.setDisable(false);
        buttonTrain.setDisable(false);
        disableRadioButtons(false);
        logic.initializeTables();
        running = false;
        training = false;
        trainingCount = 0;
        changeMap(currentMap);
    }

    /**
     * Generate new map by setting the map to current map which is random maps. This will generate a new map
     */
    public void buttonRandomMap() {
        changeMap(currentMap);
    }

    /**
     * When the run button is pressed it starts to run and it stops if its currently running
     */
    public void buttonRun() {
        if (running) {
            buttonRun.setText("Play");
            running = false;
            buttonTrain.setDisable(false);
            buttonReset.setDisable(false);
            disableRandomMapButton();
            disableRadioButtons(false);
        } else {
            buttonTrain.setDisable(true);
            buttonReset.setDisable(true);
            buttonRandomMap.setDisable(true);
            disableRadioButtons(true);
            buttonRun.setText("Stop");
            running = true;
        }
    }

    /**
     * Called 60 frames per second either training or running on a map and displays the game
     */
    void gameLoop() {
        //If it's training
        if (training == true && trainingCount < trainingRounds) {
            labelTrained.setText("Training: " + trainingCount + " of " + trainingRounds);
            logic.trainAgent(currentMap);
            trainingCount++;
        }
        //If training finished
        else if (training == true) {
            training = false;
            trainingCount = 0;
            labelTrained.setText("Agent trained");
            buttonTrain.setText("Train agent");
            buttonRun.setDisable(false);
            buttonReset.setDisable(false);
            disableRandomMapButton();
            disableRadioButtons(false);
        }
        //If it should run
        if (running == true) {
            boolean reachedGoal = logic.doAction(false);
            labelSteps.setText("Steps: " + logic.savedMoves);
            display();
            if (reachedGoal) {
                buttonTrain.setDisable(false);
                buttonReset.setDisable(false);
                disableRandomMapButton();
                disableRadioButtons(false);
                running = false;
                buttonRun.setText("Play");
            }
        }
    }

    /**
     * Handles all display of the game
     */
    void display() {
        //Display of the game objects
        for (int i = 0; i < logic.columns; i++) {
            for (int j = 0; j < logic.rows; j++) {
                //Color of each game object
                if (logic.map[i][j] == 0) canvas.getGraphicsContext2D().setFill(Color.WHITE);
                else if (logic.map[i][j] == 1) canvas.getGraphicsContext2D().setFill(Color.GRAY);
                else if (logic.map[i][j] == 2) canvas.getGraphicsContext2D().setFill(Color.GREEN);
                else if (logic.map[i][j] == 3) canvas.getGraphicsContext2D().setFill(Color.BLUE);
                canvas.getGraphicsContext2D().fillRect(logic.cellSize * i, logic.cellSize * j, logic.cellSize, logic.cellSize);
                //Borders around game objects
                canvas.getGraphicsContext2D().setStroke(Color.BLACK);
                canvas.getGraphicsContext2D().strokeRect(logic.cellSize * i, logic.cellSize * j, logic.cellSize, logic.cellSize);
            }
        }
        //Vision for the agent
        canvas.getGraphicsContext2D().setStroke(Color.RED);
        canvas.getGraphicsContext2D().strokeRect(logic.cellSize * logic.agentX - logic.cellSize, logic.cellSize * logic.agentY - logic.cellSize, logic.cellSize * 3, logic.cellSize * 3);

        //Q-values for each action in the state
        canvas.getGraphicsContext2D().setFill(Color.BLACK);
        int state = logic.findStateId();
        double centerOfAgentX = logic.cellSize * logic.agentX + logic.cellSize / 2;
        double centerOfAgentY = logic.cellSize * logic.agentY + logic.cellSize / 2;
        canvas.getGraphicsContext2D().fillText("" + logic.Q[state][0], centerOfAgentX, centerOfAgentY - logic.cellSize);
        canvas.getGraphicsContext2D().fillText("" + logic.Q[state][1], centerOfAgentX - logic.cellSize, centerOfAgentY);
        canvas.getGraphicsContext2D().fillText("" + logic.Q[state][2], centerOfAgentX + logic.cellSize, centerOfAgentY);
        canvas.getGraphicsContext2D().fillText("" + logic.Q[state][3], centerOfAgentX, centerOfAgentY + logic.cellSize);

    }
}
