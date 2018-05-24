package dk.ruc.bachelor;

import java.util.Random;

import static java.lang.Math.pow;

/**
 * The logic class controls all logic related to the game and training of an agent
 */
class Logic {

    Random random = new Random();
    MapLibrary mapLibrary = new MapLibrary();

    double gameWidth;
    double gameHeight;
    double cellSize;

    int columns = 15;
    int rows = 15;

    int agentX, agentY;
    int previousAgentX, previousAgentY;

    double alpha = 0.8;
    double gamma = 0.8;

    int numberOfStates = (int) pow(3, 8); //6561
    int numberOfActions = 4; //up, down, left, right

    int state;
    int action;
    int oldState;
    int oldAction;

    int[][] Q, R, map;
    int selectedMap;

    int moves, savedMoves;

    /**
     * Constructor sets some variables depending of the width and height of the canvas
     *
     * @param gameWidthInput  Canvas width
     * @param gameHeightInput Canvas height
     */
    Logic(double gameWidthInput, double gameHeightInput) {
        gameWidth = gameWidthInput;
        gameHeight = gameHeightInput;
        if (gameWidth > gameHeight) cellSize = gameWidth / columns;
        else cellSize = gameHeight / rows;
        initialize();
    }

    /**
     * Sets the selected map, initializes it, sets agent position and initializes tables
     */
    void initialize() {
        selectedMap = 0;
        initializeMap();
        agentX = 1;
        agentY = 1;
        initializeTables();
    }

    /**
     * Initializes map according to what map is selected. Important notice, due to a bug that caused a lot of fields in random generated maps to become agents we found a workaround that resets all agent game objects to empty fields and then places an agent at the starting position
     */
    void initializeMap() {
        if (selectedMap == 0) map = mapLibrary.zero;
        else map = mapLibrary.randomMap(columns, rows, 5);

        //Workaround
        for (int i = 0; i < columns - 1; i++) {
            for (int j = 0; j < rows - 1; j++) {
                if (map[i][j] == 3) map[i][j] = 0;
            }

        }
        map[1][1] = 3;
    }

    /**
     * Initializes the tables and sets the values to 0
     */
    void initializeTables() {
        Q = new int[numberOfStates][numberOfActions];
        R = new int[numberOfStates][numberOfActions];

        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < numberOfActions; j++) {
                Q[i][j] = 0;
                R[i][j] = 0;
            }
        }
    }

    /** Sets the map and initializes it and the tables
     * @param map What the map should be set to
     */
    void setMap(int map) {
        agentX = 1;
        agentY = 1;
        moves = 0;
        selectedMap = map;
        initializeMap();
        initializeTables();
    }

    /**
     * Sets the map and runs until the agent reaches the goal.
     *
     * @param map What the map should be
     */
    void trainAgent(int map) {
        setMap(map);
        while (!move()) ;
    }

    /**
     * This function is the main part of the training. It decides the best action, then moves, updates Q-table and checks if it reached the goal
     *
     * @return returns true if it reached goal and false is not
     */
    boolean move() {
        state = findStateId(); //Find id of current position
        updateRTable(); //Update the R-table for this state
        action = decideAction();

        //Save the old state and positions
        oldState = state;
        oldAction = action;
        previousAgentX = agentX;
        previousAgentY = agentY;

        //Increase the number of moves and save the moves
        moves++;
        savedMoves = moves;

        //Move the agent, update the Q-table and check if it reached goal
        map[agentX][agentY] = 0; //Remove the agent from its current position
        moveAgent(action); //Move the agent according to the action found before
        updateQTable();
        if (map[agentX][agentY] == 2) {
            resetMap();
            return true; //If moving towards the goal the logic is won
        } else map[agentX][agentY] = 3;//Else it places the agent at the new position
        return false;
    }

    /**
     * Decides the best action from the current state
     * @return Returns the best action
     */
    int decideAction(){
        int bestAction = random.nextInt(4); //The action taken is random unless another actions Q-value is greater
        while (!validAction(bestAction)) bestAction = random.nextInt(4);

        //Find the action that has the greatest Q-value
        for (int i = 0; i < 4; i++) {
            if (R[state][i] + Q[state][i] > R[state][bestAction] + Q[state][bestAction]) {
                if (validAction(i)) bestAction = i;
            }
        }
        System.out.println(agentX+", "+agentY+", a: "+bestAction);
        return bestAction;
    }

    /**
     * Resets the positions of the agent and number of moves ready for a new training round
     */
    void resetMap() {
        map[previousAgentX][previousAgentY] = 0;
        agentX = 1;
        agentY = 1;
        map[agentX][agentY] = 3;
        moves = 0;
    }

    /**
     * Performs the movement according to the action given
     *
     * @param action Is the action decided for the agent
     */
    void moveAgent(int action) {
        if (action == 0) agentY -= 1;
        if (action == 1) agentX -= 1;
        if (action == 2) agentX += 1;
        if (action == 3) agentY += 1;
    }

    /**
     * Check if action is valid. If the action points towards a wall it is invalid
     *
     * @param action what action thats' being checked
     * @return returns true if its valid and false if invalid
     */
    boolean validAction(int action) {
        if (action == 0 && map[agentX][agentY - 1] == 1) return false;
        if (action == 1 && map[agentX - 1][agentY] == 1) return false;
        if (action == 2 && map[agentX + 1][agentY] == 1) return false;
        if (action == 3 && map[agentX][agentY + 1] == 1) return false;
        return true;
    }

    /**
     * Change R-table according to goals from the agents position and state
     */
    void updateRTable() {
        //
        if (map[agentX][agentY - 1] == 2) R[state][0] = 10000000;
        if (map[agentX - 1][agentY] == 2) R[state][1] = 10000000;
        if (map[agentX + 1][agentY] == 2) R[state][2] = 10000000;
        if (map[agentX][agentY + 1] == 2) R[state][3] = 10000000;
    }

    /**
     * Determines the id of the state the agent is in
     *
     * @return Returns the id of the state
     */
    int findStateId() {
        System.out.println(agentX+", "+agentY);
        int stateId = 0;
        int divider = 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != 1 || j != 1) {
                    stateId += numberOfStates / divider * map[agentX + i - 1][agentY + j - 1];
                    divider *= 3;
                }
            }
        }
        return stateId;
    }

    /**
     * Updates the Q-table
     */
    void updateQTable() {
        //The states and actions
        state = findStateId();
        action = maxQ();

        //The Q-table update itself
        Q[oldState][oldAction] = (int) ((1 - alpha) * Q[oldState][oldAction] + alpha * (R[state][action] + gamma * Q[state][action]));
    }

    /**
     * Finds the highest Q-value of the actions available
     *
     * @return Returns the maximum Q-value between the actions of the current state
     */
    int maxQ() {
        double maxQ = 0;
        int action = 0;
        for (int i = 0; i < numberOfActions; i++) {
            if (Q[state][i] + R[state][i] >= maxQ) {
                maxQ = Q[state][i] + R[state][i];
                action = i;
            }
        }
        return action;
    }
}