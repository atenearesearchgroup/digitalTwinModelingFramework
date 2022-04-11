package behaviors;

import car.Car;

/**
 * This action moves the car forward.
 */
public class DriveForward extends CarBehavior {

    public DriveForward(Car c) {
        super("Forward", c);
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
        car.getPilot().travel(60, true);
        while (car.getPilot().isMoving()) Thread.yield();
    }
}
