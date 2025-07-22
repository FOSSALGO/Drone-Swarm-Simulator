package com.fosalgo.sim_javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.HashSet;

public class Simulator extends Application {

    private final HashSet<String> inputKeyboard = new HashSet<>();
    // GRID
    private final int numRows = 100;
    private final int numCols = 200;
    private final int[][] grid = new int[numRows][numCols];
    private final int cellSize = 10;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateX = 0;
    private double translateY = 0;
    private double scale = 1.0;
    private Canvas canvas;
    private GraphicsContext gc;
    private String[][] wallType = new String[numRows][numCols];
    //Line
    private int beginI = -1;
    private int beginJ = -1;
    private int endI = -1;
    private int endJ = -1;
    //drone
    private double maxViewDistance = 10; // satuan: cell
    private int droneI = 0;
    private int droneJ = 0;

    public static void main(String[] args) {
        launch();
    }

    private void drawVisibility(GraphicsContext gc, double sx, double sy) {
        int rays = 360;
        double[] xPoints = new double[rays];
        double[] yPoints = new double[rays];
        double cSize = cellSize;

        for (int i = 0; i < rays; i++) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);

            double x = sx;
            double y = sy;
            double step = 0.01;//epsilon resolusi ray
            double traveled = 0;
            double MAX_RADIUS = maxViewDistance * cellSize;
            while (traveled < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                traveled += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row < 0 || row >= numRows || col < 0 || col >= numCols || grid[row][col] == -1) {
                    break;
                }
            }
            xPoints[i] = x;
            yPoints[i] = y;

        }
        gc.fillPolygon(xPoints, yPoints, rays);
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

            // Draw visibility field
            double halfCellSize = 0.5 * cellSize;
            int droneX = (int) (droneJ * cellSize + halfCellSize);
            int droneY = (int) (droneI * cellSize + halfCellSize);

            if (droneX >= 0 && droneY >= 0) {
                gc.setFill(Color.rgb(225, 68, 52, 0.3));// red semi-transparent
                drawVisibility(gc, droneX, droneY);
                // gambar titikdrone
                gc.setFill(Color.BLACK);
                gc.fillOval(droneX - 5, droneY - 5, 10, 10);
            }

            /* draw wall/obstacle */
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    if (grid[i][j] == -1) {
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

    private String checkWallType(int i, int j) {
        String type = null;
        if (grid != null && i >= 0 && i < grid.length && j >= 0 && j < grid[0].length && grid[i][j] == -1) {
            type = "";

            // check EAST
            String value = "0";
            if (j == grid[i].length - 1 || (j < grid[i].length - 1 && grid[i][j + 1] == 0)) {
                value = "1";
            }
            type += value;

            // check SOUTH
            value = "0";
            if (i == grid.length - 1 || (i < grid.length - 1 && grid[i + 1][j] == 0)) {
                value = "1";
            }
            type += value;

            // check WEST
            value = "0";
            if (j == 0 || (j > 0 && grid[i][j - 1] == 0)) {
                value = "1";
            }
            type += value;

            // check NORTH
            value = "0";
            if (i == 0 || (i > 0 && grid[i - 1][j] == 0)) {
                value = "1";
            }
            type += value;

        }
        return type;
    }

    private void checkWallType() {
        if (grid != null) {
            wallType = new String[grid.length][];
            for (int i = 0; i < grid.length; i++) {
                wallType[i] = new String[grid[i].length];
                for (int j = 0; j < grid[i].length; j++) {
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
                if (grid[i][j] == 0) {
                    grid[i][j] = -1;
                } else {
                    grid[i][j] = 0;
                }
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
            double halfCSize = 0.5 * cSize;
            double x0 = beginJ * cSize + halfCSize;
            double y0 = beginI * cSize + halfCSize;
            double x1 = endJ * cSize + halfCSize;
            double y1 = endI * cSize + halfCSize;

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
                        grid[row][col] = -1;
                    } else if (inputKeyboard.contains("K")) {
                        grid[row][col] = 0;
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

        Pane canvasPane = new Pane();
        canvasPane.getChildren().add(canvas);
        root.getChildren().addAll(canvasPane);

        // stage
        stage.setTitle("Simulator Drone 2D");
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
}
