package swarmdronesimulator.swarmdrone;

import java.util.ArrayList;

public class AStar implements Algorithm {


    @Override
    public Point[] pathfinding(Point start, Point finish, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors) {
        return new Point[0];
    }

    @Override
    public Point[][] goToHome(ArrayList<Point> firstPositionOfActiveDrones, ArrayList<Point> lastPositionOfActiveDrones, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors) {
        return new Point[0][];
    }

    @Override
    public Point[][] toNearestFrontier(ArrayList<Point> lastPositionOfActiveDrones, ArrayList<Point>[] frontiers, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors) {
        return new Point[0][];
    }

    private class Node {
        Node parent;
        int row, col;
        double f, g, h;

        public Node(Node parent, int row, int col, double g, double h) {
            this.parent = parent;
            this.row = row;
            this.col = col;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}
