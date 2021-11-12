package car;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import behaviors.CarBehavior;
import behaviors.DriveForward;
import behaviors.OffLine;
import behaviors.RemoteControl;
import lejos.robotics.subsumption.Behavior;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class LineFollowerCar extends Car {
	
	private OffLine offLineBehav;
	private DriveForward driveForwardBehav;
	private RemoteControl remoteControlBehav;
	

	/**
	 * Default constructor
	 * @throws IOException
	 */
	public LineFollowerCar() throws IOException {
		super();
		List<CarBehavior> behaviors = new ArrayList<CarBehavior>();
		offLineBehav = new OffLine(this);
		behaviors.add(offLineBehav);
		driveForwardBehav = new DriveForward(this);
		behaviors.add(driveForwardBehav);
		remoteControlBehav = new RemoteControl(this, behaviors, this.commands);
		
		this.setBehaviors(new Behavior[]{this.offLineBehav, this.driveForwardBehav, this.remoteControlBehav});
		setActiveBehavior(this.offLineBehav.ACTION);
	}
}
