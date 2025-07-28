package com.fosalgo.sim_javafx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.*;

public class Simulator extends Application {

    private final HashSet<String> inputKeyboard = new HashSet<>();
    // GRID
    private final int numRows = 100;
    private final int numCols = 200;
    private final int[][] environment = new int[numRows][numCols];
    private final int cellSize = 10;
    //drone
    private final int sensoreFoV = 6; // satuan: cell
    private final int cameraFoV = 4; // satuan: cell
    private final int droneRadius = 1;
    private final int[][] neighbors8 = {
            {0, 1},/*east*/
            {1, 1},/*southeast*/
            {1, 0},/*south*/
            {1, -1},/*southwest*/
            {0, -1},/*west*/
            {-1, -1},/*northwest*/
            {-1, 0},/*north*/
            {-1, 1},/*northeast*/
    };
    private final int max_s = 4;//animation;invers speed
    private double halfCellSize = 0.5 * cellSize;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateX = 0;
    private double translateY = 0;
    private double scale = 1.0;
    private Canvas canvas;
    private GraphicsContext gc;
    private String[][] wallType = new String[numRows][numCols];
    private int[][]darkScreen = null;
    //Line
    private int beginI = -1;
    private int beginJ = -1;
    private int endI = -1;
    private int endJ = -1;
    private int droneI = 0;
    private int droneJ = 0;
    /*==============================*/
    private int MIN_ROW, MIN_COL, MAX_ROW, MAX_COL;
    private int[][] map = null;
    private int[][] base_map = null;
    private ArrayList<Point> frontiers = new ArrayList<>();
    private int explored = 0;
    private int mustBeExplored = 0;
    private ArrayList<Point> explorationPath = null;// animation
    private ArrayList<Point[]> explorationFrontiers = null;
    private int frame = -1;// animation
    private int s = 0;// animation

    public static void main(String[] args) {
        launch();
    }

    private void update() {
        if (explorationPath != null && frame >= 0 && frame < explorationPath.size()) {
            s++;
            if (s > max_s) {
                s = 0;
                frame++;
            }
        }
    }

