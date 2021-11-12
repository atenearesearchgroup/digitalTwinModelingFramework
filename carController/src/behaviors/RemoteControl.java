package behaviors;

import java.util.Iterator;
import java.util.List;

import car.Car;

public class RemoteControl extends CarBehavior{	

	private List<CarBehavior> behaviors;
	private List<String> commands;
	
	public RemoteControl(Car c, List<CarBehavior> behaviors, List<String> commands) {
		super("Command", c);
		this.behaviors = behaviors;
		this.commands = commands;
	}

	public boolean takeControl() {
		return !c.commandsIsEmpty();
	}

	public void suppress() {
		c.getPilot().stop();
	}

	public void action() {
		super.action();
		while(!c.commandsIsEmpty()) {
			Iterator<CarBehavior> it = behaviors.iterator();
			while(it.hasNext()) {
				CarBehavior b = it.next();
				if(b.getACTION().equals(commands.get(0))) {
					b.action();
					commands.remove(0);
					break;
				}
			}
		}
	}
	
	
}
