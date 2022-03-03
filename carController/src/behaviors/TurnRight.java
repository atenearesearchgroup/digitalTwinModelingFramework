package behaviors;

import car.Car;

/**
 * This action turns the car to the right.
 */
public class TurnRight extends CarBehavior {

    public TurnRight(Car c) {
        super("TurnRight", c);
    }

    // It never takes control on its own, since it is a behavior for the remote control
    public boolean takeControl() {
        return false;
    }

    public void suppress() {
        car.getPilot().stop();
    }

    public void action() {
        super.action();
        car.getPilot().rotate(-90);
        Thread.yield(); // action complete when not on line
    }
}