    private void render() {
        // render
        if (canvas != null && gc != null) {
            gc = canvas.getGraphicsContext2D();
            gc.setImageSmoothing(true);
            gc.setFontSmoothingType(FontSmoothingType.LCD);
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            gc.save();
            gc.translate(translateX, translateY);
            gc.scale(scale, scale);

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

            halfCellSize = 0.5 * cellSize;

            // draw explorationPath
            if (explorationPath != null && frame >= 0 && frame < explorationPath.size()) {
                Point p = explorationPath.get(frame);
                double xo = p.j * cellSize;
                double yo = p.i * cellSize;
                //gc.setFill(Color.rgb(180, 229, 13, 0.6));
                //gc.fillRect(xo, yo, cellSize, cellSize);

                droneI = p.i;
                droneJ = p.j;


                //open screen
                setFreeDarkScreen(droneI-MIN_ROW,droneJ-MIN_COL);
                if (darkScreen != null) {
                    for (int i = 0; i < darkScreen.length; i++) {
                        for (int j = 0; j < darkScreen[i].length; j++) {
                            if ( darkScreen[i][j] ==1) {
                                double xd = (MIN_COL + j) * cellSize;
                                double yd = (MIN_ROW + i) * cellSize;
                                gc.setFill(Color. rgb(0, 0, 0, 0.6));
                                gc.fillRect(xd, yd, cellSize, cellSize);
                            }
                        }
                    }
                }

                //draw frontiers
                Point[] front = explorationFrontiers.get(frame);
                if (front != null && front.length > 0) {
                    for (int i = 0; i < front.length; i++) {
                        Point f = front[i];
                        double xf = (MIN_COL + f.j) * cellSize;
                        double yf = (MIN_ROW + f.i) * cellSize;
                        gc.setFill(Color.rgb(180, 229, 13, 0.6));
                        gc.fillRect(xf, yf, cellSize, cellSize);
                    }
                }
            }

            // draw frontiers
            /*
            if (frontiers != null) {
                for (Point f : frontiers) {
                    double xo = (MIN_COL + f.j) * cellSize;
                    double yo = (MIN_ROW + f.i) * cellSize;
                    gc.setFill(Color.rgb(180, 229, 13, 0.6));
                    gc.fillRect(xo, yo, cellSize, cellSize);
                }
            }
            */

            // draw explored area
            /*
            if (map != null) {
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[i].length; j++) {
                        if (map[i][j] > 0) {
                            double xo = (MIN_COL + j) * cellSize;
                            double yo = (MIN_ROW + i) * cellSize;
                            gc.setFill(Color.rgb(251, 248, 239, 0.3));
                            gc.fillRect(xo, yo, cellSize, cellSize);

                        }
                    }
                }
            }
            */

            // Draw visibility field
            double droneX = (double) droneJ * cellSize + halfCellSize;
            double droneY = (double) droneI * cellSize + halfCellSize;

            if (droneX >= 0 && droneY >= 0) {
                gc.setFill(Color.rgb(251, 248, 239, 0.3));
                //drawCameraVisibility(droneX, droneY);
                drawSensoreVisibility(droneX, droneY);

                // gambar titikdrone
                //gc.setFill(Color.BLACK);
                //gc.fillOval(droneX - 5, droneY - 5, 10, 10);

                // gambar drone
                gc.setFill(Color.BLACK);
                gc.fillRect(droneX - 1.5, droneY - 1.5, 3, 3);
                gc.setLineWidth(0.4);
                gc.setStroke(Color.BLACK);
                double rx1 = droneX-4;
                double ry1 = droneY-4;
                double rx2 = droneX + 4;
                double ry2 = droneY + 4;
                double rx3 = droneX - 4;
                double ry3 = droneY + 4;
                double rx4 = droneX + 4;
                double ry4 = droneY - 4;
                gc.strokeLine(rx1, ry1, rx2, ry2);
                gc.strokeLine(rx3,ry3,rx4,ry4);
                //propeller
                double rPropeller = 3;
                gc.strokeOval(rx1-rPropeller,ry1-rPropeller,2*rPropeller,2*rPropeller);
                gc.strokeOval(rx2-rPropeller,ry2-rPropeller,2*rPropeller,2*rPropeller);
                gc.strokeOval(rx3-rPropeller,ry3-rPropeller,2*rPropeller,2*rPropeller);
                gc.strokeOval(rx4-rPropeller,ry4-rPropeller,2*rPropeller,2*rPropeller);
            }

            /* draw wall/obstacle */
            for (int i = 0; i < environment.length; i++) {
                for (int j = 0; j < environment[i].length; j++) {
                    if (environment[i][j] == -1) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;
                        //gc.setFill(Color.WHITE);
                        //gc.fillRect(xo, yo, cellSize, cellSize);

                        gc.setStroke(Color.valueOf("#ffffff"));
                        gc.setLineWidth(0.2);
                        gc.strokeLine(xo, yo, xo + cellSize, yo + cellSize);
                        gc.strokeLine(xo + halfCellSize, yo, xo + cellSize, yo + cellSize - halfCellSize);
                        gc.strokeLine(xo, yo + halfCellSize, xo + cellSize - halfCellSize, yo + cellSize);

                        //gc.setLineWidth(0.8);
                        //gc.strokeRect(xo, yo, cellSize, cellSize);

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

            //menggambar garis
            if (beginI != -1 && beginJ != -1 && endI != -1 && endJ != -1) {
                double x0 = beginJ * cellSize + halfCellSize;
                double y0 = beginI * cellSize + halfCellSize;
                double x1 = endJ * cellSize + halfCellSize;
                double y1 = endI * cellSize + halfCellSize;
                gc.setStroke(Color.valueOf("#ffffff"));
                gc.setLineWidth(0.8);
                gc.strokeLine(x0, y0, x1, y1);
            }

            gc.restore();
        }// end of check canvas and gc not null
    }

    private void drawCameraVisibility(double sx, double sy) {
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

    private void drawSensoreVisibility(double sx, double sy) {
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

    private void handleKeyPressed(KeyEvent e) {
        String code = e.getCode().toString();
        inputKeyboard.add(code);
        System.out.println(inputKeyboard);
    }

    private void handleKeyReleased(KeyEvent e) {
        String code = e.getCode().toString();
        inputKeyboard.remove(code);
    }

    private void handleMouseClicked(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double cSize = cellSize * scale;
        if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
            int i = (int) Math.floor((y - translateY) / cSize);
            int j = (int) Math.floor((x - translateX) / cSize);

            if (inputKeyboard.contains("W")) {
                if (environment[i][j] == 0) {
                    environment[i][j] = -1;
                } else {
                    environment[i][j] = 0;
                }
            } else if (inputKeyboard.contains("R")) {
                runFrontierBasedExploration();
            }

            checkWallType();
            render();
        }
    }

    private void handleMousePressed(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
            double cSize = cellSize * scale;
            if (beginI == -1 && beginJ == -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                beginI = i;
                beginJ = j;
                endI = -1;
                endJ = -1;
            }
        } else {
            mouseAnchorX = x;
            mouseAnchorY = y;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        double cSize = cellSize * scale;
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
            if (beginI != -1 && beginJ != -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                endI = i;
                endJ = j;
            }
        } else if (inputKeyboard.contains("D")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                droneI = i;
                droneJ = j;
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

    private void handleMouseReleased(MouseEvent event) {
        if (beginI != -1 && beginJ != -1 && endI != -1 && endJ != -1) {
            double cSize = cellSize * scale;
            double hcSize = 0.5 * cSize;
            double x0 = beginJ * cSize + hcSize;
            double y0 = beginI * cSize + hcSize;
            double x1 = endJ * cSize + hcSize;
            double y1 = endI * cSize + hcSize;

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
                    if (inputKeyboard.contains("L")) {
                        environment[row][col] = -1;
                    } else if (inputKeyboard.contains("K")) {
                        environment[row][col] = 0;
                    }
                }
                x += dx * step;
                y += dy * step;
            }
            checkWallType();
        }
        //reset
        beginI = -1;
        beginJ = -1;
        endI = -1;
        endJ = -1;
        render();
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = 1.05;
        double oldScale = scale;

        if (event.getDeltaY() > 0) {
            scale *= zoomFactor;
        } else {
            scale /= zoomFactor;
        }

        // Zoom terfokus pada posisi mouse
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Penyesuaian translasi agar zoom fokus ke pointer
        translateX = mouseX - (mouseX - translateX) * (scale / oldScale);
        translateY = mouseY - (mouseY - translateY) * (scale / oldScale);

        render();
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        canvas = new Canvas(numCols * cellSize, numRows * cellSize);
        gc = canvas.getGraphicsContext2D();
        render();
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleScroll);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        canvas.setOnMouseReleased(this::handleMouseReleased);

        borderPane.setCenter(canvas);
        root.getChildren().addAll(borderPane);

        /* STRART the ENGINE */
        AnimationTimer engine = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // UPDATE
                update();

                // DRAW
                render();
            }
        };
        engine.start();

