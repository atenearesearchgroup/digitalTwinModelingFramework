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
		System.out.println("buenas tardes");
		c.getPilot().travel(60, true);
		System.out.println("quetaltodoelmundo");
		while(c.getPilot().isMoving())Thread.yield();
	}
}
