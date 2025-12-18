package swarmdronesimulator.swarmdronesimulator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Drone {
    public int indexI, indexJ, sensoreFoV, cameraFoV, cellSize;
    public String name = "";

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