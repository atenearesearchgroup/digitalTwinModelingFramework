package behaviors;

import car.Car;

/**
 * This action causes the car to rotate progressively as long as it does not detect a dark line on the ground.
 */
public class OffLine extends CarBehavior {

    private boolean suppress = false;

    public OffLine(Car c) {
        super("RotateOffLine", c);
    }

    public boolean takeControl() {
        return car.getLight().readValue() > 40 && car.commandsIsEmpty();
    }

    public void suppress() {
        suppress = true;
    }

    public void action() {
        super.action();
        int sweep = 10;
        while (!suppress) {
            car.getPilot().rotate(sweep, true);
            while (!suppress && car.getPilot().isMoving())
                Thread.yield();
            sweep *= -2;
        }
        car.getPilot().stop();
        suppress = false;
    }
}
