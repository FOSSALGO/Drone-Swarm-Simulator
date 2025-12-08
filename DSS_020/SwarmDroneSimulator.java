package frontier.based.swarmdronesimulator.autonomousexplorationofswarmdrone;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SwarmDroneSimulator extends Application {

    // Path SVG Icon
    private final String svgNewEnv = "M4 20V4h6.615v1H5v14h14v-5.615h1V20H4Zm12-9V8h-3V7h3V4h1v3h3v1h-3v3h-1Z";
    private final String svgOpenEnv = "M5.615 20q-.69 0-1.152-.462Q4 19.075 4 18.385V5.615q0-.69.463-1.152Q4.925 4 5.615 4h12.77q.69 0 1.152.463q.463.462.463 1.152v5.5q0 .214-.143.357q-.144.143-.357.143t-.357-.143Q19 11.33 19 11.115v-5.5q0-.23-.192-.423Q18.615 5 18.385 5H5.615q-.23 0-.423.192Q5 5.385 5 5.615v12.77q0 .23.192.423q.193.192.423.192h5.5q.214 0 .357.143q.143.144.143.357t-.143.357q-.143.143-.357.143h-5.5Zm12.697-1l-8.889-8.87q-.14-.14-.143-.35q-.003-.21.143-.357q.14-.14.354-.14q.213 0 .354.14L19 18.287V14.5q0-.213.143-.357T19.5 14q.213 0 .357.143q.143.144.143.357v4.692q0 .348-.23.578q-.23.23-.578.23H14.5q-.213 0-.357-.143Q14 19.713 14 19.5t.143-.357T14.5 19h3.812Z";
    private final String svgSaveEnv = "M20 7.423V20H4V4h12.577L20 7.423Zm-1 .427L16.15 5H5v14h14V7.85Zm-7.005 8.688q.832 0 1.418-.582q.587-.582.587-1.413q0-.831-.582-1.418t-1.413-.587t-1.418.582Q10 13.703 10 14.534t.582 1.418q.582.586 1.413.586ZM6.77 9.77h7.423v-3H6.77v3ZM5 7.85V19V5v2.85Z";
    private final String svgTransform = "M7.692 16.308V7.692h8.616v8.616H7.692Zm1-1h6.616V8.692H8.692v6.616Zm-1 4.692v-1.23h1.231V20h-1.23ZM4 5.23V4h1.23v1.23H4Zm3.692 0V4h1.231v1.23h-1.23ZM11.385 20v-1.23h1.23V20h-1.23Zm0-14.77V4h1.23v1.23h-1.23Zm3.692 0V4h1.23v1.23h-1.23Zm0 14.77v-1.23h1.23V20h-1.23Zm3.692-14.77V4H20v1.23h-1.23ZM4 20v-1.23h1.23V20H4Zm0-3.692v-1.231h1.23v1.23H4Zm0-3.693v-1.23h1.23v1.23H4Zm0-3.692v-1.23h1.23v1.23H4ZM18.77 20v-1.23H20V20h-1.23Zm0-3.692v-1.231H20v1.23h-1.23Zm0-3.693v-1.23H20v1.23h-1.23Zm0-3.692v-1.23H20v1.23h-1.23Z";
    private final String svgDrone = "M6.784 21q-1.588 0-2.686-1.118T3 17.182t1.099-2.68t2.684-1.098q.608 0 1.158.173q.551.173 1.001.494q.793-1.013.827-2.105q.035-1.093-.727-2.087q-.475.346-1.038.538t-1.196.192q-1.587 0-2.697-1.11T3 6.802t1.11-2.694T6.809 3t2.697 1.105t1.11 2.697q0 .633-.204 1.196q-.205.563-.551 1.038q.994.762 2.096.73t2.115-.805q-.321-.45-.504-1t-.182-1.159q0-1.585 1.103-2.684t2.694-1.099t2.705 1.1T21 6.802t-1.117 2.699t-2.696 1.114q-.656 0-1.243-.208q-.586-.208-1.067-.598q-.787 1.019-.752 2.152t.846 2.179q.475-.347 1.029-.542t1.186-.195q1.58 0 2.697 1.099Q21 15.603 21 17.188t-1.118 2.699t-2.7 1.113t-2.69-1.117t-1.107-2.696q0-.633.195-1.197q.195-.563.541-1.038q-1.044-.812-2.158-.84t-2.153.778q.39.481.598 1.06t.208 1.237q0 1.58-1.122 2.696T6.784 21M17.187 9.615q1.17 0 1.991-.82q.822-.821.822-1.99q0-1.168-.82-1.977q-.821-.809-1.99-.809t-1.987.809t-.819 1.974q0 .43.115.816t.337.701l2.2-2.18l.67.669l-2.192 2.236q.34.279.77.425t.903.147M6.807 9.61q.443 0 .841-.146q.398-.147.733-.376L6.1 6.809l.708-.689L9.094 8.4q.248-.334.385-.748t.137-.85q0-1.165-.822-1.986q-.821-.822-1.986-.822t-1.987.821Q4 5.638 4 6.803t.821 1.987q.821.82 1.987.82M17.19 20q1.168 0 1.99-.82t.82-1.99t-.822-1.978t-1.992-.808q-.442 0-.84.134q-.398.133-.733.362l2.312 2.312l-.733.688l-2.286-2.286q-.229.334-.375.732t-.146.84q0 1.17.818 1.992T17.19 20m-10.4 0q1.175 0 2-.822q.824-.822.824-1.992q0-.467-.146-.893t-.425-.766L6.633 17.9l-.714-.688l2.356-2.356q-.315-.223-.688-.338t-.804-.114q-1.166 0-1.974.809Q4 16.02 4 17.19q0 1.168.809 1.99q.808.82 1.983.82m5.207-7.25q.31 0 .521-.21q.21-.21.21-.52t-.21-.521q-.209-.21-.52-.21t-.52.21t-.21.52t.21.52q.209.211.52.211";
    private final String svgWall = "M4.19 19.81v-.708L19.102 4.19h.713v.708L4.898 19.81H4.19ZM4 13.527v-1.415L12.112 4h1.415L4 13.527Zm0-7.18V4h2.346L4 6.346ZM17.654 20L20 17.654V20h-2.346Zm-7.18 0L20 10.473v1.415L11.888 20h-1.415Z";
    private final String svgDrawLine = "M4 20V4h16v16H4Zm15-1v-6.5h-6.5V19H19Zm0-14h-6.5v6.5H19V5ZM5 5v6.5h6.5V5H5Zm0 14h6.5v-6.5H5V19Z";
    private final String svgRemoveLine = "M4 20v-1.23h1.23V20H4Zm0-3.692v-1.231h1.23v1.23H4Zm0-3.693v-1.23h1.23v1.23H4Zm0-3.692v-1.23h1.23v1.23H4Zm0-3.692V4h1.23v1.23H4ZM7.692 20v-1.23h1.231V20h-1.23Zm0-7.385v-1.23h1.231v1.23h-1.23Zm0-7.384V4h1.231v1.23h-1.23ZM11.385 20v-1.23h1.23V20h-1.23Zm0-3.692v-1.231h1.23v1.23h-1.23Zm0-3.693v-1.23h1.23v1.23h-1.23Zm0-3.692v-1.23h1.23v1.23h-1.23Zm0-3.692V4h1.23v1.23h-1.23ZM15.077 20v-1.23h1.23V20h-1.23Zm0-7.385v-1.23h1.23v1.23h-1.23Zm0-7.384V4h1.23v1.23h-1.23ZM18.769 20v-1.23H20V20h-1.23Zm0-3.692v-1.231H20v1.23h-1.23Zm0-3.693v-1.23H20v1.23h-1.23Zm0-3.692v-1.23H20v1.23h-1.23Zm0-3.692V4H20v1.23h-1.23Z";
    private final String svgRun = "M13.115 17.885v-3.498l-1.317-1.437l-.544 2.765l-3.52-.736l.162-.744l2.775.575l1.033-5.208l-1.82.671v1.612h-.769V9.727l3.198-1.162q.336-.125.66.006t.487.469q.58 1.23 1.314 1.633q.734.404 1.11.442v.77q-.524-.039-1.326-.5t-1.516-1.423l-.482 2.676l1.325 1.425v3.822h-.77Zm.385-10q-.376 0-.63-.255q-.255-.254-.255-.63t.255-.63q.254-.255.63-.255t.63.255q.255.254.255.63t-.255.63q-.254.255-.63.255ZM12.003 21q-1.866 0-3.51-.708q-1.643-.709-2.859-1.924q-1.216-1.214-1.925-2.856Q3 13.87 3 12.003q0-1.866.708-3.51q.709-1.643 1.924-2.859q1.214-1.216 2.856-1.925Q10.13 3 11.997 3q1.866 0 3.51.708q1.643.709 2.859 1.924q1.216 1.214 1.925 2.856Q21 10.13 21 11.997q0 1.866-.708 3.51q-.709 1.643-1.924 2.859q-1.214 1.216-2.856 1.925Q13.87 21 12.003 21ZM12 20q3.35 0 5.675-2.325T20 12q0-3.35-2.325-5.675T12 4Q8.65 4 6.325 6.325T4 12q0 3.35 2.325 5.675T12 20Z";
    private final String svgPlayPause = "M9.808 15.5h1v-7h-1v7Zm3.384 0h1v-7h-1v7ZM12.003 21q-1.866 0-3.51-.708q-1.643-.709-2.859-1.924q-1.216-1.214-1.925-2.856Q3 13.87 3 12.003q0-1.866.708-3.51q.709-1.643 1.924-2.859q1.214-1.216 2.856-1.925Q10.13 3 11.997 3q1.866 0 3.51.708q1.643.709 2.859 1.924q1.216 1.214 1.925 2.856Q21 10.13 21 11.997q0 1.866-.708 3.51q-.709 1.643-1.924 2.859q-1.214 1.216-2.856 1.925Q13.87 21 12.003 21ZM12 20q3.35 0 5.675-2.325T20 12q0-3.35-2.325-5.675T12 4Q8.65 4 6.325 6.325T4 12q0 3.35 2.325 5.675T12 20Zm0-8Z";
    private final String svgReplay = "M10.385 15.23L15.23 12l-4.846-3.23zM12 21q-1.864 0-3.506-.701t-2.857-1.916t-1.926-2.849Q3 13.902 3 12.04q0-.902.167-1.776t.497-1.715l.78.78q-.219.65-.331 1.317T4 12q0 3.35 2.325 5.675T12 20t5.675-2.325T20 12t-2.325-5.675T12 4q-.675 0-1.332.112t-1.3.332l-.776-.775q.789-.315 1.606-.492T11.885 3q1.882 0 3.544.701t2.896 1.926t1.955 2.867T21 12t-.71 3.506q-.711 1.642-1.926 2.857q-1.216 1.216-2.858 1.926Q13.864 21 12 21M5.923 6.808q-.356 0-.62-.265q-.264-.264-.264-.62t.264-.62t.62-.264t.62.264t.265.62t-.265.62t-.62.265M12 12";

    private final String delimiter = ":";// FILE

    // Button
    private final Button btnNew = createIconButton(svgNewEnv, 1.0, 1.0, "New Environment");// VISUALIZATION
    private final Button btnOpen = createIconButton(svgOpenEnv, 1.0, 1.0, "Open Environment");// VISUALIZATION
    private final Button btnSave = createIconButton(svgSaveEnv, 1.0, 1.0, "Save Environment");// VISUALIZATION
    private final Button btnTransform = createIconButton(svgTransform, 1.0, 1.0, "Transform");// VISUALIZATION
    private final Button btnDrone = createIconButton(svgDrone, 1.0, 1.0, "Drone");// VISUALIZATION
    private final Button btnWall = createIconButton(svgWall, 1.0, 1.0, "Wall");// VISUALIZATION
    private final Button btnDrawLine = createIconButton(svgDrawLine, 1.0, 1.0, "Draw Line");// VISUALIZATION
    private final Button btnRemoveLine = createIconButton(svgRemoveLine, 1.0, 1.0, "Remove Line");// VISUALIZATION
    private final Button btnRun = createIconButton(svgRun, 1.0, 1.0, "Run");// VISUALIZATION
    private final Button btnPlayPause = createIconButton(svgPlayPause, 1.0, 1.0, "Play - Pause");// VISUALIZATION
    private final Button btnReplay = createIconButton(svgReplay, 1.0, 1.0, "Replay");// VISUALIZATION

    private final int sensoreFoV = 5; // DRONES, satuan: cell
    private final int cameraFoV = 4;// DRONES, satuan: cell
    private final int droneRadius = 1;// DRONES
    private final int[][] neighbors8 = {// DRONES
            {0, 1},/*east*/
            {1, 1},/*southeast*/
            {1, 0},/*south*/
            {1, -1},/*southwest*/
            {0, -1},/*west*/
            {-1, -1},/*northwest*/
            {-1, 0},/*north*/
            {-1, 1},/*northeast*/
    };// DRONES

    private final HashSet<String> inputKeyboard = new HashSet<>();// HANDLER
    private final int cellSize = 10;// GRID
    private final double halfCellSize = 0.5 * cellSize;// GRID
    private final int MAX_FRAME_PERIOD = 2;
    private final Color[] pathColor = {
            Color.valueOf("#FF0B55"),
            Color.valueOf("#9BEC00"),
            Color.valueOf("#00F5FF"),
            Color.valueOf("#FCE700"),
            Color.valueOf("#AA2EE6"),
            Color.valueOf("#0046FF")
    };

    private final int MAX_NUM_ROWS = 200;// GRID
    private final int MAX_NUM_COLS = 400;// GRID
    private final String help = "SHORTCUT:\n" +
            "[CTRL] [F] = Free transform\n" +
            "[CTRL] [0] = Reset Canvas Size\n" +
            "[CTRL] [-] = Zoom Out\n" +
            "[CTRL] [+] = Zoom In\n" +
            "[CTRL] [SPACE] = Run Algorithm\n" +
            "[CTRL] [ENTER] = Reset Environment\n" +
            "[CTRL] [O] = Open File\n" +
            "[CTRL] [S] = Save Environment\n" +
            "[P] = Play-Pause\n" +
            "[R] = Replay\n" +
            "[W] + Mouse Click = Create/Remove Wall\n" +
            "[L] + Mouse Drag = Create Line\n" +
            "[K] + Mouse Drag = Remove Line\n" +
            "[Digit 1] + Mouse Drag = Move Drone-1 (state = DEFAULT)\n" +
            "[Digit 2] + Mouse Drag = Move Drone-2 (state = DEFAULT)\n" +
            "[Digit 3] + Mouse Drag = Move Drone-3 (state = DEFAULT)\n" +
            "[Digit 4] + Mouse Drag = Move Drone-4 (state = DEFAULT)\n" +
            "[Digit 5] + Mouse Drag = Move Drone-5 (state = DEFAULT)\n" +
            "[Digit 6] + Mouse Drag = Move Drone-6 (state = DEFAULT)\n\n";
    private Stage primaryStage = null;
    private FileChooser fileChooser = new FileChooser();// VISUALIZATION
    private int numberOfDrones = 6;// DRONES
    private int numRows = MAX_NUM_ROWS;// GRID
    private int numCols = MAX_NUM_COLS;// GRID
    private int[][] environment = new int[numRows][numCols];// GRID
    private String[][] wallType = new String[numRows][numCols];// GRID
    private int[][] darkScreen = new int[numRows][numCols];// GRID
    private int[][] darkScreenCopy = new int[numRows][numCols];// GRID
    private ArrayList<Drone> drones = new ArrayList<>();// DRONES
    private ArrayList<Drone> activeDrones = new ArrayList<>();// DRONES
    private ArrayList<Point> lastPositionOfActiveDrones = new ArrayList<>();// DRONES
    private Canvas canvas;// VISUALIZATION
    private GraphicsContext gc;// VISUALIZATION
    private double mouseAnchorX;// VISUALIZATION
    private double mouseAnchorY;// VISUALIZATION
    private double translateX = 0;// VISUALIZATION
    private double translateY = 0;// VISUALIZATION
    private double scale = 1.0;// VISUALIZATION
    private boolean visualized = false;// VISUALIZATION
    private boolean pause = false;// VISUALIZATION
    private ProgressIndicator progressIndicator;// VISUALIZATION
    private StringBuffer logArea;// VISUALIZATION
    private Label statusLabel;// VISUALIZATION
    private Label statelabel;// VISUALIZATION
    private double offsetX = 0;
    private double offsetY = 0;// VISUALIZATION
    private boolean isMaximized = true;// VISUALIZATION
    private Screen currentScreen;// VISUALIZATION
    private State state = State.DEFAULT;
    private int moveToI = -1;// LINE
    private int moveToJ = -1;// LINE
    private int lineToI = -1;// LINE
    private int lineToJ = -1;// LINE
    private int positionI = -1;// LINE
    private int positionJ = -1;// LINE
    private int MIN_ROW, MIN_COL, MAX_ROW, MAX_COL;// EXPLORATION
    private int[][] map = null;// EXPLORATION
    private int[][] base_map = null;// EXPLORATION
    private int explored = 0;// EXPLORATION
    private int mustBeExplored = 0;// EXPLORATION
    private ArrayList<Point>[] frontiers = null;// EXPLORATION
    private boolean running = false;// EXPLORATION - run the algorithm to explore
    private int frame = -1;// FRAME
    private int framePeriod = 0;
    private ArrayList<Point>[] pathOfDrones = null;

    public static void main(String[] args) {
        launch();
    }

    private String checkWallType(int i, int j) {
        String type = null;
        if (environment != null && i >= 0 && i < environment.length && j >= 0 && j < environment[0].length && environment[i][j] == -1) {
            type = "";

            // check EAST
            String value = "0";
            if (j == environment[i].length - 1 || (j < environment[i].length - 1 && environment[i][j + 1] == 0)) {
                value = "1";
            }
            type += value;

            // check SOUTH
            value = "0";
            if (i == environment.length - 1 || (i < environment.length - 1 && environment[i + 1][j] == 0)) {
                value = "1";
            }
            type += value;

            // check WEST
            value = "0";
            if (j == 0 || (j > 0 && environment[i][j - 1] == 0)) {
                value = "1";
            }
            type += value;

            // check NORTH
            value = "0";
            if (i == 0 || (i > 0 && environment[i - 1][j] == 0)) {
                value = "1";
            }
            type += value;
        }
        return type;
    }

    private void checkWallType() {
        if (environment != null) {
            wallType = new String[environment.length][];
            for (int i = 0; i < environment.length; i++) {
                wallType[i] = new String[environment[i].length];
                for (int j = 0; j < environment[i].length; j++) {
                    String type = checkWallType(i, j);
                    wallType[i][j] = type;
                }
            }
        }
    }

    private void writeToFile(File file) {
        if (environment != null) {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("ENVIRONMENT----------------------------------------------------------------------------------\n");
                sb.append("NUM ROWS" + delimiter + environment.length + "\n");
                sb.append("NUM COLS" + delimiter + environment[0].length + "\n");
                for (int i = 0; i < environment.length; i++) {
                    for (int j = 0; j < environment[i].length; j++) {
                        sb.append(environment[i][j] + delimiter);
                    }
                    sb.append("\n");
                }

                if (drones != null && !drones.isEmpty()) {
                    sb.append("DRONES---------------------------------------------------------------------------------------\n");
                    sb.append("NUM DRONES" + delimiter + drones.size() + "\n");
                    for (int d = 0; d < drones.size(); d++) {
                        sb.append(drones.get(d).indexI + delimiter + drones.get(d).indexJ + "\n");
                    }
                }

                String data = sb.toString();
                Path filePath = file.toPath();
                Files.write(filePath, data.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readFromFile(File file) {
        try {
            Path filePath = file.toPath();
            BufferedReader reader = Files.newBufferedReader(filePath);
            String line = reader.readLine();//lompati baris pertama
            line = reader.readLine();
            String[] values = line.split(delimiter);
            int r = Integer.parseInt(values[1]);

            line = reader.readLine();
            values = line.split(delimiter);
            int c = Integer.parseInt(values[1]);
            if (r > 0 && c > 0) {
                translateX = 0;
                translateY = 0;
                scale = 1;
                numRows = r;// GRID
                numCols = c;// GRID
                canvas.setWidth(numCols * cellSize);// VISUALIZATION
                canvas.setHeight(numRows * cellSize);// VISUALIZATION
                environment = new int[numRows][numCols];// GRID
                wallType = new String[numRows][numCols];// GRID
                pathOfDrones = null;
                frame = -1;
                darkScreen = new int[numRows][numCols];// GRID
                darkScreenCopy = new int[numRows][numCols];// GRID
                for (int i = 0; i < environment.length; i++) {
                    line = reader.readLine();
                    values = line.split(delimiter);
                    if (values.length == environment[i].length) {
                        for (int j = 0; j < environment[i].length; j++) {
                            environment[i][j] = Integer.parseInt(values[j]);
                        }
                    }
                }
                checkWallType();

                // read drone
                line = reader.readLine();//lompati satu baris
                line = reader.readLine();
                values = line.split(delimiter);
                int nDrone = Integer.parseInt(values[1]);
                if (nDrone > 0) {
                    numberOfDrones = nDrone;
                    drones = new ArrayList<>();// DRONES
                    activeDrones = new ArrayList<>();// DRONES
                    lastPositionOfActiveDrones = new ArrayList<>();// DRONES

                    for (int d = 0; d < numberOfDrones; d++) {
                        line = reader.readLine();
                        values = line.split(delimiter);
                        int i = Integer.parseInt(values[0]);
                        int j = Integer.parseInt(values[1]);
                        Drone drone = new Drone(i, j, sensoreFoV, cameraFoV, cellSize);
                        drone.setName("D" + (1 + d));
                        drones.add(drone);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void resetScale() {
        scale = 1.0;
        render();
    }

    private void resetEnvironment() {
        numRows = MAX_NUM_ROWS;// GRID
        numCols = MAX_NUM_COLS;// GRID
        translateX = 0;
        translateY = 0;
        scale = 1;
        canvas.setWidth(numCols * cellSize);// VISUALIZATION
        canvas.setHeight(numRows * cellSize);// VISUALIZATION
        environment = new int[numRows][numCols];// GRID
        wallType = new String[numRows][numCols];// GRID
        darkScreen = new int[numRows][numCols];// GRID
        darkScreenCopy = new int[numRows][numCols];// GRID
        initializeDrones(numberOfDrones);// initialize drone
        state = State.DEFAULT;
        pathOfDrones = null;
        frame = -1;
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
        render();
    }

    private void handleKeyPressed(KeyEvent e) {
        String code = e.getCode().toString();
        inputKeyboard.add(code);
        // System.out.println(inputKeyboard);
    }

    private void handleKeyReleased(KeyEvent e) {
        String code = e.getCode().toString();
        inputKeyboard.remove(code);
    }

    private void handleMouseReleased(MouseEvent mouseEvent) {
        if (moveToI != -1 && moveToJ != -1 && lineToI != -1 && lineToJ != -1) {
            double cSize = cellSize * scale;
            double hcSize = 0.5 * cSize;
            double x0 = moveToJ * cSize + hcSize;
            double y0 = moveToI * cSize + hcSize;
            double x1 = lineToJ * cSize + hcSize;
            double y1 = lineToI * cSize + hcSize;

            double dx = (x1 - x0) / cSize;
            double dy = (y1 - y0) / cSize;

            double x = x0;
            double y = y0;

            double minX = Math.min(x0, x1);
            double minY = Math.min(y0, y1);
            double maxX = Math.max(x0, x1);
            double maxY = Math.max(y0, y1);

            double step = 0.001;//epsilon

            while (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
                    if (inputKeyboard.contains("L") || state.equals(State.LINE)) {
                        environment[row][col] = -1;
                    } else if (inputKeyboard.contains("K") || state.equals(State.REMOVE_LINE)) {
                        environment[row][col] = 0;
                    }
                }
                x += dx * step;
                y += dy * step;
            }
            checkWallType();
        }
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
        render();
    }

    private void handleMouseClicked(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        double cSize = cellSize * scale;
        if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
            int i = (int) Math.floor((y - translateY) / cSize);
            int j = (int) Math.floor((x - translateX) / cSize);

            if (inputKeyboard.contains("W") || state.equals(State.OBSTACLE)) {
                if (environment[i][j] == 0) {
                    environment[i][j] = -1;
                } else {
                    environment[i][j] = 0;
                }
            }

            checkWallType();
            render();
        }
    }

    private void handleMouseScroll(ScrollEvent scrollEvent) {
        double zoomFactor = 1.05;
        double oldScale = scale;

        if (scrollEvent.getDeltaY() > 0) {
            scale *= zoomFactor;
        } else {
            scale /= zoomFactor;
        }

        // Zoom terfokus pada posisi mouse
        double mouseX = scrollEvent.getX();
        double mouseY = scrollEvent.getY();

        // Penyesuaian translasi agar zoom fokus ke pointer
        translateX = mouseX - (mouseX - translateX) * (scale / oldScale);
        translateY = mouseY - (mouseY - translateY) * (scale / oldScale);

        render();
    }

    private void handleMouseDragged(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        double cSize = cellSize * scale;
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K") || state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
            if (moveToI != -1 && moveToJ != -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                lineToI = i;
                lineToJ = j;
                positionI = i;
                positionJ = j;
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT1")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(0).setIndex(i, j);
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT2")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(1).setIndex(i, j);
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT3")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(2).setIndex(i, j);
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT4")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(3).setIndex(i, j);
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT5")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(4).setIndex(i, j);
            }
        } else if (state.equals(State.DEFAULT) && inputKeyboard.contains("DIGIT6")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(5).setIndex(i, j);
            }
        } else {
            double deltaX = x - mouseAnchorX;
            double deltaY = y - mouseAnchorY;

            translateX += deltaX;
            translateY += deltaY;

            mouseAnchorX = x;
            mouseAnchorY = y;
        }
        render();
    }

    private void handleMousePressed(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K") || state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
            double cSize = cellSize * scale;
            if (moveToI == -1 && moveToJ == -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                moveToI = i;
                moveToJ = j;
                lineToI = -1;
                lineToJ = -1;
            }
        } else {
            mouseAnchorX = x;
            mouseAnchorY = y;
        }
    }

    private void handleMouseMoved(MouseEvent mouseEvent) {
        if (state.equals(State.OBSTACLE) || state.equals(State.LINE) || state.equals(State.REMOVE_LINE) || inputKeyboard.contains("W") || inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();
            double cSize = cellSize * scale;
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                positionI = i;
                positionJ = j;
                render();
            }
        }
    }

    private void handleButtonOpen(ActionEvent actionEvent) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Environment");

        // Set initial directory "Documents"
        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        if (documentsDir.exists()) {
            fileChooser.setInitialDirectory(documentsDir);
        }

        // Opsional: filter jenis file
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data", "*.sc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            // System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            readFromFile(selectedFile);
        } else {
            // System.out.println("Tidak ada file dipilih.");
        }
    }

    private void handleButtonFree(ActionEvent actionEvent) {
        state = State.DEFAULT;
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
        statelabel.setText("STATE: " + state.toString() + "\t-\t");
    }

    private void handleButtonObstacle(ActionEvent actionEvent) {
        state = State.OBSTACLE;
        statelabel.setText("STATE: " + state.toString() + "\t-\t");
    }

    private void handleButtonLine(ActionEvent actionEvent) {
        state = State.LINE;
        statelabel.setText("STATE: " + state.toString() + "\t-\t");
    }

    private void handleButtonRemoveLine(ActionEvent actionEvent) {
        state = State.REMOVE_LINE;
        statelabel.setText("STATE: " + state.toString() + "\t-\t");
    }

    private void handleButtonSave(ActionEvent actionEvent) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Save Environment");

        // Set initial directory "Documents"
        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        if (documentsDir.exists()) {
            fileChooser.setInitialDirectory(documentsDir);
        }

        // Opsional: filter jenis file
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data", "*.sc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile != null) {
            // System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            writeToFile(selectedFile);
        } else {
            // System.out.println("Tidak ada file dipilih.");
        }
    }

    private void handleButtonRun(ActionEvent actionEvent) {
        if (!running) {
            running = true;
            visualized = false;
            frame = -1;
            state = State.DEFAULT;
            statelabel.setText("STATE: " + state.toString() + "\t-\t");
            runFrotierBasedExplorationInBackground();
        }
    }

    private void handleButtonPlayPause(ActionEvent actionEvent) {
        if (pause) {
            pause = false;
        } else if (!pause) {
            pause = true;
        }
    }

    private void handleButtonReplay(ActionEvent actionEvent) {
        pause = false;
        frame = -1;
        visualized = true;
        darkScreen = copyArrayInteger(darkScreenCopy);
    }

    private void handleButtonResetEnvironment(ActionEvent actionEvent) {
        resetEnvironment();
        statelabel.setText("STATE: " + state.toString() + "\t-\t");
    }

    private void initializeDrones(int numberOfDrone) {
        drones = new ArrayList<>();
        int row = 1 + sensoreFoV;
        for (int d = 0; d < numberOfDrone; d++) {
            Drone drone = new Drone(row, numCols - 1, sensoreFoV, cameraFoV, cellSize);
            drone.setName("D" + (1 + d));
            drones.add(drone);
            row += (2 * sensoreFoV + 1);
        }
    }

    private void setBorders() {// EXPLORATION
        MIN_ROW = numRows - 1;
        MIN_COL = numCols - 1;
        MAX_ROW = 0;
        MAX_COL = 0;
        for (int i = 0; i < environment.length; i++) {
            for (int j = 0; j < environment[i].length; j++) {
                if (environment[i][j] == -1) {
                    if (i < MIN_ROW) {
                        MIN_ROW = i;
                    }
                    if (j < MIN_COL) {
                        MIN_COL = j;
                    }
                    if (i > MAX_ROW) {
                        MAX_ROW = i;
                    }
                    if (j > MAX_COL) {
                        MAX_COL = j;
                    }
                }
            }
        }
    }

    private int[][] getMap() {// EXPLORATION
        setBorders();
        int nrows = MAX_ROW - MIN_ROW + 1;
        int ncols = MAX_COL - MIN_COL + 1;
        map = null;
        base_map = null;
        if (nrows > 0 && ncols > 0) {
            darkScreen = new int[numRows][numCols];
            map = new int[nrows][ncols];
            base_map = new int[nrows][ncols];
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    map[i][j] = environment[i + MIN_ROW][j + MIN_COL];
                    base_map[i][j] = environment[i + MIN_ROW][j + MIN_COL];
                }
            }
        }
        return map;
    }

    private int[][] copyArrayInteger(int[][] array) {// EXPLORATION
        int[][] copy = null;
        if (array != null) {
            copy = new int[array.length][];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = new int[array[i].length];
                System.arraycopy(array[i], 0, copy[i], 0, copy[i].length);
            }
        }
        return copy;
    }

    private int getMustBeExplored(int ci, int cj) {// EXPLORATION
        int mustBeExploredFromCiCj = 0;
        if (base_map != null) {
            int[][] mustBeExploredMap = copyArrayInteger(base_map);
            Queue<Point> queue = new LinkedList<Point>();
            queue.offer(new Point(ci, cj));
            mustBeExploredMap[ci][cj] = 1;
            darkScreen[ci + MIN_ROW][cj + MIN_COL] = 1;
            mustBeExploredFromCiCj = 1;
            while (!queue.isEmpty()) {
                Point center = queue.poll();
                // check neighbors
                for (int n = 0; n < neighbors8.length; n++) {
                    int i = center.i + neighbors8[n][0];
                    int j = center.j + neighbors8[n][1];
                    if (i >= 0 && i < mustBeExploredMap.length && j >= 0 && j < mustBeExploredMap[0].length && mustBeExploredMap[i][j] == 0 && map[i][j] == 0) {
                        mustBeExploredMap[i][j] = 1;
                        darkScreen[i + MIN_ROW][j + MIN_COL] = 1;
                        queue.offer(new Point(i, j));
                        mustBeExploredFromCiCj++;
                    }
                }
            }
        }
        return mustBeExploredFromCiCj;
    }

    private int getMustBeExplored() {// EXPLORATION
        explored = 0;
        mustBeExplored = 0;
        if (!activeDrones.isEmpty()) {
            //int[]mustExploredByAllDrones = new int[activeDrones.size()];
            HashSet<Integer> unique = new HashSet<>();
            for (int d = 0; d < activeDrones.size(); d++) {
                int ci = activeDrones.get(d).indexI - MIN_ROW;
                int cj = activeDrones.get(d).indexJ - MIN_COL;
                int m = getMustBeExplored(ci, cj);
                //mustExploredByAllDrones[d]=m;
                //System.out.println("m: "+m);
                unique.add(m);
            }
            // System.out.println(unique);
            for (Integer val : unique) {
                mustBeExplored += val;
            }
            /* copy dark screen */
            darkScreenCopy = copyArrayInteger(darkScreen);
        }
        return mustBeExplored;
    }

    private boolean isDroneValid(int droneI, int droneJ) {// EXPLORATION
        boolean valid = droneI >= MIN_ROW && droneI <= MAX_ROW && droneJ >= MIN_COL && droneJ <= MAX_COL;
        return valid;
    }

    private ArrayList<Drone> setDrone() {// EXPLORATION
        activeDrones = new ArrayList<>();
        for (int i = 0; i < drones.size(); i++) {
            Drone drone = drones.get(i);
            if (isDroneValid(drone.indexI, drone.indexJ)) {
                activeDrones.add(drone);
            }
        }
        if (!activeDrones.isEmpty()) {
            lastPositionOfActiveDrones = new ArrayList<>();
            frontiers = new ArrayList[activeDrones.size()];
            pathOfDrones = new ArrayList[activeDrones.size()];
            for (int d = 0; d < activeDrones.size(); d++) {
                lastPositionOfActiveDrones.add(new Point(activeDrones.get(d).indexI, activeDrones.get(d).indexJ));
                frontiers[d] = new ArrayList<>();
                pathOfDrones[d] = new ArrayList<>();
            }
        }
        return activeDrones;
    }

    private void setFree(int ci, int cj) {
        // setiap drone memiliki frontiernya masing-masing, namun sebuah drone dapat menghapus frontier drone lain saat setfree
        int rays = 360;
        double cSize = cellSize * scale;
        double hcSize = 0.5 * cSize;

        for (int i = 0; i < rays; i += 2) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            double x = cj * cSize + hcSize;
            double y = ci * cSize + hcSize;
            double step = 0.01;//epsilon resolusi ray
            double checked = 0;
            double MAX_RADIUS = (double) cameraFoV * cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] == -1) {
                    break;
                } else {
                    if (map[row][col] == 0) {
                        explored++;//check as explored
                    }
                    map[row][col] = 1;
                    // remove frontier
                    Point freePoint = new Point(row, col);
                    for (int d = 0; d < activeDrones.size(); d++) {
                        if (!frontiers[d].isEmpty()) {
                            frontiers[d].remove(freePoint);
                        }
                    }
                }
            }

        }
    }

    private void initializeExploration() {
        if (lastPositionOfActiveDrones != null && map != null && !lastPositionOfActiveDrones.isEmpty()) {
            for (int d = 0; d < lastPositionOfActiveDrones.size(); d++) {
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int ci = position.i - MIN_ROW;
                int cj = position.j - MIN_COL;
                setFree(ci, cj);
                pathOfDrones[d].add(new Point(ci, cj));// save initial pathOfDrones (start points)
            }
        }
    }

    private ArrayList<Point>[] detectFrontiers(int droneIndex, int ci, int cj) {
        // jika masuk ke dalam jangkauan sensor dan bukan obstacle dan bukan wall dan merupakan unknown
        int rays = 360;
        double cSize = cellSize * scale;
        double hcSize = 0.5 * cSize;

        for (int i = 0; i < rays; i += 2) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            double x = cj * cSize + hcSize;
            double y = ci * cSize + hcSize;
            double step = 0.01;//epsilon resolusi ray
            double checked = 0;
            double MAX_RADIUS = (double) sensoreFoV * cSize + cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);

                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] <= 0) {
                    if (row >= 0 && row < map.length && col >= 0 && col < map[0].length && map[row][col] == 0) {
                        Point frontier = new Point(row, col);
                        boolean detected = false;
                        for (int d = 0; d < activeDrones.size(); d++) {
                            if (frontiers[d].contains(frontier)) {
                                detected = true;
                                break;
                            }
                        }
                        if (!detected) {
                            frontiers[droneIndex].add(frontier);
                        }
                    }
                    break;
                }
            }

        }
        return frontiers;
    }

    private ArrayList<Point>[] detectFrontiers() {
        if (activeDrones != null && !activeDrones.isEmpty()) {
            for (int d = 0; d < activeDrones.size(); d++) {
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int ci = position.i - MIN_ROW;
                int cj = position.j - MIN_COL;
                detectFrontiers(d, ci, cj);
            }
        }
        return frontiers;
    }

    private Point[][] aStartToNearestFrontier(){
        Point[][] pathToNearestFrontier = null;
        if (activeDrones != null && !activeDrones.isEmpty() && frontiers != null && frontiers.length > 0) {
            pathToNearestFrontier = new Point[activeDrones.size()][];
            for (int d = 0; d < activeDrones.size(); d++) {
                ArrayList<Point> frontiersDrone = frontiers[d];
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int ci = position.i - MIN_ROW;
                int cj = position.j - MIN_COL;
                for(Point frontier:frontiersDrone){
                    Point begin = new Point(ci, cj);
                    Point end = new Point(frontier.getI(),frontier.getJ());
                    //temukan a star dari begin ke end


                }

            }
        }
        return pathToNearestFrontier;
    }

    private Point[][] bfsToNearestfrontier() {
        Point[][] pathToNearestFrontier = null;
        if (activeDrones != null && !activeDrones.isEmpty() && frontiers != null && frontiers.length > 0) {
            pathToNearestFrontier = new Point[activeDrones.size()][];
            for (int d = 0; d < activeDrones.size(); d++) {
                ArrayList<Point> frontiersDrone = frontiers[d];
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int ci = position.i - MIN_ROW;
                int cj = position.j - MIN_COL;
                Point begin = new Point(ci, cj);
                Stack<Point> path = null;
                if (base_map != null) {
                    int[][] mapBFS = copyArrayInteger(base_map);
                    int step = 1;
                    mapBFS[begin.i][begin.j] = step;
                    boolean complete = false;
                    Queue<Point> queue = new LinkedList<Point>();
                    queue.offer(begin);

                    while (!queue.isEmpty() && !complete) {
                        //System.out.println(queue);
                        /* check neighbors */
                        Point center = queue.poll();
                        step = mapBFS[center.i][center.j];
                        for (int n = 0; n < neighbors8.length; n++) {
                            int i = center.i + neighbors8[n][0];
                            int j = center.j + neighbors8[n][1];
                            Point newNeighbor = new Point(i, j);
                            if (frontiersDrone.contains(newNeighbor)) {
                                //finish
                                complete = true;
                                mapBFS[i][j] = step + 1;
                                path = new Stack<>();
                                path.push(newNeighbor);
                                int footsteps = step;
                                while (footsteps > 0) {
                                    Point top = path.peek();
                                    for (int l = 0; l < neighbors8.length; l++) {
                                        int ni = top.i + neighbors8[l][0];
                                        int nj = top.j + neighbors8[l][1];
                                        if (ni >= 0 && ni < mapBFS.length && nj >= 0 && nj < mapBFS[0].length && mapBFS[ni][nj] == footsteps) {
                                            path.push(new Point(ni, nj));
                                            footsteps--;
                                            break;
                                        }
                                    }
                                }
                                pathToNearestFrontier[d] = new Point[path.size()];
                                for (int k = 0; k < path.size(); k++) {
                                    pathToNearestFrontier[d][k] = path.pop();
                                }
                            } else {
                                if (i >= 0 && i < mapBFS.length && j >= 0 && j < mapBFS[0].length && mapBFS[i][j] == 0) {
                                    queue.offer(newNeighbor);
                                    mapBFS[i][j] = step + 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return pathToNearestFrontier;
    }

    private void runFrotierBasedExplorationInBackground() {
        try {
            running = true;

            // create task
            Task<Void> task = createBackgroundTask();

            // Bind UI ke task
            progressIndicator.setVisible(true);
            progressIndicator.progressProperty().bind(task.progressProperty());
            statusLabel.textProperty().bind(task.messageProperty());

            // Jalankan task
            Thread backgroundThread = new Thread(task);
            //backgroundThread.setDaemon(true);
            backgroundThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Task<Void> createBackgroundTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {

                // INITIALIZE ------------------------------------------------------------------------------------------
                map = getMap();
                activeDrones = setDrone();

                // LOOP ------------------------------------------------------------------------------------------------
                if (activeDrones != null && !activeDrones.isEmpty()) {
                    mustBeExplored = getMustBeExplored();

                    // LOG TEXT ===============================
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
                    String currentTime = LocalDateTime.now().format(formatter);
                    logArea.append("==========================================================\n");
                    logArea.append("Run_" + currentTime + "\n");
                    logArea.append("----------------------------------------------------------\n");
                    logArea.append("Must be explored: " + mustBeExplored + "\n");
                    logArea.append("Number of drones: " + activeDrones.size() + "\n");
                    // LOG TEXT ===============================

                    int totalSteps = mustBeExplored;
                    initializeExploration();
                    detectFrontiers();
                    boolean complete = false;
                    while (!complete) {
                        //check complete
                        boolean areThereStillAnyFrontiers = false;
                        if (frontiers != null && frontiers.length == activeDrones.size()) {
                            for (int f = 0; f < frontiers.length; f++) {
                                if (frontiers[f] != null && !frontiers[f].isEmpty()) {
                                    areThereStillAnyFrontiers = true;
                                    break;
                                }
                            }
                        }

                        if (!areThereStillAnyFrontiers) {
                            complete = true;
                            break;
                        } else {
                            Point[][] pathToNearestFrontier = bfsToNearestfrontier();
                            //set free
                            for (int d = 0; d < activeDrones.size(); d++) {
                                if (pathToNearestFrontier[d] != null && pathToNearestFrontier[d].length > 0) {
                                    Point lastPoint = null;
                                    for (int p = 0; p < pathToNearestFrontier[d].length; p++) {
                                        Point point = pathToNearestFrontier[d][p];
                                        if (point != null) {
                                            setFree(point.i, point.j);
                                            lastPoint = point;
                                            if (p > 0) {
                                                pathOfDrones[d].add(point);// save pathOfDrones
                                            }
                                        }
                                    }
                                    //move droneto the last point in path
                                    if (lastPoint != null) {
                                        //activeDrones.get(d).setIndex(lastPoint.i+MIN_ROW, lastPoint.j+MIN_COL);
                                        lastPositionOfActiveDrones.set(d, new Point(lastPoint.i + MIN_ROW, lastPoint.j + MIN_COL));

                                        //update progress indicator
                                        Thread.sleep(50); // Simulasi kerja
                                        updateProgress(explored, totalSteps);
                                        updateMessage("explored: " + (explored) + " of " + (mustBeExplored));
                                    }
                                }
                            }
                            //detect frontiers again
                            detectFrontiers();
                        }
                    }
                }

                // FINALIZE --------------------------------------------------------------------------------------------
                progressIndicator.setVisible(false);
                double coverage = 100.0 * (double) explored / (double) mustBeExplored;
                updateMessage("EXPLORATION COMPLETE - explored: " + (explored) + " of " + (mustBeExplored) + " (" + String.format("%.2f", coverage) + "%)");

                // LOG TEXT ===============================
                logArea.append("Explored: " + explored + "\n");
                logArea.append("Coverage: " + coverage + "%\n");
                logArea.append("Trajectories of the drones:\n");
                if (pathOfDrones != null && pathOfDrones.length > 0) {
                    for (int d = 0; d < pathOfDrones.length; d++) {
                        logArea.append("Drone-" + (1 + d) + ": ");
                        ArrayList<Point> path = pathOfDrones[d];
                        if (path != null && !path.isEmpty()) {
                            for (int p = 0; p < path.size(); p++) {
                                if (p > 0) {
                                    logArea.append(" - ");
                                }
                                Point point = path.get(p);
                                logArea.append("(" + point.i + "," + point.j + ")");
                            }
                        }
                        logArea.append("\n\n");
                        System.out.println(pathOfDrones[d]);

                    }
                }
                logArea.append("----------------------------------------------------------\n\n\n");

                // LOG TEXT ===============================
                running = false;
                visualized = true;
                frame = 0;
                render();
                return null;
            }
        };
    }

    private void openTheDarkScreen(int ci, int cj) {
        int rays = 360;
        double cSize = cellSize * scale;
        double hcSize = 0.5 * cSize;

        // SET FREE AREA ================================
        for (int i = 0; i < rays; i += 2) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            double x = cj * cSize + hcSize;
            double y = ci * cSize + hcSize;
            double step = 0.01;//epsilon resolusi ray
            double checked = 0;
            double MAX_RADIUS = (double) cameraFoV * cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row < 0 || row >= darkScreen.length || col < 0 || col >= darkScreen[0].length || environment[row][col] == -1) {
                    break;
                } else {
                    darkScreen[row][col] = 0;
                }
            }
        }
        //
        for (int i = 0; i < rays; i += 2) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            double x = cj * cSize + hcSize;
            double y = ci * cSize + hcSize;
            double step = 0.01;//epsilon resolusi ray
            double checked = 0;
            double MAX_RADIUS = (double) sensoreFoV * cSize + cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row < 0 || row >= darkScreen.length || col < 0 || col >= darkScreen[0].length || darkScreen[row][col] >= 1 || environment[row][col] == -1) {
                    if (row >= 0 && row < darkScreen.length && col >= 0 && col < darkScreen[0].length && darkScreen[row][col] == 1) {
                        darkScreen[row][col] = 2;
                    }
                    break;
                }
            }


        }
    }

    private void update() {

        // update frame
        if (!pause) {
            framePeriod++;
            if (framePeriod >= MAX_FRAME_PERIOD) {
                framePeriod = 0;
                frame++;
            }
        }

        if (inputKeyboard.contains("CONTROL")) {
            if (inputKeyboard.contains("DIGIT0")) {
                resetScale();
            } else if (inputKeyboard.contains("EQUALS")) {
                double zoomFactor = 1.05;
                scale *= zoomFactor;
                render();
            } else if (inputKeyboard.contains("MINUS")) {
                double zoomFactor = 1.05;
                scale /= zoomFactor;
                render();
            }
        }

    }

    private void render() {
        if (canvas != null && gc != null) {
            gc = canvas.getGraphicsContext2D();
            gc.setImageSmoothing(true);
            gc.setFontSmoothingType(FontSmoothingType.LCD);
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            gc.save();
            gc.translate(translateX, translateY);
            gc.scale(scale, scale);

            // BEGIN VISUALIZATION -------------------------------------------------------------------------------------

            /* draw blue background */
            gc.setFill(Color.valueOf("#283d64"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            /* draw horizontal line */
            for (int i = 0; i <= numRows; i++) {
                gc.setLineWidth(0.2);
                gc.setStroke(Color.valueOf("#425478"));
                if (i % 10 == 0) {
                    gc.setLineWidth(0.8);
                    gc.setStroke(Color.valueOf("#445578"));
                }
                gc.strokeLine(0, i * cellSize, canvas.getWidth(), i * cellSize);
            }

            /* draw vertical line */
            for (int i = 0; i <= numCols; i++) {
                gc.setLineWidth(0.2);
                gc.setStroke(Color.valueOf("#425478"));
                if (i % 10 == 0) {
                    gc.setLineWidth(0.8);
                    gc.setStroke(Color.valueOf("#445578"));
                }
                gc.strokeLine(i * cellSize, 0, i * cellSize, canvas.getHeight());
            }

            /* draw dark screen*/
            if (darkScreen != null) {
                for (int i = 0; i < darkScreen.length; i++) {
                    for (int j = 0; j < darkScreen[i].length; j++) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;
                        if (darkScreen[i][j] == 1) {
                            gc.setFill(Color.rgb(0, 0, 0, 0.5));
                            gc.fillRect(xo, yo, cellSize, cellSize);
                        } else if (darkScreen[i][j] == 2) {
                            gc.setFill(Color.rgb(255, 225, 0, 0.4));
                            gc.fillRect(xo, yo, cellSize, cellSize);
                        }
                    }
                }
            }

            /* Draw Path Of Drones */
            if (pathOfDrones != null && !running && frame >= 0) {
                int complete = 0;
                for (int d = 0; d < pathOfDrones.length; d++) {
                    if (pathOfDrones[d] != null && !pathOfDrones[d].isEmpty()) {
                        int max = frame;
                        if (max >= pathOfDrones[d].size()) {
                            max = pathOfDrones[d].size() - 1;
                            complete++;
                        }
                        Point point0 = pathOfDrones[d].get(0);
                        double px0 = (double) (point0.j + MIN_COL) * cellSize + halfCellSize;
                        double py0 = (double) (point0.i + MIN_ROW) * cellSize + halfCellSize;

                        for (int p = 1; p <= max; p++) {
                            Point point1 = pathOfDrones[d].get(p);
                            double px1 = (double) (point1.j + MIN_COL) * cellSize + halfCellSize;
                            double py1 = (double) (point1.i + MIN_ROW) * cellSize + halfCellSize;
                            gc.setStroke(pathColor[d % pathColor.length]);
                            gc.setLineWidth(0.8);
                            gc.strokeLine(px0, py0, px1, py1);
                            point0 = point1;
                            px0 = (double) (point0.j + MIN_COL) * cellSize + halfCellSize;
                            py0 = (double) (point0.i + MIN_ROW) * cellSize + halfCellSize;
                        }

                        /* set position for active drone*/
                        if (visualized) {
                            activeDrones.get(d).setIndex(point0.i + MIN_ROW, point0.j + MIN_COL);
                        }
                        openTheDarkScreen(point0.i + MIN_ROW, point0.j + MIN_COL);
                    }
                }
                if (complete >= activeDrones.size()) {
                    visualized = false;
                }
            }

            /* Draw Drones */
            if (drones != null && !drones.isEmpty()) {
                for (int i = 0; i < drones.size(); i++) {
                    drones.get(i).draw(gc, environment);
                }
            }

            /* draw wall/obstacle */
            for (int i = 0; i < environment.length; i++) {
                for (int j = 0; j < environment[i].length; j++) {
                    if (environment[i][j] == -1) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;

                        gc.setStroke(Color.valueOf("#ffffff"));
                        gc.setLineWidth(0.2);
                        gc.strokeLine(xo, yo, xo + cellSize, yo + cellSize);
                        gc.strokeLine(xo + halfCellSize, yo, xo + cellSize, yo + cellSize - halfCellSize);
                        gc.strokeLine(xo, yo + halfCellSize, xo + cellSize - halfCellSize, yo + cellSize);

                        // draw wall type
                        if (wallType != null) {
                            gc.setLineWidth(0.8);
                            String type = wallType[i][j];

                            // east
                            char value = type.charAt(0);
                            if (value == '1') {
                                gc.strokeLine(xo + cellSize, yo, xo + cellSize, yo + cellSize);
                            }

                            //south
                            value = type.charAt(1);
                            if (value == '1') {
                                gc.strokeLine(xo, yo + cellSize, xo + cellSize, yo + cellSize);
                            }
                            // west
                            value = type.charAt(2);
                            if (value == '1') {
                                gc.strokeLine(xo, yo, xo, yo + cellSize);
                            }
                            // north
                            value = type.charAt(3);
                            if (value == '1') {
                                gc.strokeLine(xo, yo, xo + cellSize, yo);
                            }

                        }
                    }
                }
            }

            /* draw line */
            if (moveToI != -1 && moveToJ != -1 && lineToI != -1 && lineToJ != -1) {
                double x0 = moveToJ * cellSize + halfCellSize;
                double y0 = moveToI * cellSize + halfCellSize;
                double x1 = lineToJ * cellSize + halfCellSize;
                double y1 = lineToI * cellSize + halfCellSize;
                gc.setStroke(Color.valueOf("#ffffff"));
                gc.setLineWidth(0.8);
                gc.strokeLine(x0, y0, x1, y1);
            }

            /* draw position */
            if (state.equals(State.OBSTACLE) || state.equals(State.LINE) || state.equals(State.REMOVE_LINE) || inputKeyboard.contains("W") || inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
                double px = positionJ * cellSize + halfCellSize;
                double py = positionI * cellSize + halfCellSize;
                String textPosition = "(" + (positionI + 1) + "," + (positionJ + 1) + ")";
                gc.setFont(Font.font("Arial", 8));
                gc.setFill(Color.rgb(255, 255, 255, 0.8));
                gc.fillText(textPosition, px, py + 2 + 3 * cellSize);
            }

            // END OF VISUALIZATION ------------------------------------------------------------------------------------

            gc.restore();
        }// end of check canvas and gc not null

    }

    private Button createIconButton(String svgPathData, double scaleX, double scaleY, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.LIGHTGRAY);
        path.setScaleX(scaleX);
        path.setScaleY(scaleY);
        Button btn = new Button();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent;");
        //btn.setOnMouseEntered(e -> path.setFill(Color.WHITE));
        btn.setOnMouseEntered(e -> path.setFill(Color.valueOf("#0096FF")));
        btn.setOnMouseExited(e -> path.setFill(Color.LIGHTGRAY));
        return btn;
    }

    // Dapatkan screen tempat jendela saat ini berada
    private Screen getCurrentScreen(Stage stage) {
        double centerX = stage.getX() + stage.getWidth() / 2;
        double centerY = stage.getY() + stage.getHeight() / 2;

        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getBounds();
            if (bounds.contains(centerX, centerY)) {
                return screen;
            }
        }
        return Screen.getPrimary();
    }

    // Set stage agar fullscreen di layar tertentu
    private void setStageToScreen(Stage stage, Screen screen) {
        Rectangle2D visualBounds = screen.getVisualBounds();
        stage.setX(visualBounds.getMinX());
        stage.setY(visualBounds.getMinY());
        stage.setWidth(visualBounds.getWidth());
        stage.setHeight(visualBounds.getHeight());
    }

    private void setAccelerators(Scene scene) {
        // Shortcut Ctrl+O untuk btnOpen
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                () -> btnOpen.fire()
        );

        // Shortcut Ctrl+S untuk btnSave
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> btnSave.fire()
        );

        // Shortcut Ctrl+F untuk btnFree
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                () -> btnTransform.fire()
        );

        // Shortcut Ctrl+Space untuk btnRun
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN),
                () -> btnRun.fire()
        );

        // Shortcut Ctrl+Space untuk btnReset
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
                () -> btnNew.fire()
        );

        // Shortcut Ctrl+Space untuk btnPlayPause
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.P),
                () -> btnPlayPause.fire()
        );

        // Shortcut Ctrl+Space untuk btnReplay
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R),
                () -> btnReplay.fire()
        );
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // === Info Bar ===
        Label infoLabel = new Label("");
        infoLabel.getStyleClass().add("info-label");

        HBox infoBar = new HBox();
        infoBar.getStyleClass().add("info-bar");
        infoBar.setPadding(new Insets(0));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.NEVER);
        Label info = new Label("");
        info.getStyleClass().add("info-label");
        infoBar.getChildren().addAll(spacer2, infoLabel, info);

        VBox topBar = new VBox(infoBar);
        VBox.setVgrow(infoBar, Priority.NEVER);



        // === Sidebar ===
        VBox sidebar = new VBox(2);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(4));

        VBox topButtons = new VBox(2, btnNew, btnOpen, btnTransform, btnDrone, btnWall, btnDrawLine, btnRemoveLine, btnSave, btnRun, btnPlayPause, btnReplay);
        topButtons.setAlignment(Pos.TOP_CENTER);

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        statusLabel = new Label("");

        VBox bottomButtons = new VBox(2, progressIndicator);
        bottomButtons.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(topButtons, Priority.ALWAYS);
        sidebar.getChildren().addAll(topButtons, bottomButtons);

        // set on action for buttons
        btnOpen.setOnAction(this::handleButtonOpen);
        btnTransform.setOnAction(this::handleButtonFree);
        btnWall.setOnAction(this::handleButtonObstacle);
        btnDrawLine.setOnAction(this::handleButtonLine);
        btnRemoveLine.setOnAction(this::handleButtonRemoveLine);
        btnSave.setOnAction(this::handleButtonSave);
        btnRun.setOnAction(this::handleButtonRun);
        btnPlayPause.setOnAction(this::handleButtonPlayPause);
        btnReplay.setOnAction(this::handleButtonReplay);
        btnNew.setOnAction(this::handleButtonResetEnvironment);

        // === Main Content ===
        StackPane canvasPane = new StackPane();
        canvasPane.getStyleClass().add("canvas-pane");

        // Add Canvas
        initializeDrones(numberOfDrones);// initialize drone
        canvas = new Canvas(numCols * cellSize, numRows * cellSize);
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleMouseScroll);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvasPane.getChildren().add(canvas);

        ScrollPane scrollPane = new ScrollPane(canvasPane);
        scrollPane.getStyleClass().add("dark-scrollpane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefHeight(900);

        // === Log Area ===
        logArea = new StringBuffer();
        logArea.append("Simulator start...\n" + help);


        // === Status Bar ===
        statusLabel = new Label("Ready.");
        statusLabel.getStyleClass().add("status-label");
        statelabel = new Label("STATE: " + state.toString() + "\t-\t");
        statelabel.getStyleClass().add("status-label");
        HBox statusBar = new HBox(statelabel, statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));

        // SplitPane vertikal
        SplitPane splitPane = new SplitPane(scrollPane);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.9);

        // === Bottom (log + status) ===
        VBox rightBottomBox = new VBox(statusBar);

        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(splitPane);
        rightPane.setBottom(rightBottomBox);


        // === Layout ===
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");
        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(rightPane);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        setAccelerators(scene);

        /* STRART the ENGINE */
        AnimationTimer engine = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // UPDATE
                update();

                // RENDER
                render();
            }
        };
        engine.start();

        // === CSS Centralized ===
        scene.getStylesheets().add("data:text/css," +
                /* Title Bar */
                ".title-bar { -fx-background-color: #27282b; }" +
                ".title-label { -fx-text-fill: #dfe1e5; -fx-font-size: 14; }" +

                /* Control Buttons */
                ".control-btn { -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; }" +
                ".control-btn:hover { -fx-background-color: #555; -fx-text-fill: white; }" +

                /* Info Bar */
                ".info-bar { -fx-background-color: #2b2d30; }" +
                ".info-label { -fx-text-fill: #8c8a8c; -fx-font-size: 10; }" +

                /* Sidebar */
                ".sidebar { -fx-background-color: #2b2d30; -fx-pref-width: 10; }" +

                /* Canvas Pane */
                ".canvas-pane { -fx-background-color: #1e1f22; }" +

                /* Dark ScrollPane */
                ".dark-scrollpane { -fx-background-color: #1e1e1e; }" +
                ".dark-scrollpane .scroll-bar { -fx-background-color: #2b2b2b; }" +
                ".dark-scrollpane .scroll-bar .thumb { -fx-background-color: #555555; -fx-background-radius: 4; }" +
                ".dark-scrollpane .scroll-bar .thumb:hover { -fx-background-color: #777777; }" +
                ".dark-scrollpane .increment-button, .dark-scrollpane .decrement-button { -fx-background-color: #2b2b2b; }" +
                ".dark-scrollpane .increment-arrow, .dark-scrollpane .decrement-arrow { -fx-background-color: transparent; }" +
                ".dark-scrollpane .corner { -fx-background-color: #1e1e1e; }" +

                /* Dark TextArea */
                ".dark-textarea { -fx-control-inner-background: #1e1f22; -fx-text-fill: #8c8a8c; -fx-font-size: 13px; -fx-highlight-fill: #555555; -fx-highlight-text-fill: #ffffff; -fx-background-color: #1e1f22; -fx-border-color: #333333; }" +
                ".dark-textarea .scroll-bar { -fx-background-color: #1e1e1e; }" +
                ".dark-textarea .scroll-bar .thumb { -fx-background-color: #555555; -fx-background-radius: 5; }" +
                ".dark-textarea .scroll-bar .thumb:hover { -fx-background-color: #777777; }" +
                ".dark-textarea .increment-button, .dark-textarea .decrement-button { -fx-background-color: #1e1e1e; }" +
                ".dark-textarea .increment-arrow, .dark-textarea .decrement-arrow { -fx-background-color: transparent; }" +
                ".dark-textarea .corner { -fx-background-color: #1e1e1e; }" +

                /* SplitPane */
                ".split-pane { -fx-background-color: #1e1e1e; }" +
                ".split-pane-divider { -fx-background-color: #333333; }" +

                /* Status Bar */
                ".status-bar { -fx-background-color: #27282b; }" +
                ".status-label { -fx-text-fill: #8c8a8c; }"
        );

        // stage
        primaryStage.setTitle("Lab. Control and Robotic - Mechanical Engineering - Hasanuddin University");
        primaryStage.initStyle(StageStyle.DECORATED);
        //primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
        //primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
    }

    /*INNER CLASS =====================================================================================================*/

    private enum State {
        DEFAULT, OBSTACLE, LINE, REMOVE_LINE
    }// end of private enum State

    private class Point {
        public int i, j;

        public Point(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "i=" + i +
                    ", j=" + j +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point titik = (Point) obj;
            return i == titik.i && j == titik.j;
        }
    }// end of private class Point

    private class Drone {
        int indexI, indexJ, sensoreFoV, cameraFoV, cellSize;
        String name = "";

        public Drone(int indexI, int indexJ, int sensoreFoV, int cameraFoV, int cellSize) {
            this.indexI = indexI;
            this.indexJ = indexJ;
            this.sensoreFoV = sensoreFoV;
            this.cameraFoV = cameraFoV;
            this.cellSize = cellSize;
        }

        public void setIndex(int indexI, int indexJ) {
            this.indexI = indexI;
            this.indexJ = indexJ;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void draw(GraphicsContext gc, int indexI, int indexJ, int[][] environment) {
            setIndex(indexI, indexJ);
            draw(gc, environment);
        }

        public void draw(GraphicsContext gc, int[][] environment) {
            double halfCellSize = 0.5 * cellSize;
            double droneX = (double) indexJ * cellSize + halfCellSize;
            double droneY = (double) indexI * cellSize + halfCellSize;

            if (droneX >= 0 && droneY >= 0) {
                gc.setFill(Color.rgb(251, 248, 239, 0.2));
                drawCameraVisibility(gc, droneX, droneY, environment);
                drawSensoreVisibility(gc, droneX, droneY, environment);

                // gambar titikdrone
                //gc.setFill(Color.BLACK);
                //gc.fillOval(droneX - 5, droneY - 5, 10, 10);

                // gambar drone
                gc.setFill(Color.BLACK);
                gc.fillRect(droneX - 1.5, droneY - 1.5, 3, 3);
                gc.setLineWidth(0.4);
                gc.setStroke(Color.BLACK);
                double rx1 = droneX - 4;
                double ry1 = droneY - 4;
                double rx2 = droneX + 4;
                double ry2 = droneY + 4;
                double rx3 = droneX - 4;
                double ry3 = droneY + 4;
                double rx4 = droneX + 4;
                double ry4 = droneY - 4;
                gc.strokeLine(rx1, ry1, rx2, ry2);
                gc.strokeLine(rx3, ry3, rx4, ry4);
                //propeller
                double rPropeller = 3;
                gc.strokeOval(rx1 - rPropeller, ry1 - rPropeller, 2 * rPropeller, 2 * rPropeller);
                gc.strokeOval(rx2 - rPropeller, ry2 - rPropeller, 2 * rPropeller, 2 * rPropeller);
                gc.strokeOval(rx3 - rPropeller, ry3 - rPropeller, 2 * rPropeller, 2 * rPropeller);
                gc.strokeOval(rx4 - rPropeller, ry4 - rPropeller, 2 * rPropeller, 2 * rPropeller);

                // draw drone name
                gc.setFont(Font.font("Arial", 8));
                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillText(name + " (" + (indexI + 1) + "," + (indexJ + 1) + ")", droneX + cellSize, droneY - cellSize);
            }
        }

        private void drawCameraVisibility(GraphicsContext gc, double sx, double sy, int[][] environment) {
            int numRows = environment.length;
            int numCols = environment[0].length;
            int rays = 360;
            double[] xPoints = new double[rays];
            double[] yPoints = new double[rays];
            double cSize = cellSize;//tidak perlu dikalikan dg scale karena dia dipanggil di dalam method render setelah mengatur ulang scale
            double hcSize = 0.5 * cSize;

            for (int i = 0; i < rays; i++) {
                double angle = Math.toRadians(i);
                double dx = Math.cos(angle);
                double dy = Math.sin(angle);

                double x = sx;
                double y = sy;
                double step = 0.01;//epsilon resolusi ray
                double checked = 0;
                double MAX_RADIUS = (double) cameraFoV * cSize + hcSize;
                while (checked < MAX_RADIUS) {
                    x += dx * step;
                    y += dy * step;
                    checked += step;
                    int col = (int) (x / cSize);
                    int row = (int) (y / cSize);
                    if (row < 0 || row >= numRows || col < 0 || col >= numCols || environment[row][col] == -1) {
                        break;
                    }
                }
                xPoints[i] = x;
                yPoints[i] = y;

            }
            gc.fillPolygon(xPoints, yPoints, rays);
        }

        private void drawSensoreVisibility(GraphicsContext gc, double sx, double sy, int[][] environment) {
            int numRows = environment.length;
            int numCols = environment[0].length;
            int rays = 360;
            double[] xPoints = new double[rays];
            double[] yPoints = new double[rays];
            double cSize = cellSize;//tidak perlu dikalikan dg scale karena dia dipanggil di dalam method render setelah mengatur ulang scale
            double hcSize = 0.5 * cSize;

            for (int i = 0; i < rays; i++) {
                double angle = Math.toRadians(i);
                double dx = Math.cos(angle);
                double dy = Math.sin(angle);

                double x = sx;
                double y = sy;
                double step = 0.01;//epsilon resolusi ray
                double checked = 0;
                double MAX_RADIUS = (double) sensoreFoV * cSize + hcSize;
                while (checked < MAX_RADIUS) {
                    x += dx * step;
                    y += dy * step;
                    checked += step;
                    int col = (int) (x / cSize);
                    int row = (int) (y / cSize);
                    if (row < 0 || row >= numRows || col < 0 || col >= numCols || environment[row][col] == -1) {
                        break;
                    }
                }
                xPoints[i] = x;
                yPoints[i] = y;

            }
            gc.fillPolygon(xPoints, yPoints, rays);
        }

        @Override
        public String toString() {
            return "Drone{" +
                    "indexI=" + indexI +
                    ", indexJ=" + indexJ +
                    '}';
        }

    }// end of private class Drone

    /*INNER CLASS =====================================================================================================*/
}
