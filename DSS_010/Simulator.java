package com.fosalgo.sim_javafx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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

    private final int numberOfDrones = 6;// DRONES
    private final int sensoreFoV = 6; // DRONES, satuan: cell
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
    private final int numRows = 100;// GRID
    private final int numCols = 200;// GRID
    private final int cellSize = 10;// GRID
    private final double halfCellSize = 0.5 * cellSize;// GRID
    private final int[][] environment = new int[numRows][numCols];// GRID
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

    private int moveToI = -1;// LINE
    private int moveToJ = -1;// LINE
    private int lineToI = -1;// LINE
    private int lineToJ = -1;// LINE

    private int MIN_ROW, MIN_COL, MAX_ROW, MAX_COL;// EXPLORATION
    private int[][] map = null;// EXPLORATION
    private int[][] base_map = null;// EXPLORATION
    private int explored = 0;// EXPLORATION
    private int mustBeExplored = 0;// EXPLORATION
    private ArrayList<Point>[] frontiers = null;// EXPLORATION
    private boolean running = false;// EXPLORATION - run the algorithm to explore

    private int frame = -1;// FRAME
    private int framePeriod = 0;
    private final int MAX_FRAME_PERIOD = 4;
    private ArrayList<Point>[] pathOfDrones = null;
    private final Color[] pathColor = {
            Color.valueOf("#FF0B55"),
            Color.valueOf("#9BEC00"),
            Color.valueOf("#00F5FF"),
            Color.valueOf("#FCE700"),
            Color.valueOf("#AA2EE6"),
            Color.valueOf("#0046FF")
    };

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

    private void handleKeyPressed(KeyEvent e) {
        String code = e.getCode().toString();
        inputKeyboard.add(code);
        System.out.println(inputKeyboard);
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

            if (inputKeyboard.contains("W")) {
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

    private void handleScroll(ScrollEvent scrollEvent) {
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
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
            if (moveToI != -1 && moveToJ != -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                lineToI = i;
                lineToJ = j;
            }
        } else if (inputKeyboard.contains("DIGIT1")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(0).setIndex(i, j);
            }
        } else if (inputKeyboard.contains("DIGIT2")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(1).setIndex(i, j);
            }
        } else if (inputKeyboard.contains("DIGIT3")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(2).setIndex(i, j);
            }
        } else if (inputKeyboard.contains("DIGIT4")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(3).setIndex(i, j);
            }
        } else if (inputKeyboard.contains("DIGIT5")) {
            if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                int i = (int) Math.floor((y - translateY) / cSize);
                int j = (int) Math.floor((x - translateX) / cSize);
                drones.get(4).setIndex(i, j);
            }
        } else if (inputKeyboard.contains("DIGIT6")) {
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
        if (inputKeyboard.contains("L") || inputKeyboard.contains("K")) {
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

    private void initializeDrones(int numberOfDrone) {
        drones = new ArrayList<>();
        int row = 1 + sensoreFoV;
        for (int i = 0; i < numberOfDrone; i++) {
            Drone drone = new Drone(row, 10, sensoreFoV, cameraFoV, cellSize);
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
            System.out.println(unique);
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

                if (row >= 0 && row < map.length && col >= 0 && col < map[0].length && map[row][col] <= 0) {
                    if (map[row][col] == 0) {
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

    private void runFrontierBasedExploration() {// EXPLORATION
        map = getMap();
        activeDrones = setDrone();
        if (activeDrones != null && !activeDrones.isEmpty()) {
            mustBeExplored = getMustBeExplored();
            initializeExploration();
            detectFrontiers();
            for (int i = 0; i < frontiers.length; i++) {
                System.out.println("FRONTIERS[" + i + "]" + frontiers[i]);
            }
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
                            }
                        }
                    }
                    //detect frontiers again
                    detectFrontiers();
                }
            }
        }
        System.out.println("EXPLORED        : " + explored);
        System.out.println("MUST BE EXPLORED: " + mustBeExplored);
        running = false;
        visualized = true;
        frame = 0;
        render();
    }

    private void openThedarkScreen(int ci, int cj) {
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
                if (row < 0 || row >= darkScreen.length || col < 0 || col >= darkScreen[0].length || environment[row][col] == -1) {
                    break;
                } else {
                    darkScreen[row][col] = 0;
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

        //check keyboard
        if (inputKeyboard.contains("R") && !running) {
            running = true;
            visualized = false;
            frame = -1;
            runFrontierBasedExploration();
        }

        if (inputKeyboard.contains("SPACE")) {
            if (pause) {
                pause = false;
            } else if (!pause) {
                pause = true;
            }
            if (inputKeyboard.contains("CONTROL")) {
                System.out.println("OK");
                pause = false;
                frame = -1;
                visualized = true;
                darkScreen = copyArrayInteger(darkScreenCopy);
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
                        if (darkScreen[i][j] == 1) {
                            double xo = j * cellSize;
                            double yo = i * cellSize;
                            gc.setFill(Color.rgb(0, 0, 0, 0.5));
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
                        openThedarkScreen(point0.i + MIN_ROW, point0.j + MIN_COL);
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

            // END OF VISUALIZATION ------------------------------------------------------------------------------------

            gc.restore();
        }// end of check canvas and gc not null

    }

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        initializeDrones(numberOfDrones);// initialize drone
        canvas = new Canvas(numCols * cellSize, numRows * cellSize);
        gc = canvas.getGraphicsContext2D();
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

                // RENDER
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
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
    }
}
