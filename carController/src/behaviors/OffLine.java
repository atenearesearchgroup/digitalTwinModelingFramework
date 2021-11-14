package behaviors;

import car.Car;

public class OffLine extends CarBehavior {
	
	private boolean suppress = false;
	
	public OffLine(Car c) {
		super("RotateOffLine", c);
	}

	public boolean takeControl() {
		return c.getLight().readValue() > 40 && c.commandsIsEmpty();
	}

	public void suppress() {
		suppress = true;
	}

	public void action() {
		super.action();
		int sweep = 10;
		while (!suppress) {
			c.getPilot().rotate(sweep, true);
			while (!suppress && c.getPilot().isMoving())
				Thread.yield();
			sweep *= -2;
		}
		c.getPilot().stop();
		suppress = false;
	}
}
