package behaviors;

import car.Car;

public class DriveForward extends CarBehavior{	
	
	public DriveForward(Car c) {
		super("Forward", c);
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
		c.getPilot().travel(1);
	}
}
