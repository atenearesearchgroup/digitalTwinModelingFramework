package behaviors;

import car.Car;

/**
 * This action moves the car forward as long as it detects a dark line on the ground.
 */
public class DriveOnLine extends CarBehavior {

    private boolean suppress = false;

    public DriveOnLine(Car c) {
        super("ForwardOnLine", c);
    }

    public boolean takeControl() {
        return car.getLight().readValue() <= 40 && car.commandsIsEmpty();
    }

    public void suppress() {
        suppress = true;
    }

    public void action() {
        super.action();
        car.getPilot().forward();
        while (car.getLight().readValue() <= 40)
            Thread.yield(); // action complete when not on line
        car.getPilot().stop();
    }
}
