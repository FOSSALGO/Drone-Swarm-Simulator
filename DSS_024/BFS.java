package swarmdronesimulator.swarmdrone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class BFS implements Algorithm {

    @Override
    public Point[][] toNearestFrontier(ArrayList<Point> lastPositionOfActiveDrones, ArrayList<Point>[] frontiers, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors) {
        Point[][] pathToNearestFrontier = null;
        /*------------------------------------------------------*/
        if (lastPositionOfActiveDrones != null && lastPositionOfActiveDrones.size() > 0 && frontiers != null && frontiers.length > 0 && base_map != null) {
            pathToNearestFrontier = new Point[lastPositionOfActiveDrones.size()][];
            for (int d = 0; d < lastPositionOfActiveDrones.size(); d++) {
                ArrayList<Point> frontiersDrone = frontiers[d];
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int cRow = position.row - MIN_ROW;
                int cCol = position.col - MIN_COL;
                Point startPoint = new Point(cRow, cCol);
                Stack<Point> stackPath = null;
                int[][] map = ArrayCopy.copy(base_map);
                int step = 1;
                map[startPoint.row][startPoint.col] = step;
                boolean complete = false;
                Queue<Point> queue = new LinkedList<Point>();
                queue.offer(startPoint);

                while (!queue.isEmpty() && !complete) {
                    //System.out.println(queue);
                    /* check neighbors */
                    Point center = queue.poll();
                    step = map[center.row][center.col];
                    for (int n = 0; n < neighbors.length; n++) {
                        int i = center.row + neighbors[n][0];
                        int j = center.col + neighbors[n][1];
                        Point newNeighbor = new Point(i, j);
                        if (frontiersDrone.contains(newNeighbor)) {
                            //finish
                            complete = true;
                            map[i][j] = step + 1;
                            stackPath = new Stack<>();
                            stackPath.push(newNeighbor);
                            int footsteps = step;
                            while (footsteps > 0) {
                                Point top = stackPath.peek();
                                for (int l = 0; l < neighbors.length; l++) {
                                    int ni = top.row + neighbors[l][0];
                                    int nj = top.col + neighbors[l][1];
                                    if (ni >= 0 && ni < map.length && nj >= 0 && nj < map[0].length && map[ni][nj] == footsteps) {
                                        stackPath.push(new Point(ni, nj));
                                        footsteps--;
                                        break;
                                    }
                                }
                            }
                            pathToNearestFrontier[d] = new Point[stackPath.size()];
                            for (int k = 0; k < stackPath.size(); k++) {
                                pathToNearestFrontier[d][k] = stackPath.pop();
                            }
                        } else {
                            if (i >= 0 && i < map.length && j >= 0 && j < map[0].length && map[i][j] == 0) {
                                queue.offer(newNeighbor);
                                map[i][j] = step + 1;
                            }
                        }
                    }
                }

            }
        }
        /*------------------------------------------------------*/
        return pathToNearestFrontier;
    }
}
