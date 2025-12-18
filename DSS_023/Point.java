package swarmdronesimulator.swarmdrone;

public class Point {
    public int row, col;

    public Point(int i, int j) {
        this.row = i;
        this.col = j;
    }

    public int getI() {
        return row;
    }

    public void setI(int i) {
        this.row = i;
    }

    public int getJ() {
        return col;
    }

    public void setJ(int j) {
        this.col = j;
    }

    @Override
    public String toString() {
        return "Point{" +
                "i=" + row +
                ", j=" + col +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point titik = (Point) obj;
        return row == titik.row && col == titik.col;
    }
}// end of private class Poin