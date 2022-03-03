package behaviors;

import car.Car;

/**
 * This action moves the car backwards.
 */
public class DriveBackward extends CarBehavior {

    public DriveBackward(Car c) {
        super("Backward", c);
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
        car.getPilot().travel(-60);
        while (car.getPilot().isMoving()) Thread.yield();
    }
}
