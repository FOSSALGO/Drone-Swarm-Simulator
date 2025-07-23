package com.fosalgo.sim_javafx;/* Final JavaFX Program: Frontier-Based Exploration with FTFD, A*, Heatmap, Logging */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class FrontierExplorer extends Application {

    final int CELL_SIZE = 20;
    final int GRID_WIDTH = 60;
    final int GRID_HEIGHT = 40;
    final int VIEW_RADIUS = 4;
    CellState[][] grid = new CellState[GRID_HEIGHT][GRID_WIDTH];
    boolean[][] visible;
    int[][] visitCount = new int[GRID_HEIGHT][GRID_WIDTH];
    Set<Point> frontiers = new HashSet<>();
    List<Point> pathHistory = new ArrayList<>();
    Queue<Point> currentPath = new LinkedList<>();
    Point robot = new Point(30, 20);
    Canvas canvas;
    GraphicsContext gc;
    double scale = 1.0;
    double offsetX = 0, offsetY = 0;
    double dragStartX, dragStartY;
    long startTime;
    int exploredCells = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        for (int y = 0; y < GRID_HEIGHT; y++)
            Arrays.fill(grid[y], CellState.UNKNOWN);

        canvas = new Canvas(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnScroll(this::handleZoom);
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                dragStartX = e.getX();
                dragStartY = e.getY();
            }
        });
        canvas.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                offsetX += (e.getX() - dragStartX);
                offsetY += (e.getY() - dragStartY);
                dragStartX = e.getX();
                dragStartY = e.getY();
                draw();
            }
        });

        Pane root = new Pane(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Frontier-Based Exploration with A* and Heatmap");
        primaryStage.show();

        startTime = System.currentTimeMillis();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            performRayCasting();
            detectFrontiersFTFD();
            if (currentPath.isEmpty()) {
                Point target = findNearestFrontier();
                if (target != null) currentPath = aStar(robot, target);
            }
            if (!currentPath.isEmpty()) {
                robot = currentPath.poll();
                pathHistory.add(robot);
                visitCount[robot.y][robot.x]++;
            }
            draw();
            logExplorationData();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    void logExplorationData() {
        int newlyExplored = 0;
        for (int y = 0; y < GRID_HEIGHT; y++)
            for (int x = 0; x < GRID_WIDTH; x++)
                if (grid[y][x] == CellState.FREE && visitCount[y][x] == 1) newlyExplored++;
        exploredCells += newlyExplored;
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Time: " + elapsed + "s, Explored: " + exploredCells + " cells");
    }

    void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int maxVisit = Arrays.stream(visitCount).flatMapToInt(Arrays::stream).max().orElse(1);
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                double sx = x * CELL_SIZE * scale + offsetX;
                double sy = y * CELL_SIZE * scale + offsetY;

                if (visitCount[y][x] > 0) {
                    double ratio = (double) visitCount[y][x] / maxVisit;
                    gc.setFill(Color.color(1.0, 1.0 - ratio, 1.0 - ratio));
                } else {
                    switch (grid[y][x]) {
                        case UNKNOWN -> gc.setFill(Color.LIGHTGRAY);
                        case FREE -> gc.setFill(Color.WHITE);
                        case OBSTACLE -> gc.setFill(Color.BLACK);
                        case FRONTIER -> gc.setFill(Color.ORANGE);
                    }
                }
                gc.fillRect(sx, sy, CELL_SIZE * scale, CELL_SIZE * scale);
                gc.setStroke(Color.GRAY);
                gc.strokeRect(sx, sy, CELL_SIZE * scale, CELL_SIZE * scale);
            }
        }

        gc.setStroke(Color.RED);
        for (int i = 1; i < pathHistory.size(); i++) {
            Point p1 = pathHistory.get(i - 1);
            Point p2 = pathHistory.get(i);
            double x1 = p1.x * CELL_SIZE * scale + offsetX + CELL_SIZE * scale / 2;
            double y1 = p1.y * CELL_SIZE * scale + offsetY + CELL_SIZE * scale / 2;
            double x2 = p2.x * CELL_SIZE * scale + offsetX + CELL_SIZE * scale / 2;
            double y2 = p2.y * CELL_SIZE * scale + offsetY + CELL_SIZE * scale / 2;
            gc.strokeLine(x1, y1, x2, y2);
        }

        gc.setFill(Color.BLUE);
        double rx = robot.x * CELL_SIZE * scale + offsetX;
        double ry = robot.y * CELL_SIZE * scale + offsetY;
        gc.fillOval(rx, ry, CELL_SIZE * scale, CELL_SIZE * scale);
    }

    void performRayCasting() {
        visible = new boolean[GRID_HEIGHT][GRID_WIDTH];
        for (int a = 0; a < 360; a += 2) {
            double rad = Math.toRadians(a);
            double dx = Math.cos(rad), dy = Math.sin(rad);
            double x = robot.x + 0.5, y = robot.y + 0.5;
            for (int r = 0; r < VIEW_RADIUS; r++) {
                int ix = (int) x, iy = (int) y;
                if (!inBounds(ix, iy)) break;
                visible[iy][ix] = true;
                if (grid[iy][ix] == CellState.OBSTACLE) break;
                grid[iy][ix] = CellState.FREE;
                x += dx;
                y += dy;
            }
        }
    }

    void detectFrontiersFTFD() {
        frontiers.clear();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (visible[y][x] && grid[y][x] == CellState.FREE && isFrontier(x, y)) {
                    grid[y][x] = CellState.FRONTIER;
                    frontiers.add(new Point(x, y));
                }
            }
        }
    }

    boolean isFrontier(int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (inBounds(nx, ny) && grid[ny][nx] == CellState.UNKNOWN)
                    return true;
            }
        }
        return false;
    }

    Point findNearestFrontier() {
        Point nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Point f : frontiers) {
            int dist = Math.abs(f.x - robot.x) + Math.abs(f.y - robot.y);
            if (dist < minDist) {
                minDist = dist;
                nearest = f;
            }
        }
        return nearest;
    }

    Queue<Point> aStar(Point start, Point goal) {
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>();
        PriorityQueue<Point> open = new PriorityQueue<>(Comparator.comparingInt(p -> gScore.get(p) + heuristic(p, goal)));
        Set<Point> closed = new HashSet<>();

        gScore.put(start, 0);
        open.add(start);

        while (!open.isEmpty()) {
            Point current = open.poll();
            if (current.equals(goal)) {
                LinkedList<Point> path = new LinkedList<>();
                while (cameFrom.containsKey(current)) {
                    path.addFirst(current);
                    current = cameFrom.get(current);
                }
                return path;
            }
            closed.add(current);

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (Math.abs(dx) + Math.abs(dy) != 1) continue;
                    int nx = current.x + dx;
                    int ny = current.y + dy;
                    Point neighbor = new Point(nx, ny);
                    if (!inBounds(nx, ny) || grid[ny][nx] == CellState.OBSTACLE || closed.contains(neighbor)) continue;

                    int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                    if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        cameFrom.put(neighbor, current);
                        gScore.put(neighbor, tentativeG);
                        open.add(neighbor);
                    }
                }
            }
        }
        return new LinkedList<>();
    }

    int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    boolean inBounds(int x, int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    void handleMouseClick(MouseEvent e) {
        int x = (int) ((e.getX() - offsetX) / (CELL_SIZE * scale));
        int y = (int) ((e.getY() - offsetY) / (CELL_SIZE * scale));
        if (!inBounds(x, y)) return;
        if (grid[y][x] == CellState.OBSTACLE)
            grid[y][x] = CellState.UNKNOWN;
        else
            grid[y][x] = CellState.OBSTACLE;
        draw();
    }

    void handleZoom(ScrollEvent e) {
        double delta = e.getDeltaY() > 0 ? 1.1 : 0.9;
        scale *= delta;
        draw();
    }

    enum CellState {UNKNOWN, FREE, OBSTACLE, FRONTIER}

    static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object o) {
            return o instanceof Point p && p.x == x && p.y == y;
        }

        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