        // stage
        stage.setTitle("Lab. Control and Robotic - Mechanical Engineering - Hasanuddin University");
        stage.initStyle(StageStyle.DECORATED);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void setBorders() {
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

    private int[][] getMap() {
        setBorders();
        int nrows = MAX_ROW - MIN_ROW + 1;
        int ncols = MAX_COL - MIN_COL + 1;
        map = null;
        base_map = null;
        darkScreen = null;
        if (nrows > 0 && ncols > 0) {
            map = new int[nrows][ncols];
            base_map = new int[nrows][ncols];
            darkScreen = new int[nrows][ncols];
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    map[i][j] = environment[i + MIN_ROW][j + MIN_COL];
                    base_map[i][j] = environment[i + MIN_ROW][j + MIN_COL];
                    darkScreen[i][j] = environment[i + MIN_ROW][j + MIN_COL];
                    if(environment[i + MIN_ROW][j + MIN_COL]==0){
                        darkScreen[i][j] = 1;
                    }
                }
            }
        }
        return map;
    }

    private int getMustBeExplored(int ci, int cj) {
        mustBeExplored = 0;
        if (base_map != null) {
            int[][] mustBeExploredMap = copyArrayInteger(base_map);
            Queue<Point> queue = new LinkedList<Point>();
            queue.offer(new Point(ci, cj));
            mustBeExplored = 1;
            while (!queue.isEmpty()) {
                Point center = queue.poll();
                // check neighbors
                for (int n = 0; n < neighbors8.length; n++) {
                    int i = center.i + neighbors8[n][0];
                    int j = center.j + neighbors8[n][1];
                    if (i >= 0 && i < mustBeExploredMap.length && j >= 0 && j < mustBeExploredMap[0].length && mustBeExploredMap[i][j] == 0) {
                        mustBeExploredMap[i][j] = 1;
                        queue.offer(new Point(i, j));
                        mustBeExplored++;
                    }
                }
            }
        }
        return mustBeExplored;
    }

    private boolean isDroneValid() {
        boolean valid = droneI >= MIN_ROW && droneI <= MAX_ROW && droneJ >= MIN_COL && droneJ <= MAX_COL;
        return valid;
    }

    private void setFreeDarkScreen(int ci, int cj) {
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
                if (row < 0 || row >= darkScreen.length || col < 0 || col >= darkScreen[0].length || darkScreen[row][col] == -1) {
                    break;
                } else {
                    darkScreen[row][col] = 0;
                }
            }
        }
    }

    private void setFree(int ci, int cj) {
        explorationFrontiers.add(arrayListToArrayPoint(frontiers));
        explorationPath.add(new Point(ci + MIN_ROW, cj + MIN_COL));
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
                    Point freePoint = new Point(row, col);
                    if (!frontiers.isEmpty()) {
                        frontiers.remove(freePoint);
                    }
                }
            }

        }
    }

    private ArrayList<Point> detectFrontiers(int ci, int cj) {
        // jika masuk ke dalam jangkauan sensor dan buan obstacle dan bukan wall dan merupakan unknown
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

                if (row >= 0 && row < map.length && col >= 0 && col < map[0].length && map[row][col] <= 0) {
                    if (map[row][col] == 0) {
                        Point frontier = new Point(row, col);
                        if (!frontiers.contains(frontier)) {
                            frontiers.add(frontier);
                        }
                    }
                    break;
                }
            }

        }
        return frontiers;
    }

    private Point findNearestFrontier(int ci, int cj) {
        Point nearest = null;
        double minDist = Double.MAX_VALUE;
        if (frontiers != null) {
            for (int i = frontiers.size() - 1; i >= 0; i--) {
                Point f = frontiers.get(i);
                double dist = Math.abs(f.i - ci) + Math.abs(f.j - cj);
                if (dist <= minDist) {
                    minDist = dist;
                    nearest = f;
                }
            }
        }
        return nearest;
    }

    private Stack<Point> bfsToFrontier(Point begin, Point end) {
        Stack<Point> path = null;
        if (base_map != null) {
            System.out.println("TRACE BFS");
            int[][] mapBFS = copyArrayInteger(base_map);
            int step = 1;
            mapBFS[begin.i][begin.j] = step;
            boolean complete = false;
            Queue<Point> queue = new LinkedList<Point>();
            queue.offer(begin);
            //System.out.println("------------------------------");
            //System.out.println("begin " + begin);
            //System.out.println("end   " + end);

            while (!queue.isEmpty() && !complete) {
                //System.out.println(queue);
                /* check neighbors */
                Point center = queue.poll();
                step = mapBFS[center.i][center.j];
                for (int n = 0; n < neighbors8.length; n++) {
                    int i = center.i + neighbors8[n][0];
                    int j = center.j + neighbors8[n][1];
                    Point newNeighbor = new Point(i, j);
                    if (newNeighbor.equals(end)) {
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
                    } else {
                        if (i >= 0 && i < mapBFS.length && j >= 0 && j < mapBFS[0].length && mapBFS[i][j] == 0) {
                            queue.offer(newNeighbor);
                            mapBFS[i][j] = step + 1;
                        }
                    }
                }
            }
        }
        return path;
    }

    private Point[] arrayListToArrayPoint(ArrayList<Point> list) {
        Point[] arrayFrontiers = null;
        if (list != null && !list.isEmpty()) {
            arrayFrontiers = new Point[list.size()];
            for (int i = 0; i < arrayFrontiers.length; i++) {
                Point point = list.get(i);
                arrayFrontiers[i] = new Point(point.i, point.j);
            }
        }
        return arrayFrontiers;
    }

    private int[][] copyArrayInteger(int[][] array) {
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

    private void runFrontierBasedExploration() {
        frame = -1;
        explorationPath = new ArrayList<>();
        explorationFrontiers = new ArrayList<>();
        getMap();
        frontiers = new ArrayList<>();
        if (isDroneValid()) {
            //System.out.println("---------------------------------------------------");
            //explorationPath.add(new Point(droneI, droneJ));
            int ci = droneI - MIN_ROW;
            int cj = droneJ - MIN_COL;
            explored = 0;
            mustBeExplored = getMustBeExplored(ci, cj);
            //System.out.println("mustBeExplored :" + mustBeExplored);
            setFree(ci, cj);
            detectFrontiers(ci, cj);


            while (!frontiers.isEmpty() && explored < mustBeExplored) {
                Point selectedFrontier = findNearestFrontier(ci, cj);
                frontiers.remove(selectedFrontier);//remove frontier from list
                Point begin = new Point(ci, cj);
                ci = selectedFrontier.i;
                cj = selectedFrontier.j;
                Point end = new Point(ci, cj);
                Stack<Point> path = bfsToFrontier(begin, end);
                //System.out.println("path: " + path);
                if (path != null) {
                    while (!path.isEmpty()) {
                        Point point = path.pop();
                        setFree(point.i, point.j);
                    }
                }
                detectFrontiers(ci, cj);
            }
        }
        frame = 0;
    }


}
