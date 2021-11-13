package car;

import java.io.IOException;

import lejos.robotics.subsumption.Behavior;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class LineFollowerCar extends Car {
	
	private final String DRIVE_FORWARD = "Forward";
	private final String OFF_LINE = "Rotate";
	

	/**
	 * Default constructor
	 * @throws IOException
	 */
	public LineFollowerCar() throws IOException {
		super();
		this.setBehaviors(new Behavior[]{this.offLine(), this.driveForward()});
		setActiveBehavior(DRIVE_FORWARD);
	}
	
	/**
	 * The car moves forward while the light sensor detects a dark line on the floor.
	 * @return
	 */
	public Behavior driveForward() {
		Behavior driveForward = new Behavior() {
			public boolean takeControl() {
				return getLight().readValue() <= 40;
			}

			public void suppress() {
				getPilot().stop();
			}

			public void action() {
				setActiveBehavior(DRIVE_FORWARD);
				getPilot().forward();
				while (getLight().readValue() <= 40)
					Thread.yield(); // action complete when not on line
			}
		};
		return driveForward;
	}
	
	/**
	 * The car starts spinning while it does not detect the black line on the floow
	 * @return
	 */
	public Behavior offLine() {
		Behavior offLine = new Behavior() {
			private boolean suppress = false;

			public boolean takeControl() {
				return getLight().readValue() > 40;
			}

			public void suppress() {
				suppress = true;
			}

			public void action() {
				setActiveBehavior(OFF_LINE);
				int sweep = 10;
				while (!suppress) {
					getPilot().rotate(sweep, true);
					while (!suppress && getPilot().isMoving())
						Thread.yield();
					sweep *= -2;
				}
				getPilot().stop();
				suppress = false;
			}
		};
		return offLine;
	}
	
	public String DRIVE_FORWARD() {
		return DRIVE_FORWARD;
	}
	
	public String OFF_LINE() {
		return OFF_LINE;
	}
}
