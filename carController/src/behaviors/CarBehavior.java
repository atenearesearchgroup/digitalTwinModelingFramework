package behaviors;

import car.Car;
import lejos.robotics.subsumption.Behavior;

/**
 * Template class to define the Car's behaviors
 */
public abstract class CarBehavior implements Behavior {
    public final String ACTION;

    protected Car car;

    public CarBehavior(String action, Car car) {
        this.ACTION = action;
        this.car = car;
    }

    public void action() {
        car.setActiveBehavior(ACTION);
    }

    public String getACTION() {
        return ACTION;
    }
}
