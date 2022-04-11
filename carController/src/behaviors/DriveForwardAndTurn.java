package behaviors;

import car.Car;

/**
 * This action moves the car forward.
 */
public class DriveForwardAndTurn extends CarBehavior {

    public DriveForwardAndTurn(Car c) {
        super("Forward", c);
    }

    public boolean takeControl() {
        return true;
    }

    public void suppress() {
        car.getPilot().stop();
    }

    public void action() {
        super.action();
        //car.getPilot().travel(100, true);
        //while (car.getPilot().isMoving()) Thread.yield();

        car.getPilot().rotate(90);

        //car.getPilot().travel(100, true);
        //while (car.getPilot().isMoving()) Thread.yield();

        while(true){
            car.getPilot().stop();
        }
    }
}
