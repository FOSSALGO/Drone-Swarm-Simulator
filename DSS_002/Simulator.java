package com.fosalgo.sim_javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Simulator extends Application {

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateX = 0;
    private double translateY = 0;
    private double scale = 1.0;

    private Canvas canvas;
    private GraphicsContext gc;

    // GRID
    private int numRows = 100;
    private int numCols = 200;
    private int[][] grid = new int[numRows][numCols];
    private int cellSize = 10;

    private void render() {
        // render
        if(canvas!=null && gc!=null){
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(true);
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.save();
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

            gc.setFill(Color.valueOf("#283d64"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            int cellSize = 10;
            int numRows = (int) Math.floor(canvas.getHeight()/(double)cellSize);
            int numCols = (int) Math.floor(canvas.getWidth()/(double)cellSize);

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

            gc.restore();
        }// end of check canvas and gc not null
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
    public void start(Stage stage){
        StackPane root = new StackPane();
        Scene scene =new Scene(root);
        canvas = new Canvas(numCols*cellSize,numRows*cellSize);
        gc = canvas.getGraphicsContext2D();
        render();
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleScroll);

        Pane  canvasPane = new Pane();
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
