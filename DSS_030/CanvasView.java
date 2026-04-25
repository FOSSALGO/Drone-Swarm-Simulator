package dsde.simulator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class CanvasView extends Canvas {

    private final String delimiter = ":";// FILE
    private final int sensorFoV = 8; // DRONES, satuan: cell ; 1 cell = 10 cm x 10 cm
    private final int cameraFoV = 7;// DRONES, satuan: cell
    private final int MAX_NUM_ROWS = 200;// GRID
    private final int MAX_NUM_COLS = 400;// GRID
    protected double translateX = 0;// VISUALIZATION
    protected double translateY = 0;// VISUALIZATION
    protected double scale = 1.0;// VISUALIZATION
    private ArrayList<Drone> drones = new ArrayList<>();// DRONES
    private final ArrayList<Drone> activeDrones = new ArrayList<>();// EXPLORATION
    private double mouseAnchorX;// VISUALIZATION
    private double mouseAnchorY;// VISUALIZATION
    private int moveToI = -1;// LINE
    private int moveToJ = -1;// LINE
    private int lineToI = -1;// LINE
    private int lineToJ = -1;// LINE
    private int positionI = -1;// LINE
    private int positionJ = -1;// LINE
    private int cellSize;
    private double halfCellSize;
    private int MIN_ROW;// EXPLORATION
    private int MIN_COL;// EXPLORATION
    private int MAX_ROW;// EXPLORATION
    private int MAX_COL;// EXPLORATION
    private int[][] map = null;// EXPLORATION
    private int[][] base_map = null;// EXPLORATION
    private int numRows;// GRID
    private int numCols;// GRID
    private final int[][] darkScreenCopy = new int[numRows][numCols];// GRID
    private int[][] environment = null;// GRID
    private String[][] wallType = new String[numRows][numCols];// GRID
    private int[][] darkScreen = new int[numRows][numCols];// GRID
    private State state = State.DEFAULT;//State.OBSTACLE;
    private int draggingDrone = -1;//


    protected CanvasView(int[][] environment, int cellSize) {
        super();
        if (environment != null && cellSize > 0) {
            this.environment = environment;
            numRows = environment.length;
            numCols = environment[0].length;
            this.cellSize = cellSize;
            this.halfCellSize = 0.5 * cellSize;
            setWidth(numCols * cellSize);
            setHeight(numRows * cellSize);
        }
    }

    protected void writeToFile(File file) {
        if (environment != null) {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("ENVIRONMENT----------------------------------\n");
                sb.append("NUM ROWS" + delimiter + environment.length + "\n");
                sb.append("NUM COLS" + delimiter + environment[0].length + "\n");
                for (int i = 0; i < environment.length; i++) {
                    for (int j = 0; j < environment[i].length; j++) {
                        sb.append(environment[i][j] + delimiter);
                    }
                    sb.append("\n");
                }

                if (drones != null && !drones.isEmpty()) {
                    sb.append("DRONES---------------------------------------\n");
                    sb.append("NUM DRONES" + delimiter + drones.size() + "\n");
                    for (int d = 0; d < drones.size(); d++) {
                        sb.append(drones.get(d).indexI + delimiter + drones.get(d).indexJ + "\n");
                    }
                }

                String data = sb.toString();
                Path filePath = file.toPath();
                Files.write(filePath, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                state = State.DEFAULT;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void readFromFile(File file) {
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
                setWidth(numCols * cellSize);// VISUALIZATION
                setHeight(numRows * cellSize);// VISUALIZATION
                environment = new int[numRows][numCols];// GRID
                wallType = new String[numRows][numCols];// GRID
                //pathOfDrones = null;
                //frame = -1;
                //darkScreen = new int[numRows][numCols];// GRID
                //darkScreenCopy = new int[numRows][numCols];// GRID
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
                    drones = new ArrayList<>();// DRONES
                    //activeDrones = new ArrayList<>();// DRONES
                    //lastPositionOfActiveDrones = new ArrayList<>();// DRONES

                    for (int d = 0; d < nDrone; d++) {
                        line = reader.readLine();
                        values = line.split(delimiter);
                        int i = Integer.parseInt(values[0]);
                        int j = Integer.parseInt(values[1]);
                        Drone drone = new Drone(i, j, sensorFoV, cameraFoV, cellSize);
                        drones.add(drone);
                    }
                }
                state = State.DEFAULT;
                render();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    protected State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void setFreeTransform() {
        state = State.DEFAULT;
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
    }

    protected void resetEnvironment() {
        numRows = MAX_NUM_ROWS;// GRID
        numCols = MAX_NUM_COLS;// GRID
        translateX = 0;
        translateY = 0;
        scale = 1;
        setWidth(numCols * cellSize);// VISUALIZATION
        setHeight(numRows * cellSize);// VISUALIZATION
        environment = new int[numRows][numCols];// GRID
        wallType = new String[numRows][numCols];// GRID
        //darkScreen = new int[numRows][numCols];// GRID
        //darkScreenCopy = new int[numRows][numCols];// GRID
        drones = new ArrayList<>();
        //initializeDrones(numberOfDrones);// initialize drone
        state = State.DEFAULT;
        //pathOfDrones = null;
        //frame = -1;
        //reset
        moveToI = -1;
        moveToJ = -1;
        lineToI = -1;
        lineToJ = -1;
        render();
    }

    protected void handleMouseClicked(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        double cSize = cellSize * scale;
        if (x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
            int i = (int) Math.floor((y - translateY) / cSize);
            int j = (int) Math.floor((x - translateX) / cSize);

            if (state.equals(State.OBSTACLE)) {
                if (environment[i][j] == 0) {
                    environment[i][j] = -1;
                } else {
                    environment[i][j] = 0;
                }
            } else if (state.equals(State.ADD_DRONE)) {
                // ADD DRONE
                Drone drone = new Drone(i, j, sensorFoV, cameraFoV, cellSize);
                drones.add(drone);
            } else if (state.equals(State.REMOVE_DRONE)) {
                // REMOVE DRONE
                if (!drones.isEmpty()) {
                    for (int k = drones.size() - 1; k >= 0; k--) {
                        Drone drone = drones.get(k);
                        if (drone.indexI == i && drone.indexJ == j) {
                            drones.remove(drone);
                            break;
                        }
                    }
                }
            }

            checkWallType();
            render();
        }
    }

    protected void handleMouseScroll(ScrollEvent scrollEvent) {
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

    protected void handleMousePressed(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        double cSize = cellSize * scale;
        int i = (int) Math.floor((y - translateY) / cSize);
        int j = (int) Math.floor((x - translateX) / cSize);

        if (state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
            if (moveToI == -1 && moveToJ == -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                moveToI = i;
                moveToJ = j;
                lineToI = -1;
                lineToJ = -1;
            }
        } else {
            //check selected drone
            draggingDrone = -1;
            if (i >= 0 && i < numRows && j >= 0 && j < numCols) {
                for (int k = 0; k < drones.size(); k++) {
                    Drone drone = drones.get(k);
                    if (i == drone.indexI && j == drone.indexJ) {
                        draggingDrone = k;
                        break;
                    }
                }
            }

            if (!(draggingDrone >= 0 && draggingDrone < drones.size())) {
                //fre transform
                mouseAnchorX = x;
                mouseAnchorY = y;
            }
        }
    }

    protected void handleMouseMoved(MouseEvent mouseEvent) {
        if (state.equals(State.OBSTACLE) || state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
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

    protected void handleMouseDragged(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        double cSize = cellSize * scale;
        int i = (int) Math.floor((y - translateY) / cSize);
        int j = (int) Math.floor((x - translateX) / cSize);
        if (state.equals(State.OBSTACLE) || state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
            if (moveToI != -1 && moveToJ != -1 && x >= translateX && x < translateX + numCols * cSize && y >= translateY && y < translateY + numRows * cSize) {
                positionI = i;
                positionJ = j;
                if (state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
                    lineToI = i;
                    lineToJ = j;
                }

            }
        } else {
            if (draggingDrone >= 0 && draggingDrone < drones.size()) {
                if (i >= 0 && i < numRows && j >= 0 && j < numCols) {
                    drones.get(draggingDrone).indexI = i;
                    drones.get(draggingDrone).indexJ = j;
                }
            } else {
                // free transform
                double deltaX = x - mouseAnchorX;
                double deltaY = y - mouseAnchorY;

                translateX += deltaX;
                translateY += deltaY;

                mouseAnchorX = x;
                mouseAnchorY = y;
            }
        }
        render();
    }

    protected void handleMouseReleased(MouseEvent mouseEvent) {
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

            boolean valid = maxX != minX || maxY != minY;
            double step = 0.01 * scale;//epsilon
            while (valid && x >= minX && x <= maxX && y >= minY && y <= maxY) {
                int col = (int) (x / cSize);
                int row = (int) (y / cSize);
                if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
                    if (state.equals(State.LINE)) {
                        environment[row][col] = -1;
                    } else if (state.equals(State.REMOVE_LINE)) {
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
        draggingDrone = -1;
        render();
    }

    protected void render() {
        if (this != null && environment != null) {
            this.halfCellSize = 0.5 * cellSize;
            GraphicsContext gc = this.getGraphicsContext2D();
            gc.setImageSmoothing(true);
            gc.setFontSmoothingType(FontSmoothingType.LCD);
            gc.clearRect(0, 0, getWidth(), getHeight());

            gc.save();// save & restore
            gc.translate(translateX, translateY);
            gc.scale(scale, scale);
            // BEGIN VISUALIZATION -------------------------------------------------------------------------------------

            /* draw background color */
            gc.setFill(Color.valueOf("#F7F7F7"));//("#1e323d");//("#14272e"));//("#14252d"));
            gc.fillRect(0, 0, getWidth(), getHeight());

            /* draw horizontal line */
            for (int i = 0; i <= numRows; i++) {
                gc.setLineWidth(0.2);
                gc.setStroke(Color.valueOf("#929AAB"));
                if (i % 10 == 0) {
                    gc.setLineWidth(0.6);
                    gc.setStroke(Color.valueOf("#929AAB"));
                }
                gc.strokeLine(0, i * cellSize, getWidth(), i * cellSize);
            }

            /* draw vertical line */
            for (int i = 0; i <= numCols; i++) {
                gc.setLineWidth(0.2);
                gc.setStroke(Color.valueOf("#929AAB"));
                if (i % 10 == 0) {
                    gc.setLineWidth(0.6);
                    gc.setStroke(Color.valueOf("#929AAB"));
                }
                gc.strokeLine(i * cellSize, 0, i * cellSize, getHeight());
            }

            /* Draw Drones */
            if (drones != null && !drones.isEmpty()) {
                for (int i = 0; i < drones.size(); i++) {
                    drones.get(i).draw(gc, environment);
                }
            }

            /* draw wall/obstacle */
            gc.setGlobalAlpha(0.15);
            for (int i = 0; i < environment.length; i++) {
                for (int j = 0; j < environment[i].length; j++) {
                    if (environment[i][j] == -1) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;

                        // draw shadow
                        gc.setFill(Color.valueOf("#505050"));
                        gc.fillRect(xo + 3, yo + 3, cellSize, cellSize);
                    }
                }
            }
            gc.setGlobalAlpha(1.0);
            for (int i = 0; i < environment.length; i++) {
                for (int j = 0; j < environment[i].length; j++) {
                    if (environment[i][j] == -1) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;

                        // draw wall
                        gc.setFill(Color.valueOf("#03A6A1"));
                        gc.fillRect(xo, yo, cellSize, cellSize);
                    }
                }
            }

            /* draw hatching lines wall/obstacle */
            for (int i = 0; i < environment.length; i++) {
                for (int j = 0; j < environment[i].length; j++) {
                    if (environment[i][j] == -1) {
                        double xo = j * cellSize;
                        double yo = i * cellSize;

                        gc.setStroke(Color.valueOf("#ffffff"));
                        gc.setLineWidth(0.3);
                        gc.strokeLine(xo, yo, xo + cellSize, yo + cellSize);
                        gc.strokeLine(xo + halfCellSize, yo, xo + cellSize, yo + cellSize - halfCellSize);
                        gc.strokeLine(xo, yo + halfCellSize, xo + cellSize - halfCellSize, yo + cellSize);

                        // draw wall type
                        if (wallType != null) {
                            gc.setLineWidth(0.6);
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
                gc.setStroke(Color.valueOf("#03A6A1"));
                if (state.equals(State.REMOVE_LINE)) {
                    gc.setStroke(Color.RED);
                }
                gc.setLineWidth(0.8);
                gc.strokeLine(x0, y0, x1, y1);
            }

            /* draw position */
            if (state.equals(State.OBSTACLE) || state.equals(State.LINE) || state.equals(State.REMOVE_LINE)) {
                double px = positionJ * cellSize + halfCellSize;
                double py = positionI * cellSize + halfCellSize;
                String textPosition = "(" + (positionI + 1) + "," + (positionJ + 1) + ")";
                gc.setFont(Font.font("Arial", 8));
                gc.setFill(Color.BLACK);
                gc.fillText(textPosition, px, py + 2 + 3 * cellSize);
            }

            // END OF VISUALIZATION ------------------------------------------------------------------------------------
            gc.restore();// save & restore

        }
    }

    private void getBorders() {// EXPLORATION
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
        getBorders();
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

    private boolean isDroneValid(int droneI, int droneJ) {// EXPLORATION
        boolean valid = droneI >= MIN_ROW && droneI <= MAX_ROW && droneJ >= MIN_COL && droneJ <= MAX_COL;
        return valid;
    }

//    private ArrayList<Drone> getActiveDrones() {// EXPLORATION
//        activeDrones = new ArrayList<>();
//        for (int i = 0; i < drones.size(); i++) {
//            Drone drone = drones.get(i);
//            if (isDroneValid(drone.indexI, drone.indexJ)) {
//                activeDrones.add(drone);
//                drone.setName("D"+(activeDrones.size()));
//            }
//        }
//        if (!activeDrones.isEmpty()) {
//            lastPositionOfActiveDrones = new ArrayList<>();
//            frontiers = new ArrayList[activeDrones.size()];
//            pathOfDrones = new ArrayList[activeDrones.size()];
//            for (int d = 0; d < activeDrones.size(); d++) {
//                lastPositionOfActiveDrones.add(new Point(activeDrones.get(d).indexI, activeDrones.get(d).indexJ));
//                frontiers[d] = new ArrayList<>();
//                pathOfDrones[d] = new ArrayList<>();
//            }
//        }
//        return activeDrones;
//    }


}
