package behaviors;

import car.Car;
import lejos.robotics.subsumption.Behavior;

/**
 * This action detects commands received and executes them in the car.
 */
public class RemoteControl extends CarBehavior {

    public RemoteControl(Car c) {
        super("Command", c);
    }

    public boolean takeControl() {
        return !car.commandsIsEmpty();
    }

    public void suppress() {
        car.getPilot().stop();
    }

    public void action() {
        super.action();
        while (!car.commandsIsEmpty()) {
            for (Behavior b : car.getBehaviors()) {
                CarBehavior cb = (CarBehavior) b;
                if (("Action::" + cb.getACTION()).equals(car.commands().get(0))) {
                    System.out.println("[INFO-PT] Performing action => " + cb.getACTION());
                    cb.action();
                    car.execute();
                    break;
                }
            }
        }
    }


}
