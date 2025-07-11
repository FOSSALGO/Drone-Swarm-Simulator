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

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateX = 0;
    private double translateY = 0;
    private double scale = 1.0;
    private HashSet<String> inputKeyboard = new HashSet<>();

    private Canvas canvas;
    private GraphicsContext gc;

    // GRID
    private int numRows = 100;
    private int numCols = 200;
    private int[][] grid = new int[numRows][numCols];
    private String[][] wallType = new String[numRows][numCols];
    private int cellSize = 10;

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

            /* draw wall/obstacle */
            double halfCellSize = 0.5 * cellSize;
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
        if (x >= translateX && x < translateX + numCols * cellSize && y >= translateY && y < translateY + numRows * cellSize) {
            // System.out.println("IN");
            int i = (int) Math.floor((y - translateY) / (double) (cellSize * scale));
            int j = (int) Math.floor((x - translateX) / (double) (cellSize * scale));
            System.out.println("click(" + i + "," + j + ")");

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
        mouseAnchorX = event.getX();
        mouseAnchorY = event.getY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getX() - mouseAnchorX;
        double deltaY = event.getY() - mouseAnchorY;

        translateX += deltaX;
        translateY += deltaY;

        mouseAnchorX = event.getX();
        mouseAnchorY = event.getY();

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

    public static void main(String[] args) {
        launch();
    }
  
}
