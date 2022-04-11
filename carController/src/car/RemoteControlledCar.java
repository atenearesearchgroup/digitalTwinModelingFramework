package car;

import java.io.IOException;

import behaviors.DriveBackward;
import behaviors.DriveForward;
import behaviors.DriveOnLine;
import behaviors.OffLine;
import behaviors.RemoteControl;
import behaviors.Stop;
import behaviors.TurnLeft;
import behaviors.TurnRight;
import lejos.robotics.subsumption.Behavior;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class RemoteControlledCar extends Car {

	private final DriveForward forward;
	private final DriveBackward backward;
	private final TurnLeft left;
	private final TurnRight right;
	private final Stop stop;
	private final RemoteControl remoteControlBehav;

	/**
	 * Default constructor
	 * 
	 * @throws IOException
	 */
	public RemoteControlledCar() throws IOException {
		super();

		forward = new DriveForward(this);
		backward = new DriveBackward(this);
		left = new TurnLeft(this);
		right = new TurnRight(this);
		stop = new Stop(this);
		remoteControlBehav = new RemoteControl(this);
		this.setBehaviors(new Behavior[] { this.forward, this.backward, this.left, this.right, this.stop,
				this.remoteControlBehav });
		this.setActiveBehavior(this.stop.ACTION);
	}
}
