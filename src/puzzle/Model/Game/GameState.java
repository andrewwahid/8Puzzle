package puzzle.Model.Game;

import puzzle.Model.Solver.CostCalculator;

import java.util.ArrayList;
import java.util.Arrays;

public class GameState {
    public GameState parent;
    public int move = -1;
    public ArrayList<ArrayList<Integer>> tiles = new ArrayList<ArrayList<Integer>>();
    public String rawState = null;
    public int depth = 0;

    public ArrayList<ArrayList<Integer>> getAdjacentLocations(){
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < Constants.boardHeight; i++) {
            for (int k = 0; k < Constants.boardWidth; k++) {
                if (tiles.get(i).get(k) == 0){
                    /////////////// Left of tile
                    if (k > 0){
                        result.add(new ArrayList<Integer>(Arrays.asList(i, k-1, 0)));
                    }
                    /////////////// Right of tile
                    if (k < Constants.boardWidth - 1){
                        result.add(new ArrayList<Integer>(Arrays.asList(i, k+1, 1)));
                    }
                    /////////////// Upward of tile
                    if (i > 0){
                        result.add(new ArrayList<Integer>(Arrays.asList(i-1, k, 2)));
                    }
                    /////////////// Downward of tile
                    if (i < Constants.boardHeight - 1){
                        result.add(new ArrayList<Integer>(Arrays.asList(i+1, k, 3)));
                    }
                }
            }
        }
        return result;
    }

    public int[] getVoidLocation(){
        int[] result = new int[2];
        for (int i = 0; i < Constants.boardHeight; i++) {
            for (int k = 0; k < Constants.boardWidth; k++) {
                if (tiles.get(i).get(k) == 0) {
                    result = new int[]{i, k};
                }
            }
        }
        return result;
    }

    public String toRawState(){
        ArrayList<String> rows = new ArrayList<>();
        for (int i = 0; i < Constants.boardHeight; i++) {
            for (int k = 0; k < Constants.boardWidth; k++) {
                rows.add(tiles.get(i).get(k).toString());
            }
        }
        return String.join("", rows);
    }

    public static GameState fromRawState(String rawState){
        GameState gameState = new GameState();
        gameState.rawState = rawState;
        for (int i = 0; i < Constants.boardHeight; i++) {
            gameState.tiles.add(i, new ArrayList<Integer>());
            for (int k = 0; k < Constants.boardWidth; k++) {
                gameState.tiles.get(i).add(rawState.charAt(i * Constants.boardWidth + k) - '0');
            }
        }
        return gameState;
    }



    public float calculateCost(CostCalculator type){
        float h = 0;
        for (int i = 0; i < Constants.boardHeight * Constants.boardWidth; i++) {
            int item = rawState.charAt(i) - '0';
            int row = Math.floorDiv(i, Constants.boardWidth);
            int column = i %  Constants.boardHeight;
            int goalRow = Math.floorDiv(item, Constants.boardWidth);
            int goalColumn = item %  Constants.boardHeight;
            //
            if (type == CostCalculator.manhattan){
                h += Math.abs(row - goalRow) + Math.abs(column - goalColumn);
            }else{
                h += Math.sqrt(Math.pow(row - goalRow, 2) + Math.pow(column - goalColumn, 2));
            }
        }
        return depth + h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GameState)) {
            return false;
        }
        return ((GameState) o).rawState.compareTo(this.rawState) == 0;
    }

    /*public int manhattan(){
        int h = 0;
        for (int i = 0; i < Constants.boardHeight * Constants.boardWidth; i++) {
            int item = rawState.charAt(i) - '0';
            int row = Math.floorDiv(i, Constants.boardWidth);
            int column = i %  Constants.boardHeight;
            int goalRow = Math.floorDiv(item, Constants.boardWidth);
            int goalColumn = item %  Constants.boardHeight;
            //
            h += Math.abs(row - goalRow) + Math.abs(column - goalColumn);
        }
        cost = h;
        return depth + h;
    }

    public float euclidean(){
        float h = 0;
        for (int i = 0; i < Constants.boardHeight * Constants.boardWidth; i++) {
            int item = rawState.charAt(i) - '0';
            int previousRow = Math.floorDiv(i, Constants.boardWidth);
            int previousColumn = i % Constants.boardHeight;
            int goalRow = Math.floorDiv(item, Constants.boardWidth);
            int goalColumn = item % Constants.boardHeight;
            //
            h += Math.sqrt(Math.pow(previousRow - goalRow, 2) + Math.pow(previousColumn - goalColumn, 2));
        }
        cost = (int) h;
        return depth + h;
    }*/
}

