package behaviors;

import car.Car;

public class DriveBackward extends CarBehavior{	
	
	public DriveBackward(Car c) {
		super("Backward", c);
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
		c.getPilot().travel(-60);
		while(c.getPilot().isMoving())Thread.yield();
	}
}
