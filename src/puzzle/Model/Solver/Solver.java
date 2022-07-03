package puzzle.Model.Solver;

import puzzle.Model.Game.Constants;
import puzzle.Model.Game.GameState;

import java.util.*;

public class Solver {
    public int nodesExpanded = 0;
    public int maxSearchDepth = 0;

    private ArrayList<GameState> getChildStates(GameState state){
        ArrayList<GameState> result = new ArrayList<GameState>();
        nodesExpanded++;
        ArrayList<ArrayList<Integer>> adjacent = state.getAdjacentLocations();
        if (adjacent.isEmpty()) return result;
        for (ArrayList<Integer> location : adjacent) {
            GameState newState = swapAndCreate(state, (location.get(0) * Constants.boardHeight) + location.get(1), location.get(2));
            result.add(newState);
        }
        return result;
    }

    public GameState bfsSearch(GameState initialState){
        String goalState = "012345678";
        Set<String> explored = new HashSet<String>();
        Queue<GameState> frontier = new LinkedList<GameState>();
        frontier.add(initialState);
        while (!frontier.isEmpty()){
            GameState currentState = frontier.remove();
            explored.add(currentState.rawState);
            if (currentState.rawState.equals(goalState))
                return currentState;
            ArrayList<GameState> children = getChildStates(currentState);
            for (GameState child : children) {
                if (explored.contains(child.rawState)) continue;
                frontier.add(child);
                explored.add(child.rawState);
                if (child.depth > maxSearchDepth){
                    maxSearchDepth = child.depth;
                }
            }
        }
        return null;
    }

    public GameState dfsSearch(GameState initialState){
        String goalState = "012345678";
        Set<String> explored = new HashSet<String>();
        Stack<GameState> frontier = new Stack<>();
        frontier.push(initialState);
        while (!frontier.isEmpty()){
            GameState currentState = frontier.pop();
            explored.add(currentState.rawState);
            if (currentState.rawState.equals(goalState))
                return currentState;
            ArrayList<GameState> children = getChildStates(currentState);
            for (int i = children.size() - 1; i >= 0; i--){
                GameState child = children.get(i);
                if (explored.contains(child.rawState)) continue;
                frontier.push(child);
                explored.add(child.rawState);
                if (child.depth > maxSearchDepth){
                    maxSearchDepth = child.depth;
                }
            }
        }
        return null;
    }

    public GameState AStar(GameState initialState, CostCalculator costCalculator){
        nodesExpanded = 0;
        String goalState = "012345678";
        Set<String> explored = new HashSet<String>();
        PriorityQueue<GameState> frontier;
        frontier = new PriorityQueue<GameState>(Comparator.comparing( (o) -> o.calculateCost(costCalculator)));
        /*if (costCalculator == CostCalculator.manhattan){
            frontier = new PriorityQueue<GameState>(Comparator.comparing(GameState::manhattan));
        }else{
            frontier = new PriorityQueue<GameState>(Comparator.comparing(GameState::euclidean));
        }*/
        frontier.add(initialState);
        while (!frontier.isEmpty()){
            GameState currentState = frontier.remove();
            explored.add(currentState.rawState);
            if (currentState.rawState.equals(goalState))
                return currentState;
            ArrayList<GameState> children = getChildStates(currentState);
            for (GameState child : children) {
                if (explored.contains(child.rawState)) continue;
                frontier.add(child);
                if (child.depth > maxSearchDepth){
                    maxSearchDepth = child.depth;
                }
            }
        }
        return null;
    }

    public boolean isSolvable(GameState state){
        int inversions = 0;
        for (int i = 0; i < 9; i++){
            for (int j = i + 1; j < 9; j++){
                int item1 = (int) state.rawState.charAt(i) - '0';
                int item2 = (int) state.rawState.charAt(j) - '0';
                if (item1 != 0 && item2 != 0 && item1 > item2){
                    inversions++;
                }
            }
        }
        return (inversions % 2 == 0);
    }

    public static List<String> getPath(GameState endState){
        ArrayList<String> path = new ArrayList<String>();
        GameState currentState = endState;
        while (currentState.move != -1){
            path.add(Constants.moveMapper.get(currentState.move));
            if (currentState.parent == null){
                break;
            }else{
                currentState = currentState.parent;
            }
        }
        Collections.reverse(path);
        return path;
    }

    public static String swapString(String str, int x, int y)
    {
        if( x < 0 || x >= str.length() || y < 0 || y >= str.length())
            return str;
        char[] arr = str.toCharArray();
        char tmp = arr[x];
        arr[x] = arr[y];
        arr[y] = tmp;
        return new String(arr);
    }

    public static GameState swapAndCreate(GameState initialState, int location, int move){
        String state = initialState.rawState;
        String newRawState = swapString(state, state.indexOf('0'), location);
        GameState newGameState = GameState.fromRawState(newRawState);
        newGameState.depth = initialState.depth + 1;
        //
        newGameState.parent = initialState;
        newGameState.move = move;
        //
        return newGameState;
    }
}
