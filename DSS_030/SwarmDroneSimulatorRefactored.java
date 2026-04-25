package dsde.simulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SwarmDroneSimulatorRefactored extends Application {

    Config cfg = new Config();
    int[][] map;
    boolean[][] explored;
    PheromoneGrid pheromone;
    List<DroneAgent> drones = new ArrayList<>();
    Canvas canvas;
    GraphicsContext gc;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        map = new int[cfg.rows][cfg.cols];
        explored = new boolean[cfg.rows][cfg.cols];
        pheromone = new PheromoneGrid(cfg.rows, cfg.cols);

        initDrones();

        canvas = new Canvas(cfg.cols * cfg.cellSize, cfg.rows * cfg.cellSize);
        gc = canvas.getGraphicsContext2D();

        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (now - last > 16_000_000) {
                    step();
                    render();
                    last = now;
                }
            }
        }.start();

        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Swarm Drone Simulator - Research Version");
        stage.show();
    }

    void initDrones() {
        Random r = new Random();
        for (int i = 0; i < cfg.droneCount; i++) {
            drones.add(new DroneAgent(r.nextInt(cfg.rows), r.nextInt(cfg.cols)));
        }
    }

    void step() {
        pheromone.decay(cfg.pheromoneDecay);
        pheromone.diffuse(cfg.pheromoneDiffusion);

        for (DroneAgent d : drones) {
            d.sense();
            d.think();
            d.act();
        }
    }

    boolean isFrontier(int r, int c) {
        return true;
    }

    boolean inBounds(int r, int c) {
        return r >= 0 && c >= 0 && r < cfg.rows && c < cfg.cols;
    }

    double dist(int a, int b, int c, int d) {
        return Math.hypot(a - c, b - d);
    }

    void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        for (DroneAgent d : drones) {
            gc.fillOval(d.c * cfg.cellSize, d.r * cfg.cellSize, 4, 4);
        }
    }

    static class Config {
        int rows = 120;
        int cols = 200;
        int cellSize = 6;

        int droneCount = 10;
        int rayCount = 16;

        double pheromoneDecay = 0.01;
        double pheromoneDiffusion = 0.2;

        double repulsionWeight = 2.0;
        double explorationWeight = 1.5;
        double frontierWeight = 2.5;

        int sensorRange = 10;
    }

    static class Point {
        int r, c;

        Point(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    class DroneAgent {
        int r, c;
        double tx, ty;
        List<Point> frontiers = new ArrayList<>();

        DroneAgent(int r, int c) {
            this.r = r;
            this.c = c;
        }

        void sense() {
            frontiers.clear();

            for (int i = 0; i < cfg.rayCount; i++) {
                double a = (2 * Math.PI / cfg.rayCount) * i;

                for (int s = 1; s < cfg.sensorRange; s++) {
                    int nr = r + (int) (Math.sin(a) * s);
                    int nc = c + (int) (Math.cos(a) * s);

                    if (!inBounds(nr, nc)) break;

                    explored[nr][nc] = true;

                    if (isFrontier(nr, nc)) {
                        frontiers.add(new Point(nr, nc));
                        break;
                    }
                }
            }
        }

        void think() {
            double best = -1e9;
            int br = r, bc = c;

            for (Point p : frontiers) {
                double d = dist(r, c, p.r, p.c);
                double ph = pheromone.get(p.r, p.c);

                double score =
                        cfg.frontierWeight
                                - d * cfg.explorationWeight
                                - ph * cfg.repulsionWeight;

                if (score > best) {
                    best = score;
                    br = p.r;
                    bc = p.c;
                }
            }

            tx = br;
            ty = bc;
        }

        void act() {
            r += Integer.compare((int) tx, r);
            c += Integer.compare((int) ty, c);

            pheromone.add(r, c, 1.0);
        }
    }

    class PheromoneGrid {
        double[][] g;

        PheromoneGrid(int r, int c) {
            g = new double[r][c];
        }

        void add(int r, int c, double v) {
            g[r][c] += v;
        }

        double get(int r, int c) {
            return g[r][c];
        }

        void decay(double d) {
            for (int i = 0; i < g.length; i++)
                for (int j = 0; j < g[0].length; j++)
                    g[i][j] *= (1 - d);
        }

        void diffuse(double a) {
            double[][] n = new double[g.length][g[0].length];

            for (int i = 1; i < g.length - 1; i++)
                for (int j = 1; j < g[0].length - 1; j++)
                    n[i][j] =
                            (g[i][j] + g[i + 1][j] + g[i - 1][j] + g[i][j + 1] + g[i][j - 1]) / 5.0;

            g = n;
        }
    }
}
