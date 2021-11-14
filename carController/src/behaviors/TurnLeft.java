package behaviors;

import car.Car;

public class TurnLeft extends CarBehavior{	
	
	public TurnLeft(Car c) {
		super("TurnLeft", c);
	}

	// It never takes control on its own, since it is a behavior for the remote control
	public boolean takeControl() {
		return false;
	}

	public void suppress() {
		c.getPilot().stop();
	}

	public void action() {
		super.action();
		c.getPilot().rotate(-90);
		Thread.yield(); // action complete when not on line
	}
}
