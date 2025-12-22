package swarmdronesimulator.swarmdrone;

import java.util.ArrayList;
import java.util.Stack;

public class PriorityAStar implements Algorithm {

    @Override
    public Point[][] toNearestFrontier(ArrayList<Point> lastPositionOfActiveDrones, ArrayList<Point>[] frontiers, int[][] base_map, int MIN_ROW, int MIN_COL, int[][] neighbors) {
        Point[][] pathToNearestFrontier = null;
        /*------------------------------------------------------*/
        if (lastPositionOfActiveDrones != null && lastPositionOfActiveDrones.size() > 0 && frontiers != null && frontiers.length > 0 && base_map != null) {
            pathToNearestFrontier = new Point[lastPositionOfActiveDrones.size()][];
            for (int d = 0; d < lastPositionOfActiveDrones.size(); d++) {
                double MIN_DISTANCE = Double.MAX_VALUE;
                Node MIN_NODE = null;
                Point position = lastPositionOfActiveDrones.get(d);// the last position of drone
                int cRow = position.row - MIN_ROW;
                int cCol = position.col - MIN_COL;
                Point startPoint = new Point(cRow, cCol);

                //set priority frontier
                ArrayList<Point> candidateFrontier = new ArrayList<>();
                for (int i = 0; i < frontiers[d].size(); i++) {
                    Point point = new Point(frontiers[d].get(i).row, frontiers[d].get(i).col);
                    candidateFrontier.add(point);
                }
                int sensorFoV = 5;
                double radius = sensorFoV;
                ArrayList<Point> priorityFrontier = new ArrayList<>();
                while(priorityFrontier.isEmpty()&&!candidateFrontier.isEmpty()){
                    for (int i = 0; i < candidateFrontier.size(); i++) {
                        Point finishPoint = candidateFrontier.get(i);
                        double distance = Math.sqrt(Math.pow((finishPoint.row-startPoint.row),2)+Math.pow((finishPoint.col-startPoint.col),2));
                        if(distance<=radius){
                            candidateFrontier.remove(finishPoint);
                            priorityFrontier.add(finishPoint);
                        }
                    }
                    radius += sensorFoV;
                }

                if(!priorityFrontier.isEmpty()){
                    ArrayList<Point> frontiersDrone = priorityFrontier;

                    for (int f = 0; f < frontiersDrone.size(); f++) {
                        Point finishPoint = frontiersDrone.get(f);
                        int[][] map = ArrayCopy.copy(base_map);
                        ArrayList<Node> openList = new ArrayList<>();
                        ArrayList<Node> closedList = new ArrayList<>();
                        double gStart = 0;
                        double hStart = 0;//Math.sqrt(Math.pow((finishPoint.i-startPoint.i),2)+Math.pow((finishPoint.j-startPoint.j),2));
                        Node firstNode = new Node(null, cRow, cCol, gStart, hStart);
                        openList.add(firstNode);
                        while_openlist_not_empty:
                        while (!openList.isEmpty()) {
                            // find the node with least f on the open list, call it leastFNode
                            Node nodeWithLeastF = openList.getFirst();
                            for (int i = 1; i < openList.size(); i++) {
                                Node node = openList.get(i);
                                if (node.f < nodeWithLeastF.f) {
                                    nodeWithLeastF = node;
                                }
                            }

                            // pop nodeWithLeastF from openList
                            openList.remove(nodeWithLeastF);

                            // generate leastFNode's 8 successor
                            for (int n = 0; n < neighbors.length; n++) {
                                int row = nodeWithLeastF.row + neighbors[n][0];
                                int col = nodeWithLeastF.col + neighbors[n][1];

                                // validasi cell pada posisi row col
                                boolean isValid = row >= 0 && row < map.length && col >= 0 && col < map[0].length && map[row][col] == 0;

                                if (isValid) {
                                    Point newNeighbor = new Point(row, col);
                                    // if newNeighbor is finishPoint, stop search
                                    if (newNeighbor.equals(finishPoint)) {
                                        double distance = nodeWithLeastF.g + Math.sqrt(Math.pow(neighbors[n][0], 2) + Math.pow(neighbors[n][1], 2));// use euclidean distance to neighbor
                                        if (distance < MIN_DISTANCE) {
                                            MIN_DISTANCE = distance;
                                            MIN_NODE = new Node(nodeWithLeastF, row, col, distance, 0);
                                        }
                                        break while_openlist_not_empty;
                                    } else {
                                        // compute f = g + h
                                        double g = nodeWithLeastF.g + Math.sqrt(Math.pow(neighbors[n][0], 2) + Math.pow(neighbors[n][1], 2));// use euclidean distance to neighbor
                                        double h = Math.sqrt(Math.pow((row - finishPoint.row), 2) + Math.pow((col - finishPoint.col), 2));
                                        Node node = new Node(nodeWithLeastF, row, col, g, h);

                                        // cek node
                                        boolean skip = false;
                                        // 1. if a node with the same position as successor is in the OPEN list which has a lower f than successor, skip this successor
                                        for (int i = 0; i < openList.size(); i++) {
                                            Node nodeFromOpenList = openList.get(i);
                                            if (node.row == nodeFromOpenList.row && node.col == nodeFromOpenList.col && nodeFromOpenList.f < node.f) {
                                                skip = true;
                                                break;
                                            }
                                        }
                                        // 2. if a node with the same position as successor  is in the CLOSED list which has a lower f than successor, skip this successor
                                        if (!skip) {
                                            for (int i = 0; i < closedList.size(); i++) {
                                                Node nodeFromCloseList = closedList.get(i);
                                                if (node.row == nodeFromCloseList.row && node.col == nodeFromCloseList.col && nodeFromCloseList.f < node.f) {
                                                    skip = true;
                                                    break;
                                                }
                                            }
                                        }
                                        // 3. otherwise, add  the node to the open list
                                        if (!skip) {
                                            openList.add(node);// node baru ditambahkan ke open list
                                        }
                                    }
                                }
                            }// end of for

                            // push leastFNode on the closed list
                            closedList.add(nodeWithLeastF);

                        }//end of while
                    }//end of for (int f = 0; f < frontiersDrone.size(); f++)

                    // save best path
                    if (MIN_NODE != null) {
                        Stack<Point> path = new Stack<>();
                        Node node = MIN_NODE;
                        while (node != null) {
                            path.push(new Point(node.row, node.col));
                            node = node.parent;
                        }

                        pathToNearestFrontier[d] = new Point[path.size()];
                        for (int k = 0; k < path.size(); k++) {
                            pathToNearestFrontier[d][k] = path.pop();
                        }
                    }
                }//end of if priority frontier not empty

            }// end of for (int d = 0; d < lastPositionOfActiveDrones.size(); d++)
        }
        /*------------------------------------------------------*/
        return pathToNearestFrontier;
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
