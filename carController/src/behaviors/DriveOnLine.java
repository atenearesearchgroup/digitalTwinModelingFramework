package behaviors;

import car.Car;

public class DriveOnLine extends CarBehavior{	
	
	public DriveOnLine(Car c) {
		super("ForwardOnLine", c);
	}

	public boolean takeControl() {
		return c.getLight().readValue() <= 40 && c.commandsIsEmpty();
	}

	public void suppress() {
		c.getPilot().stop();
	}

	public void action() {
		super.action();
		c.getPilot().travel(0.001);
		c.getPilot().stop();
		while (c.getLight().readValue() <= 40)
			Thread.yield(); // action complete when not on line
	}
}
