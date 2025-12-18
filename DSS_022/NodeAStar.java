package swarmdronesimulator.swarmdronesimulator;

import javafx.scene.Node;

public class NodeAStar {
    NodeAStar parent;
    int row, col;
    double f, g, h;

    public NodeAStar(NodeAStar parent, int row, int col, double g, double h) {
        this.parent = parent;
        this.row = row;
        this.col = col;
        this.g = g;
        this.h = h;
        this.f = g + h;
    }
}
