package swarmdronesimulator.swarmdronesimulator;

import java.util.ArrayList;

public class AStar {

    public static void main(String[] args) {
        int[][]grid ={{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};

        int[][] neighbors8 = {
                {0, 1},/*east*/
                {1, 1},/*southeast*/
                {1, 0},/*south*/
                {1, -1},/*southwest*/
                {0, -1},/*west*/
                {-1, -1},/*northwest*/
                {-1, 0},/*north*/
                {-1, 1},/*northeast*/
        };

        Point start = new Point(1,1);
        Point finish = new Point(18,18);

        ArrayList<NodeAStar> openList = new ArrayList<>();
        ArrayList<NodeAStar>closedList = new ArrayList<>();
        double gStart = 0;
        double hStart = Math.sqrt(Math.pow((finish.i-start.i),2)+Math.pow((finish.j-start.j),2));
        NodeAStar startingNode = new NodeAStar(null, start.i, start.j, gStart, hStart);
        openList.add(startingNode);

        outer:
        while(!openList.isEmpty()){
            // find the node with least f on the open list, call it leastFNode
            NodeAStar leastFNode = openList.getFirst();
            for (int i = 1; i < openList.size(); i++) {
                NodeAStar node = openList.get(i);
                if(node.f<leastFNode.f){
                    leastFNode = node;
                }
            }

            // generate leastFNode's 8 successor
            for (int n = 0; n < neighbors8.length; n++) {
                int row = leastFNode.row + neighbors8[n][0];
                int col = leastFNode.col + neighbors8[n][1];

                // validasi cell pada posisi row col
                boolean isValid = false;
                if(row >= 0 && row < grid.length && col >= 0 && col < grid[0].length && grid[row][col] == 0){
                    isValid = true;
                }

                if(isValid){
                    Point newNeighbor = new Point(row, col);
                    // if newNeighbor is finishPoint, stop search
                    if(newNeighbor.equals(finish)){
                        double distance = leastFNode.g + Math.sqrt(Math.pow(neighbors8[n][0],2)+Math.pow(neighbors8[n][1],2));// use euclidean distance to neighbor
                        NodeAStar node = new NodeAStar(leastFNode,row,col,distance,0);
                        while(node!=null){
                            System.out.println("CELL("+node.row+","+node.col+")");
                            node = node.parent;
                        }
                        break outer;
                    }else{
                        // compute f = g + h
                        double g = leastFNode.g + Math.sqrt(Math.pow(neighbors8[n][0],2)+Math.pow(neighbors8[n][1],2));// use euclidean distance to neighbor
                        double h = Math.sqrt(Math.pow((row-finish.i),2)+Math.pow((col-finish.j),2));
                        NodeAStar node = new NodeAStar(leastFNode,row,col,g,h);

                        // cek node
                        boolean skip = false;
                        // 1. if a node with the same position as successor is in the OPEN list which has a lower f than successor, skip this successor
                        for (int i = 0; i < openList.size(); i++) {
                            NodeAStar nodeFromOpenList = openList.get(i);
                            if(node.row==nodeFromOpenList.row&&node.col==nodeFromOpenList.col&&node.f<nodeFromOpenList.f){
                                skip = true;
                                break;
                            }
                        }
                        // 2. if a node with the same position as successor  is in the CLOSED list which has a lower f than successor, skip this successor
                        if(!skip){
                            for (int i = 0; i < closedList.size(); i++) {
                                NodeAStar nodeFromCloseList = closedList.get(i);
                                if(node.row==nodeFromCloseList.row&&node.col==nodeFromCloseList.col&&node.f<nodeFromCloseList.f){
                                    skip = true;
                                    break;
                                }
                            }
                        }
                        // 3. otherwise, add  the node to the open list
                        if(!skip){
                            openList.add(node);// node baru ditambahkan ke open list
                        }
                    }
                }

            }// end of for

            // pop leastFNode from open list
            openList.remove(leastFNode);

            // push leastFNode on the closed list
            closedList.add(leastFNode);
        }// end of while
    }
}
