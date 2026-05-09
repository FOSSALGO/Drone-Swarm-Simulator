package dsde.autonomousswarmdroneexploration;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class Drone {
    private int[][] neighbors;
    //untuk gambar drone
    protected int initialRowPosition; // posisi awal baris drone di map global
    protected int initialColPosition; // posisi awal kolom drone di map global
    protected int rowPosition; // posisi baris drone di map global
    protected int colPosition; // posisi kolom drone di map global
    protected int sensorFoV; // Field of View sensor range finder / perimeter
    protected int cameraFoV; // Field of View Camera
    protected int cellSize; // lebar cell grid
    protected double scale; // scala canvas
    protected String name = ""; // nama drone
    protected ArrayList<Point> path;
    protected int frameIndex = 0;// index animasi untuk menggambar drone berdasarkan timestep di path
    protected boolean active = false;
    // algorithm
    protected ArrayList<Point> frontier = new ArrayList<>();// frontier tracing frontier detection
    protected int[][] environment;// duplicate copy (reference) of global environment (grid original yang tidak diganggu dalam proses algoritma)
    protected int[][] map;// global map obstale = -1; unknown/unexplored = 0 ; explored/known/free = value>=1
    protected int[][]mustBeExploredArea;
    protected double[][] pheromoneGrid;
    protected double[][]deltaTau;// delta pheromone
    protected double alpha = 0.5;
    protected double beta = 0.5;
    // pheromone
    /*
    | Parameter     | Range       |
    | ------------- | ----------- |
    | Q (deposit)   | 0.5  – 2    |
    | ρ (decay)     | 0.03 – 0.08 |
    | D (diffusion) | 0.05 – 0.15 |
    | P_max         | 5    – 20   |
    */
    protected double Q = 1.0;

    public Drone(int[][] environment, int initialRowPosition, int initialColPosition, int sensorFoV, int cameraFoV, int cellSize, double scale) {
        this.environment = environment;
        this.setPosition(initialRowPosition, initialColPosition);
        this.sensorFoV = sensorFoV;
        this.cameraFoV = cameraFoV;
        this.cellSize = cellSize;
        this.scale = scale;
    }

    public void setNeighbors(int[][] neighbors) {
        this.neighbors = neighbors;
    }

    protected void setPosition(int initialRowPosition, int initialColPosition) {
        this.initialRowPosition = initialRowPosition;
        this.initialColPosition = initialColPosition;
        initializePosition();
    }

    protected void initializePosition(){
        this.rowPosition = this.initialRowPosition;
        this.colPosition = this.initialColPosition;
        this.active = false;
        this.path = new ArrayList<>();
        this.path.add(new Point(this.initialRowPosition, this.initialColPosition));
    }

    protected void setName(String name) {
        this.name = name;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }

    public void setPheromoneGrid(double[][] pheromoneGrid) {
        this.pheromoneGrid = pheromoneGrid;
    }

    public void setMustBeExploredArea(int[][] mustBeExploredArea) {
        this.mustBeExploredArea = mustBeExploredArea;
    }

    public void setDeltaTau(double[][] deltaTau) {
        this.deltaTau = deltaTau;
    }

    // VISUALIZATION ================================================================================
    protected void update() {
        if (active) {
            if (frameIndex < path.size() - 1) {
                frameIndex++;
                this.rowPosition = path.get(frameIndex).getRow();
                this.colPosition = path.get(frameIndex).getCol();
            }
        }
    }

    protected void draw(GraphicsContext gc) {
        double halfCellSize = 0.5 * cellSize;
        if (frameIndex == 0) {
            this.rowPosition = initialRowPosition;
            this.colPosition = initialColPosition;
        }
        double droneX = (double) this.colPosition * cellSize + halfCellSize;
        double droneY = (double) this.rowPosition * cellSize + halfCellSize;
        if (droneX >= 0 && droneY >= 0) {
            gc.setFill(Color.valueOf("#FF7D29"));
            drawCameraVisibility(gc, droneX, droneY);
            drawSensoreVisibility(gc, droneX, droneY);

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
            gc.setGlobalAlpha(1.0);
            gc.setFont(Font.font("Arial", 8));
            //gc.setFill(Color.WHITE);
            gc.setFill(Color.valueOf("#FF0087"));
            gc.fillText(name + " (" + (this.rowPosition + 1) + "," + (this.colPosition + 1) + ")", droneX + cellSize, droneY - cellSize);
        }
    }

    protected void drawCameraVisibility(GraphicsContext gc, double sx, double sy) {
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
        gc.setGlobalAlpha(0.5);
        gc.fillPolygon(xPoints, yPoints, rays);
        gc.setGlobalAlpha(1.0);
    }

    protected void drawSensoreVisibility(GraphicsContext gc, double sx, double sy) {
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
            double MAX_RADIUS = (double) sensorFoV * cSize + hcSize;
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
        gc.setGlobalAlpha(0.3);
        gc.fillPolygon(xPoints, yPoints, rays);
        gc.setGlobalAlpha(1.0);
    }

    @Override
    public String toString() {
        return "Drone{" +
                "row=" + rowPosition +
                ", col=" + colPosition +
                '}';
    }


    // ALGORITHM ====================================================================================
    protected void detectFrontiers() {//sensing
        //set ci cj
        Point last = path.getLast();
        int ci = last.getRow();
        int cj = last.getCol();

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
            double MAX_RADIUS = (double) sensorFoV * cSize + cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);

                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] <= 0) {
                    if (row >= 0 && row < map.length && col >= 0 && col < map[0].length && map[row][col] == 0) {
                        Point frontierCell = new Point(row, col);
                        boolean duplicate = false;
                        if (frontier.contains(frontierCell)) {
                            duplicate = true;
                            break;
                        }
                        if (!duplicate) {
                            //System.out.println("tidak sama");
                            frontier.add(frontierCell);// Memoisasi Frontier sebagai bagian dari teknik FTFD
                        }
                    }
                    break;
                }
            }
        }
        //System.out.println(frontier);
    }

    protected void frontierValidation() {
        if (frontier != null && !frontier.isEmpty()) {
            for (int i = frontier.size() - 1; i >= 0; i--) {
                Point f = frontier.get(i);
                if (map[f.getRow()][f.getCol()] > 0||mustBeExploredArea[f.getRow()][f.getCol()]==0) {
                    frontier.remove(f);
                }
            }
        }
    }

    protected boolean isThereStillAFrontier(){
        if(frontier==null||frontier.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    protected void pathPlanningToTheNearestFrontier() { //decision using digital pheromone stigmegy mechanism
       Stack<Point> stack = new Stack<>();
        ArrayList<Point> visited = new ArrayList<>();
        Point last = path.getLast();
        stack.push(last);
        visited.add(last);

        while (frontier != null && !frontier.isEmpty() && !stack.isEmpty()) {
            Point origin = stack.peek();
            if (frontier.contains(origin)) { /*cek apakah origin merupakan frontier*/
                // path to nearest frontier ditemukan, save path, lalu break
                if (stack.size() > 1) {
                    for (int i = 1; i < stack.size(); i++) {
                        Point point = stack.get(i);
                        path.add(point);
                        // move and leave a trail of pheromones
                        setFree(point.getRow(), point.getCol());
                    }
                }
                break;
            } else {/*cek neighbor*/
                boolean nextCellFound = false;
                int cRow = origin.getRow();
                int cCol = origin.getCol();
                // observe neighbors
                ArrayList<Point> candidates = new ArrayList<>();
                for (int n = 0; n < neighbors.length; n++) {
                    int row = cRow + neighbors[n][0];
                    int col = cCol + neighbors[n][1];
                    Point neighbor = new Point(row, col);
                    if (row>=0&&row<map.length&&col>=0&&col<map[0].length&&map[row][col] >= 0 && !visited.contains(neighbor)) {
                        candidates.add(neighbor);
                    }
                }
                if (!candidates.isEmpty()) { /* hitung probabilitas candidates menggunakan repulsive pheromone */
                    double[] numerator = new double[candidates.size()];
                    double denominator = 0;
                    for (int i = 0; i < numerator.length; i++) {
                        int row = candidates.get(i).getRow();
                        int col = candidates.get(i).getCol();
                        double distance = Math.sqrt(Math.pow(row - cRow, 2) + Math.pow(col - cCol, 2));/* euclidean distance*/
                        double eta = 1.0 / (distance + 1.0);
                        double pheromone = pheromoneGrid[row][col];
                        double tau = 1.0 / (pheromone + 1.0);
                        numerator[i] = Math.pow(tau, alpha) * Math.pow(eta, beta);
                        denominator += numerator[i];
                    }
                    if (denominator > 0) {
                        // hitung probabilitas
                        double sum = 0;
                        double[] cumulativeProbability = new double[numerator.length];
                        for (int i = 0; i < numerator.length; i++) {
                            double probability = numerator[i] / denominator;
                            sum += probability;
                            cumulativeProbability[i] = sum;
                        }
                        double min = 0;
                        double max = sum;
                        double random = ThreadLocalRandom.current().nextDouble(min, Math.nextUp(max));
                        // select candidate
                        int selected = -1;
                        for (int i = 0; i < cumulativeProbability.length; i++) {
                            if (random <= cumulativeProbability[i]) {
                                selected = i;
                                break;
                            }
                        }
                        if (selected >= 0) {
                            Point destination = candidates.get(selected);
                            stack.push(destination);
                            visited.add(destination);
                            nextCellFound = true;
                        }
                    }
                }
                if (!nextCellFound) { /* backtracking */
                    stack.pop();
                }
            }
        }
    }

    protected void setFree(int cRow, int cCol) {
        // setiap drone memiliki frontiernya masing-masing, namun sebuah drone dapat menghapus frontier drone lain saat setfree
        int rays = 360;
        double cSize = cellSize * scale;
        double hcSize = 0.5 * cSize;


        for (int i = 0; i < rays; i += 2) {
            double angle = Math.toRadians(i);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            double x = cCol * cSize + hcSize;
            double y = cRow * cSize + hcSize;
            double step = 0.01;//epsilon resolusi ray
            double checked = 0;
            double MAX_RADIUS = (double) cameraFoV * cSize;
            while (checked < MAX_RADIUS) {
                x += dx * step;
                y += dy * step;
                checked += step;
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] == -1||mustBeExploredArea[row][col]==0) {
                    break;
                } else {
                    if (map[row][col] == 0) {
                        //explored++;//check as explored
                    }
                    //tandai sebagai explored cell
                    //map[row][col] = map[row][col] + 1;
                    map[row][col] = 1;
                    deltaTau[row][col]+=Q;
                }
            }

        }

    }

}
