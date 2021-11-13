package behaviors;

import car.Car;
import lejos.robotics.subsumption.Behavior;

public class RemoteControl extends CarBehavior{	
	
	public RemoteControl(Car c) {
		super("Command", c);
	}

	public boolean takeControl() {
		return !c.commandsIsEmpty();
	}

	public void suppress() {
		c.getPilot().stop();
	}

	public void action() {
		super.action();
		while(!c.commandsIsEmpty()) {;
			for(Behavior b : c.getBehaviors()) {
				CarBehavior cb = (CarBehavior) b;;
				if(("Action::" + cb.getACTION()).equals(c.commands().get(0))) {
					System.out.println("[INFO-PT] Performing action => " + cb.getACTION());
					cb.action();
					c.execute();
					break;
				}
			}
		}
	}
	
	
}
