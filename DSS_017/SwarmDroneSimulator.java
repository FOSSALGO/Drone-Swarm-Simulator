package frontier.based.swarmdronesimulator;

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
    private final String openPath = "M6.25 4.5A1.75 1.75 0 0 0 4.5 6.25v11.5c0 .966.783 1.75 1.75 1.75h11.5a1.75 1.75 0 0 0 1.75-1.75v-4a.75.75 0 0 1 1.5 0v4A3.25 3.25 0 0 1 17.75 21H6.25A3.25 3.25 0 0 1 3 17.75V6.25A3.25 3.25 0 0 1 6.25 3h4a.75.75 0 0 1 0 1.5h-4ZM13 3.75a.75.75 0 0 1 .75-.75h6.5a.75.75 0 0 1 .75.75v6.5a.75.75 0 0 1-1.5 0V5.56l-5.22 5.22a.75.75 0 0 1-1.06-1.06l5.22-5.22h-4.69a.75.75 0 0 1-.75-.75Z";// SVG ICON
    //private final String panPath = "M0 0h7.2v2.4h9.6V0H24v7.2h-2.4v9.6H24V24h-7.2v-2.4H7.2V24H0v-7.2h2.4V7.2H0V0m16.8 7.2V4.8H7.2v2.4H4.8v9.6h2.4v2.4h9.6v-2.4h2.4V7.2M2.4 2.4v2.4h2.4V2.4m14.4 0v2.4h2.4V2.4M2.4 19.2v2.4h2.4v-2.4m14.4 0v2.4h2.4v-2.4z";// SVG ICON
    private final String panPath = "M5.667 13.228a1 1 0 0 1 .774 1.84l-.108.046A2.001 2.001 0 1 0 8.83 17.81l.057-.143a1 1 0 1 1 1.885.666a4.001 4.001 0 1 1-5.105-5.105Zm12.666 0a4.001 4.001 0 1 1-5.105 5.105a1 1 0 0 1 1.84-.774l.046.108a2.001 2.001 0 1 0 2.696-2.497l-.143-.056a1 1 0 1 1 .666-1.886ZM8.011 6.64l.103.07l2.658 2.068a2 2 0 0 0 2.317.099l.139-.099l2.658-2.067a1 1 0 0 1 1.474 1.3l-.07.103l-2.068 2.658a2 2 0 0 0-.099 2.317l.099.139l2.067 2.658a1 1 0 0 1-1.3 1.474l-.103-.07l-2.658-2.068a2 2 0 0 0-2.317-.099l-.139.099l-2.658 2.067a1 1 0 0 1-1.474-1.3l.07-.103l2.068-2.658a2 2 0 0 0 .099-2.317l-.099-.139l-2.067-2.658a1 1 0 0 1 1.192-1.53l.108.056ZM17 3a4 4 0 0 1 1.333 7.772a1 1 0 0 1-.774-1.84l.108-.046A2.001 2.001 0 1 0 15.17 6.19l-.056.143a1 1 0 0 1-1.886-.666A4.001 4.001 0 0 1 17 3ZM7 3a4.001 4.001 0 0 1 3.772 2.667a1 1 0 0 1-1.84.774l-.046-.108A2.001 2.001 0 1 0 6.19 8.83l.143.057a1 1 0 0 1-.666 1.885A4.001 4.001 0 0 1 7 3Z";// SVG ICON
    private final String texturePath = "M3.075 20.925v-1.4l16.45-16.45h1.425v1.4L4.475 20.925h-1.4ZM3 14.7v-2.8L11.9 3h2.8L3 14.7ZM3 7V3h4L3 7Zm14 14l4-4v4h-4Zm-7.7 0L21 9.3v2.8L12.1 21H9.3Z";// SVG ICON
    private final String linePath = "M21.71 3.29a1 1 0 0 0-1.42 0l-18 18a1 1 0 0 0 0 1.42a1 1 0 0 0 1.42 0l18-18a1 1 0 0 0 0-1.42Z";// SVG ICON
    private final String lineDashPath = "M21.707 2.297a1 1 0 0 1 0 1.414l-.5.5a1 1 0 1 1-1.414-1.414l.5-.5a1 1 0 0 1 1.414 0Zm-4.004 4a1 1 0 0 1 0 1.414l-.997.997a1 1 0 1 1-1.414-1.414l.997-.997a1 1 0 0 1 1.414 0Zm-4.496 4.496a1 1 0 0 1 0 1.414l-1 1a1 1 0 0 1-1.414-1.414l1-1a1 1 0 0 1 1.414 0ZM8.703 16.71a1 1 0 1 0-1.414-1.414l-.998.997a1 1 0 1 0 1.414 1.415l.998-.998Zm-4.491 4.496a1 1 0 0 0-1.414-1.414l-.5.5a1 1 0 0 0 1.414 1.414l.5-.5Z";// SVG ICON
    private final String savePath = "M2 1a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H9.5a1 1 0 0 0-1 1v7.293l2.646-2.647a.5.5 0 0 1 .708.708l-3.5 3.5a.5.5 0 0 1-.708 0l-3.5-3.5a.5.5 0 1 1 .708-.708L7.5 9.293V2a2 2 0 0 1 2-2H14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h2.5a.5.5 0 0 1 0 1H2z";// SVG ICON
    private final String runPath = "M28.182 10.573c-1.552-.875-3.51.266-3.823.464l-6.714 3.807a2.621 2.621 0 0 0-3.359.083a2.62 2.62 0 0 0-.526 3.318c.646 1.089 1.979 1.557 3.167 1.115s1.885-1.672 1.667-2.917l6.74-3.828c.438-.276 1.464-.693 1.938-.427c.344.198.542.844.557 1.802h-.01v8.401c0 .786-.417 1.51-1.094 1.901l-9.63 5.563a2.197 2.197 0 0 1-2.193 0l-9.63-5.563a2.187 2.187 0 0 1-1.099-1.901v-11.12c0-.786.417-1.51 1.099-1.901l8.714-5.026a2.622 2.622 0 0 0 4.437-2.62a2.621 2.621 0 0 0-5.084.848l-8.99 5.193a4.041 4.041 0 0 0-2.031 3.505v11.115c0 1.453.771 2.786 2.026 3.51l9.625 5.563a4.051 4.051 0 0 0 4.052 0l9.63-5.563a4.063 4.063 0 0 0 2.026-3.505v-8.083h.005c.047-1.896-.464-3.151-1.5-3.734z";// SVG ICON
    private final String playpausePath = "M11 7H8v10h3V7Zm2 10h3V7h-3v10Z";// SVG ICON
    private final String replayPath = "m10 16.5l6-4.5l-6-4.5M22 12c0-5.54-4.46-10-10-10c-1.17 0-2.3.19-3.38.56l.7 1.94c.85-.34 1.74-.53 2.68-.53c4.41 0 8.03 3.62 8.03 8.03c0 4.41-3.62 8.03-8.03 8.03c-4.41 0-8.03-3.62-8.03-8.03c0-.94.19-1.88.53-2.72l-1.94-.66C2.19 9.7 2 10.83 2 12c0 5.54 4.46 10 10 10s10-4.46 10-10M5.47 3.97c.85 0 1.53.71 1.53 1.5C7 6.32 6.32 7 5.47 7c-.79 0-1.5-.68-1.5-1.53c0-.79.71-1.5 1.5-1.5Z";// SVG ICON
    private final String resetEnvPath = "M12 15v-1.5q0-.625.438-1.062T13.5 12H15zm0 4.025V16.9l4.9-4.9h2.125zm.05 1.875l8.825-8.85q.4.1.688.387t.387.688L13.1 21.95q-.4-.125-.662-.387t-.388-.663m2.925 1.1L22 14.975V17.1L17.1 22zM19 22l3-3v1.5q0 .625-.437 1.063T20.5 22zm1.775-12H18.7q-.65-2.2-2.475-3.6T12 5Q9.075 5 7.037 7.038T5 12q0 1.8.813 3.3T8 17.75V15h2v6H4v-2h2.35Q4.8 17.75 3.9 15.938T3 12q0-1.875.713-3.512t1.924-2.85t2.85-1.925T12 3q3.225 0 5.663 1.988T20.775 10";// SVG ICON

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
    private final String delimiter = ":";// FILE
    private final Button btnOpen = createIconButton(openPath, 1, 1, "Open Environment");// VISUALIZATION
    private final Button btnFree = createIconButton(panPath, 1.0, 1.0, "Default");// VISUALIZATION
    private final Button btnObstacle = createIconButton(texturePath, 1, 1, "Obstacle");// VISUALIZATION
    private final Button btnLine = createIconButton(linePath, 1, 1, "Draw Line");// VISUALIZATION
    private final Button btnRemoveLine = createIconButton(lineDashPath, 1, 1, "Remove Lain");// VISUALIZATION
    private final Button btnSave = createIconButton(savePath, 1.1, 1.1, "Save Path");// VISUALIZATION
    private final Button btnRun = createIconButton(runPath, 0.7, 0.7, "Run Frontier-Based Exploration");// VISUALIZATION
    private final Button btnPlayPause = createIconButton(playpausePath, 1, 1, "Play-Pause");// VISUALIZATION
    private final Button btnReplay = createIconButton(replayPath, 1, 1, "Replay");// VISUALIZATION
    private final Button btnResetEnvironment = createIconButton(resetEnvPath, 1, 1, "Reset Environment");// VISUALIZATION
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
    private TextArea logArea;// VISUALIZATION
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
        for (int i = 0; i < numberOfDrone; i++) {
            Drone drone = new Drone(row, numCols - 1, sensoreFoV, cameraFoV, cellSize);
            drone.setName("D" + (1 + i));
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
                    logArea.appendText("==========================================================\n");
                    logArea.appendText("Run_" + currentTime + "\n");
                    logArea.appendText("----------------------------------------------------------\n");
                    logArea.appendText("Must be explored: " + mustBeExplored + "\n");
                    logArea.appendText("Number of drones: " + activeDrones.size() + "\n");
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
                logArea.appendText("Explored: " + explored + "\n");
                logArea.appendText("Coverage: " + coverage + "%\n");
                logArea.appendText("Trajectories of the drones:\n");
                if (pathOfDrones != null && pathOfDrones.length > 0) {
                    for (int d = 0; d < pathOfDrones.length; d++) {
                        logArea.appendText("Drone-" + (1 + d) + ": ");
                        ArrayList<Point> path = pathOfDrones[d];
                        if (path != null && !path.isEmpty()) {
                            for (int p = 0; p < path.size(); p++) {
                                if (p > 0) {
                                    logArea.appendText(" - ");
                                }
                                Point point = path.get(p);
                                logArea.appendText("(" + point.i + "," + point.j + ")");
                            }
                        }
                        logArea.appendText("\n\n");
                    }
                }
                logArea.appendText("----------------------------------------------------------\n\n\n");
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
                () -> btnFree.fire()
        );

        // Shortcut Ctrl+Space untuk btnRun
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN),
                () -> btnRun.fire()
        );

        // Shortcut Ctrl+Space untuk btnReset
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
                () -> btnResetEnvironment.fire()
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

        // === Custom title bar ===
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("title-bar");
        titleBar.setPadding(new Insets(2));

        Label title = new Label(" Lab. Control and Robotic - Mechanical Engineering - Hasanuddin University");
        title.getStyleClass().add("title-label");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button minimizeBtn = new Button("—");
        Button maximizeBtn = new Button("⬜");
        Button closeBtn = new Button("✕");

        minimizeBtn.getStyleClass().add("control-btn");
        maximizeBtn.getStyleClass().add("control-btn");
        closeBtn.getStyleClass().add("control-btn");

        closeBtn.setOnAction(e -> primaryStage.close());
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));
        maximizeBtn.setOnAction(e -> {
            isMaximized = !isMaximized;
            if (isMaximized) {
                setStageToScreen(primaryStage, currentScreen);
            } else {
                primaryStage.setWidth(800);
                primaryStage.setHeight(640);
                primaryStage.centerOnScreen();
            }
        });

        Image appIcon = new Image(this.getClass().getResourceAsStream("icon.png"));
        ImageView icon = new ImageView(appIcon);
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);

        titleBar.getChildren().addAll(icon, title, spacer1, minimizeBtn, maximizeBtn, closeBtn);


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


        VBox topBar = new VBox(titleBar, infoBar);
        VBox.setVgrow(titleBar, Priority.NEVER);
        VBox.setVgrow(infoBar, Priority.NEVER);

        // Enable dragging the window
        // --- Drag to move window ---
        titleBar.setOnMousePressed((MouseEvent e) -> {
            offsetX = e.getSceneX();
            offsetY = e.getSceneY();
        });

        titleBar.setOnMouseDragged((MouseEvent e) -> {
            if (isMaximized) return; // jangan bisa di-drag saat fullscreen
            primaryStage.setX(e.getScreenX() - offsetX);
            primaryStage.setY(e.getScreenY() - offsetY);
        });

        // --- Monitor Change Detection ---
        ChangeListener<Number> positionListener = (obs, oldVal, newVal) -> {
            Screen newScreen = getCurrentScreen(primaryStage);
            if (!newScreen.equals(currentScreen)) {
                currentScreen = newScreen;
                if (isMaximized) {
                    setStageToScreen(primaryStage, currentScreen);
                }
            }
        };

        primaryStage.xProperty().addListener(positionListener);
        primaryStage.yProperty().addListener(positionListener);

        // === Sidebar ===
        VBox sidebar = new VBox(2);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(4));

        VBox topButtons = new VBox(2, btnOpen, btnFree, btnObstacle, btnLine, btnRemoveLine, btnSave, btnRun, btnPlayPause, btnReplay);
        topButtons.setAlignment(Pos.TOP_CENTER);

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        statusLabel = new Label("");

        VBox bottomButtons = new VBox(2, progressIndicator, btnResetEnvironment);
        bottomButtons.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(topButtons, Priority.ALWAYS);
        sidebar.getChildren().addAll(topButtons, bottomButtons);

        // set on action for buttons
        btnOpen.setOnAction(this::handleButtonOpen);
        btnFree.setOnAction(this::handleButtonFree);
        btnObstacle.setOnAction(this::handleButtonObstacle);
        btnLine.setOnAction(this::handleButtonLine);
        btnRemoveLine.setOnAction(this::handleButtonRemoveLine);
        btnSave.setOnAction(this::handleButtonSave);
        btnRun.setOnAction(this::handleButtonRun);
        btnPlayPause.setOnAction(this::handleButtonPlayPause);
        btnReplay.setOnAction(this::handleButtonReplay);
        btnResetEnvironment.setOnAction(this::handleButtonResetEnvironment);

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
        logArea = new TextArea();
        logArea.getStyleClass().add("dark-textarea");
        logArea.setPrefRowCount(3);
        logArea.setWrapText(false);
        logArea.setEditable(true);
        logArea.setBorder(null);
        logArea.setText("Simulator start...\n" + help);

        // Bungkus dalam ScrollPane
        ScrollPane logScrollPane = new ScrollPane(logArea);
        logScrollPane.getStyleClass().add("dark-scrollpane");
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFitToHeight(true);
        logScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        logScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // === Status Bar ===
        statusLabel = new Label("Ready.");
        statusLabel.getStyleClass().add("status-label");
        statelabel = new Label("STATE: " + state.toString() + "\t-\t");
        statelabel.getStyleClass().add("status-label");
        HBox statusBar = new HBox(statelabel, statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));

        // SplitPane vertikal
        SplitPane splitPane = new SplitPane(scrollPane, logScrollPane);
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
        //primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.initStyle(StageStyle.UNDECORATED);
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
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
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
                gc.fillText(name + "(" + (indexI + 1) + "," + (indexJ + 1) + ")", droneX + cellSize, droneY - cellSize);
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
