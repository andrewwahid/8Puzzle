package puzzle.Controller;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import puzzle.Model.Game.Constants;
import puzzle.Model.Game.GameState;
import puzzle.Model.Solver.CostCalculator;
import puzzle.Model.Solver.Solver;

import java.util.*;

public class MainController {

    @FXML
    private Label errorLabel;
    @FXML
    private TextField stateTextField;
    @FXML
    private Button setBoardButton;
    @FXML
    private Button searchButton;
    @FXML
    private TextArea resultsTextArea;
    @FXML
    private ComboBox algorithmSelector;
    @FXML
    private VBox boardPane;
    private GridPane boardGrid;

    //
    private double playbackSpeed = 1;
    private Solver solver;
    private Queue<String> path;
    //
    public static GameState gameState;

    @FXML
    public void initialize() {
        algorithmSelector.getSelectionModel().select(0);
    }


    public boolean onSetBoardClick(ActionEvent actionEvent) {
        errorLabel.setText("");
        String inputState = stateTextField.getText();
        boolean valid = inputState.matches("^[0-8]{9}$");
        if (!valid){
            errorLabel.setText("Error: Wrong format! format: 0-8{9}");
            return false;
        }
        gameState = GameState.fromRawState(stateTextField.getText());
        renderBoard(gameState, false);
        return true;
    }

    public void renderBoard(GameState state, boolean callNextMove){
        HBox.setMargin(boardPane, new Insets(100, 0, 0, 150));
        int size = 100;
        boardPane.getChildren().clear();
        boardGrid = new GridPane();
        for (int i = 0; i < Constants.boardWidth; i++) {
            for (int k = 0; k < Constants.boardWidth; k ++) {
                StackPane square = new StackPane();
                Integer tileValue = state.tiles.get(i).get(k);
                if (tileValue != 0){
                    Label tileLabel = new Label(tileValue.toString());
                    tileLabel.setStyle("-fx-font: 75px Tahoma; -fx-font-weight: bold");
                    square.getChildren().add(tileLabel);
                }
                square.setStyle("-fx-background-color: white; -fx-border-width: 0.5px; -fx-border-color: green;");
                boardGrid.add(square, k, i);
            }
        }
        for (int i = 0; i < Constants.boardWidth; i++) {
            boardGrid.getColumnConstraints().add(new ColumnConstraints(size, size, size, Priority.ALWAYS, HPos.CENTER, true));
            boardGrid.getRowConstraints().add(new RowConstraints(size, size, size, Priority.ALWAYS, VPos.CENTER, true));
        }
        boardPane.getChildren().add(boardGrid);
        //
        HBox playbackPane = new HBox();
        Button slowdownButton = new Button("<<");
        Label currentSpeedLabel = new Label(Constants.df.format(playbackSpeed)+"x");
        Button speedupButton = new Button(">>");
        slowdownButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (playbackSpeed > 1){
                    playbackSpeed -= 0.25;
                    currentSpeedLabel.setText(Constants.df.format(playbackSpeed) + "x");
                }
            }
        });
        speedupButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (playbackSpeed < 4){
                    playbackSpeed += 0.25;
                    currentSpeedLabel.setText(Constants.df.format(playbackSpeed) + "x");
                }
            }
        });
        playbackPane.getChildren().addAll(slowdownButton, currentSpeedLabel, speedupButton);
        boardPane.getChildren().add(playbackPane);
        HBox.setMargin(currentSpeedLabel, new Insets(0, 10, 0, 10));
        VBox.setMargin(playbackPane, new Insets(50, 0, 0, 75));
        //
        if (callNextMove){
            nextMove(boardGrid);
        }
    }

    public void nextMove(GridPane gridPane){
        if (path.isEmpty()) return;
        int newMove = Constants.moveMapperReverse.get(path.remove());
        moveTile(gridPane, newMove);
    }

    public void moveTile(GridPane gridPane, int move) {
        int[] voidLocation = gameState.getVoidLocation();
        int[] targetLocation;
        if (move < 2){
            targetLocation = new int[]{voidLocation[0], voidLocation[1] + (move == 0 ? -1 : 1)};
        }else{
            targetLocation = new int[]{voidLocation[0] + (move == 2 ? -1 : 1), voidLocation[1]};
        }
        //
        Node voidNode = getNodeByRowColumnIndex(voidLocation[0], voidLocation[1], gridPane);
        Node targetNode = getNodeByRowColumnIndex(targetLocation[0], targetLocation[1], gridPane);
        //
        boolean vertical = move >= 2;
        boolean reverse = false;
        if (move == 0 || move == 2){
            reverse = true;
        }
        move(voidNode, reverse, vertical);
        move(targetNode, !reverse, vertical);
        String newState = Solver.swapString(gameState.rawState, (voidLocation[0] * Constants.boardHeight) + voidLocation[1], (targetLocation[0] * Constants.boardHeight) + targetLocation[1]);
        gameState = GameState.fromRawState(newState);
    }

    public void move(Node node, boolean reverse, boolean vertical)
    {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1000), node);
        //translateTransition.setInterpolator(Interpolator.LINEAR);
        int reverseInt = 1;
        if (reverse) reverseInt = -1;
        if (!vertical){
            translateTransition.setByX(0.1*1000 * reverseInt);
        }else{
            translateTransition.setByY(0.1*1000 * reverseInt);
        }
        translateTransition.setDuration(Duration.millis(1000/playbackSpeed));
        translateTransition.setCycleCount(1);
        translateTransition.play();
        if (reverse){
            translateTransition.setOnFinished(actionEvent -> {
                renderBoard(gameState, true);
            });
        }
    }

    public static Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        return gridPane.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == row)
                .filter(node -> GridPane.getColumnIndex(node) == column)
                .findFirst().get();
    }

    public void onSearchClick(ActionEvent actionEvent) {
        if (!onSetBoardClick(null)) return;
        errorLabel.setText("");
        solver = new Solver();
        if (gameState == null){
            errorLabel.setText("Please enter an initial state!");
            return;
        }
        if (!solver.isSolvable(gameState)){
            errorLabel.setText("This game state is not solvable!");
            return;
        }
        int searchingAlgorithm = algorithmSelector.getSelectionModel().getSelectedIndex();
        GameState resultState;
        double startTime = System.nanoTime();
        switch (searchingAlgorithm){
            case 0:
                resultState = solver.bfsSearch(gameState);
                break;
            case 1:
                resultState = solver.dfsSearch(gameState);
                break;
            case 2:
                resultState = solver.AStar(gameState, CostCalculator.manhattan);
                break;
            case 3:
                resultState = solver.AStar(gameState, CostCalculator.euclidean);
                break;
            default:
                errorLabel.setText("Searching algorithm not yet implemented!");
                return;
        }
        double elapsedTime = System.nanoTime() - startTime;
        if (resultState == null) {
            errorLabel.setText("Error occurred");
            return;
        }
        path = new LinkedList<String>(Solver.getPath(resultState));
        StringBuilder searchDescription = new StringBuilder();
        searchDescription.append("Path: ").append(path.toString()).append("\n");
        searchDescription.append("Cost: ").append(path.size()).append("\n");
        searchDescription.append("Nodes expanded: ").append(solver.nodesExpanded).append("\n");
        searchDescription.append("Maximum search depth: ").append(solver.maxSearchDepth).append("\n");
        searchDescription.append("Execution time: ").append(elapsedTime / 1000000000).append(" seconds\n");
        resultsTextArea.setText(searchDescription.toString());
        nextMove(boardGrid);
    }
}
