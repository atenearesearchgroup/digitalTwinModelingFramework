package behaviors;

import car.Car;

public class Stop extends CarBehavior{	
	
	public Stop(Car c) {
		super("Stop", c);
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
		c.getPilot().stop();
		Thread.yield(); // action complete when not on line
	}
}
