package swarmdronesimulator.swarmdrone;

import java.util.ArrayList;

public interface Algorithm {

    Point[] pathfinding(Point start, Point finish, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors);
    Point[][] goToHome(ArrayList<Point> firstPositionOfActiveDrones, ArrayList<Point> lastPositionOfActiveDrones, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors);
    Point[][] toNearestFrontier(ArrayList<Point> lastPositionOfActiveDrones, ArrayList<Point>[] frontiers, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors);
}
