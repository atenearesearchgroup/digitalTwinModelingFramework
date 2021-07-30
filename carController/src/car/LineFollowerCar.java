package car;

import java.io.IOException;

import lejos.robotics.subsumption.Behavior;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class LineFollowerCar extends Car {
	
	private final String DRIVE_FORWARD = "Forward";
	private final String OFF_LINE = "Rotate";
	

	public LineFollowerCar() throws IOException {
		super();
		this.setBehaviors(new Behavior[]{this.offLine(), this.driveForward()});
		setActiveBehavior(DRIVE_FORWARD);
	}

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
