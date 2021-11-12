package behaviors;

import car.Car;
import lejos.robotics.subsumption.Behavior;

public abstract class CarBehavior implements Behavior {
	public final String ACTION;

	protected Car c;
	
	public CarBehavior(String action, Car c) {
		this.ACTION = action;
		this.c = c;
	}
	
	public void action() {
		c.setActiveBehavior(ACTION);
	}
	
	public String getACTION() {
		return ACTION;
	}
}
