package behaviors;

import car.Car;

public class DriveOnLine extends CarBehavior{

	private boolean suppress = false;
	
	public DriveOnLine(Car c) {
		super("ForwardOnLine", c);
	}

	public boolean takeControl() {
		return c.getLight().readValue() <= 40 && c.commandsIsEmpty();
	}

	public void suppress() {
		suppress = true;
	}

	public void action() {
		super.action();
		while(!suppress){
			c.getPilot().forward();
			while (c.getLight().readValue() <= 40)
				Thread.yield(); // action complete when not on line
		}
		c.getPilot().stop();
		suppress = false;
	}
}
