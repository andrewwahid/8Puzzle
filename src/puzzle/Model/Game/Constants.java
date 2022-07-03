package puzzle.Model.Game;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Constants {
    public static int boardWidth = 3;
    public static int boardHeight = 3;

    public static final DecimalFormat df = new DecimalFormat("0.00");

    public static HashMap<Integer, String> moveMapper = new HashMap<Integer, String>() {{
        put(0, "Left");
        put(1, "Right");
        put(2, "Up");
        put(3, "Down");
    }};

    public static HashMap<String, Integer> moveMapperReverse = new HashMap<String, Integer>() {{
        put("Left", 0);
        put("Right", 1);
        put("Up", 2);
        put("Down", 3);
    }};
}
