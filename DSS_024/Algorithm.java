package swarmdronesimulator.swarmdrone;

import java.util.ArrayList;

public interface Algorithm {

    Point[][] toNearestFrontier(ArrayList<Point> lastPositionOfActiveDrones, ArrayList<Point>[] frontiers, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors);
}
