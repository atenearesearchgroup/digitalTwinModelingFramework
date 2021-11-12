package behaviors;

import car.Car;

public class DriveForward extends CarBehavior{	
	
	public DriveForward(Car c) {
		super("Forward", c);
	}

	public boolean takeControl() {
		return c.getLight().readValue() <= 40 && c.commandsIsEmpty();
	}

	public void suppress() {
		c.getPilot().stop();
	}

	public void action() {
		super.action();
		c.getPilot().forward();
		while (c.getLight().readValue() <= 40)
			Thread.yield(); // action complete when not on line
	}
}
