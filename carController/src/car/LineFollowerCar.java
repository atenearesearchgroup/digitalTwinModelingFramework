package car;

import java.io.IOException;
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
	
		offLineBehav = new OffLine(this);
		driveForwardBehav = new DriveForward(this);
		remoteControlBehav = new RemoteControl(this);
		this.setBehaviors(new Behavior[]{this.offLineBehav, this.driveForwardBehav, this.remoteControlBehav});
	}
}
