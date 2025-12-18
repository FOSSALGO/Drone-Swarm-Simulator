package swarmdronesimulator.swarmdronesimulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

    private final String delimiter = ":";// FILE

    // Button
    private final Button btnNew = createIconButton(SVGIcon.newEnv, 1.0, 1.0, "New Environment");// VISUALIZATION
    private final Button btnOpen = createIconButton(SVGIcon.openEnv, 1.0, 1.0, "Open Environment");// VISUALIZATION
    private final Button btnSave = createIconButton(SVGIcon.saveEnv, 1.0, 1.0, "Save Environment");// VISUALIZATION
    private final Button btnTransform = createIconButton(SVGIcon.transform, 1.0, 1.0, "Transform");// VISUALIZATION
    private final Button btnDrone = createIconButton(SVGIcon.drone, 1.0, 1.0, "Drone");// VISUALIZATION
    private final Button btnWall = createIconButton(SVGIcon.wall, 1.0, 1.0, "Wall");// VISUALIZATION
    private final Button btnDrawLine = createIconButton(SVGIcon.drawLine, 1.0, 1.0, "Draw Line");// VISUALIZATION
    private final Button btnRemoveLine = createIconButton(SVGIcon.removeLine, 1.0, 1.0, "Remove Line");// VISUALIZATION
    private final Button btnRun = createIconButton(SVGIcon.run, 1.0, 1.0, "Run");// VISUALIZATION
    private final Button btnPlayPause = createIconButton(SVGIcon.playPause, 1.0, 1.0, "Play - Pause");// VISUALIZATION
    private final Button btnReplay = createIconButton(SVGIcon.replay, 1.0, 1.0, "Replay");// VISUALIZATION

    private final int sensoreFoV = 5; // DRONES, satuan: cell ; 1 cell = 10 cm x 10 cm
    private final int cameraFoV = 3;// DRONES, satuan: cell
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

    private final int MAX_NUM_ROWS = 200;// GRID
    private final int MAX_NUM_COLS = 400;// GRID
    private Stage primaryStage = null;
    private FileChooser fileChooser = new FileChooser();// VISUALIZATION
    private int numberOfDrones = 3;// DRONES
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
    private final double offsetX = 0;
    private final double offsetY = 0;// VISUALIZATION
    private final boolean isMaximized = true;// VISUALIZATION
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

    // Create button
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
        statelabel.setText("STATE: " + state + "\t-\t");
    }

    private void handleButtonDrone(ActionEvent actionEvent) {
        state = State.DEFAULT;
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
        statelabel.setText("STATE: " + state + "\t-\t");
    }

    private void handleButtonObstacle(ActionEvent actionEvent) {
        state = State.OBSTACLE;
        statelabel.setText("STATE: " + state + "\t-\t");
    }

    private void handleButtonLine(ActionEvent actionEvent) {
        state = State.LINE;
        statelabel.setText("STATE: " + state + "\t-\t");
    }

    private void handleButtonRemoveLine(ActionEvent actionEvent) {
        state = State.REMOVE_LINE;
        statelabel.setText("STATE: " + state + "\t-\t");
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
            statelabel.setText("STATE: " + state + "\t-\t");
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

    // ALGORITHM

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

    private Point[][] aStarToNearestfrontier() {
        Point[][] pathToNearestFrontier = null;
        if (activeDrones != null && !activeDrones.isEmpty() && frontiers != null && frontiers.length > 0) {
            pathToNearestFrontier = new Point[activeDrones.size()][];
            for (int d = 0; d < activeDrones.size(); d++) { // mencari path to nearest frontier untuk setiao drone aktif
                double MIN_DISTANCE = Double.MAX_VALUE;
                NodeAStar MIN_NODE = null;
                ArrayList<Point> frontiersDrone = frontiers[d];
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int ci = position.i - MIN_ROW;
                int cj = position.j - MIN_COL;
                Point startPoint = new Point(ci, cj);
                for(Point point : frontiersDrone){
                    Point finishPoint = point;
                    if (base_map != null) {
                        int[][] mapAStar = copyArrayInteger(base_map);
                        // initialize open list and closed list
                        ArrayList<NodeAStar>openList = new ArrayList<>();
                        ArrayList<NodeAStar>closedList = new ArrayList<>();
                        double gStart = 0;
                        double hStart = Math.sqrt(Math.pow((finishPoint.i-startPoint.i),2)+Math.pow((finishPoint.j-startPoint.j),2));
                        NodeAStar startingNode = new NodeAStar(null, ci, cj, gStart, hStart);
                        openList.add(startingNode);
                        while_openlist_not_empty:
                        while(!openList.isEmpty()){
                            // find the node with least f on the open list, call it leastFNode
                            NodeAStar leastFNode = openList.getFirst();
                            for (int i = 1; i < openList.size(); i++) {
                                NodeAStar node = openList.get(i);
                                if(node.f<leastFNode.f){
                                    leastFNode = node;
                                }
                            }

                            // generate leastFNode's 8 successor
                            for (int n = 0; n < neighbors8.length; n++) {
                                int row = leastFNode.row + neighbors8[n][0];
                                int col = leastFNode.col + neighbors8[n][1];

                                // validasi cell pada posisi row col
                                boolean isValid = false;
                                if(row >= 0 && row < mapAStar.length && col >= 0 && col < mapAStar[0].length && mapAStar[row][col] == 0){
                                    isValid = true;
                                }

                                if(isValid){
                                    Point newNeighbor = new Point(row, col);
                                    // if newNeighbor is finishPoint, stop search
                                    if(newNeighbor.equals(finishPoint)){
                                        double distance = leastFNode.g + Math.sqrt(Math.pow(neighbors8[n][0],2)+Math.pow(neighbors8[n][1],2));// use euclidean distance to neighbor
                                        if(distance<MIN_DISTANCE){
                                            MIN_DISTANCE = distance;
                                            MIN_NODE = new NodeAStar(leastFNode,row,col,distance,0);
                                        }
                                        break while_openlist_not_empty;
                                    }else{
                                        // compute f = g + h
                                        double g = leastFNode.g + Math.sqrt(Math.pow(neighbors8[n][0],2)+Math.pow(neighbors8[n][1],2));// use euclidean distance to neighbor
                                        double h = Math.sqrt(Math.pow((row-finishPoint.i),2)+Math.pow((col-finishPoint.j),2));
                                        NodeAStar node = new NodeAStar(leastFNode,row,col,g,h);

                                        // cek node
                                        boolean skip = false;
                                        // 1. if a node with the same position as successor is in the OPEN list which has a lower f than successor, skip this successor
                                        for (int i = 0; i < openList.size(); i++) {
                                            NodeAStar nodeFromOpenList = openList.get(i);
                                            if(node.row==nodeFromOpenList.row&&node.col==nodeFromOpenList.col&&node.f<nodeFromOpenList.f){
                                                skip = true;
                                                break;
                                            }
                                        }
                                        // 2. if a node with the same position as successor  is in the CLOSED list which has a lower f than successor, skip this successor
                                        if(!skip){
                                            for (int i = 0; i < closedList.size(); i++) {
                                                NodeAStar nodeFromCloseList = closedList.get(i);
                                                if(node.row==nodeFromCloseList.row&&node.col==nodeFromCloseList.col&&node.f<nodeFromCloseList.f){
                                                    skip = true;
                                                    break;
                                                }
                                            }
                                        }
                                        // 3. otherwise, add  the node to the open list
                                        if(!skip){
                                            openList.add(node);// node baru ditambahkan ke open list
                                        }
                                    }
                                }

                            }// end of for

                            // pop leastFNode from open list
                            openList.remove(leastFNode);

                            // push leastFNode on the closed list
                            closedList.add(leastFNode);
                        }// end of while
                    }
                }

                // save best path
                if(MIN_NODE!=null){
                    Stack<Point>path = new Stack<>();
                    NodeAStar node = MIN_NODE;
                    while(true){
                        if(node.parent==null){
                            break;
                        }else{
                            path.push(new Point(node.row,node.col));
                            node = node.parent;
                        }
                    }

                    pathToNearestFrontier[d] = new Point[path.size()];
                    for (int k = 0; k < path.size(); k++) {
                        pathToNearestFrontier[d][k] = path.pop();
                    }
                    System.out.println("TRACE :"+Arrays.toString(pathToNearestFrontier[d]));
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
    // END OF ALGORITHM

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
                            //Point[][] pathToNearestFrontier = bfsToNearestfrontier();
                            Point[][] pathToNearestFrontier = aStarToNearestfrontier();
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

            /* draw gradient */
            // Radial Gradient dengan center semi-transparan
            /*
            RadialGradient gradient = new RadialGradient(
                    0, 0,
                    canvas.getWidth() / 2,
                    canvas.getHeight() / 2,
                    canvas.getWidth() / 2,//radius
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.color(1, 1, 1, 0.05)), // putih transparan
                    new Stop(1.0, Color.valueOf("#283d64"))
            );
            gc.setFill(gradient);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            */

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
        btnDrone.setOnAction(this::handleButtonDrone);
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
        scene.getStylesheets().add(CSS.style);
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
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
    }
}
