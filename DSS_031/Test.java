package dsde.autonomousswarmdroneexploration;

import java.util.concurrent.ThreadLocalRandom;

public class Test {
    public static void main(String[] args) {
        double min = 1;
        double max = 3;
        for (int i = 0; i < 20; i++) {
            double random = ThreadLocalRandom.current().nextDouble(min, Math.nextUp(max));
            System.out.println(random);
        }
    }
}
