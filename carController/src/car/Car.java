package car;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.RotateMoveController;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.util.PilotProps;
import taco.TachoPose;
import taco.TachoPoseProvider;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public abstract class Car {
	public final String WHEEL_DIAMETER = "5.55";
	public final String TRACK_WIDTH = "16.1";
	public final String RIGHT_MOTOR = "A";
	public final String LEFT_MOTOR = "C";
	public final String REVERSE = "false";

	private final LightSensor light;
	private final UltrasonicSensor ultrasonic;
	private final TouchSensor touchLeft;
	private final TouchSensor touchRight;

	private final RotateMoveController pilot;
	private final TachoPoseProvider poseProvider;
	private Behavior[] behaviors;

	private String activeBehavior;

	protected List<String> incomingCommands;

	public Car() throws IOException {
		/* 	INCOMING COMMANDS */
		this.incomingCommands = new LinkedList<>();

		/* SENSORS */
		this.light = new LightSensor(SensorPort.S3);
		this.ultrasonic = new UltrasonicSensor(SensorPort.S2);
		this.touchRight = new TouchSensor(SensorPort.S4);
		this.touchLeft = new TouchSensor(SensorPort.S1);

		/* PILOT */
		PilotProps pp = new PilotProps();
		pp.loadPersistentValues();

		float wheelDiameter = Float.parseFloat(pp.getProperty(PilotProps.KEY_WHEELDIAMETER, this.WHEEL_DIAMETER));
		float trackWidth = Float.parseFloat(pp.getProperty(PilotProps.KEY_TRACKWIDTH, this.TRACK_WIDTH));
		RegulatedMotor leftMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, this.LEFT_MOTOR));
		RegulatedMotor rightMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, this.RIGHT_MOTOR));
		boolean reverse = Boolean.parseBoolean(pp.getProperty(PilotProps.KEY_REVERSE, this.REVERSE));
		
		this.pilot = new DifferentialPilot(wheelDiameter, trackWidth, leftMotor, rightMotor, reverse);
		this.pilot.setRotateSpeed(50);
		this.pilot.setTravelSpeed(3);

		TachoPose tc = new TachoPose();
		this.poseProvider = new TachoPoseProvider(tc, this, leftMotor, rightMotor);
	}

	public void startBehaving() {
		(new Arbitrator(getBehaviors())).start();
	}

	public synchronized void addToQueue(String command) {
		incomingCommands.add(command);
	}

	public synchronized void execute() {
		incomingCommands.remove(0);
	}
	
	public synchronized boolean commandsIsEmpty() {
		return this.incomingCommands.isEmpty();
	}
	
	public List<String> commands(){
		return Collections.unmodifiableList(this.incomingCommands);
	}

	public LightSensor getLight() {
		return light;
	}

	public UltrasonicSensor getUltrasonic() {
		return ultrasonic;
	}

	public TouchSensor getTouchLeft() {
		return touchLeft;
	}

	public TouchSensor getTouchRight() {
		return touchRight;
	}

	public RotateMoveController getPilot() {
		return pilot;
	}

	public Behavior[] getBehaviors() {
		return behaviors;
	}

	public synchronized void setBehaviors(Behavior[] behaviors) {
		this.behaviors = behaviors;
	}

	public TachoPoseProvider getPoseProvider() {
		return poseProvider;
	}

	public synchronized void setActiveBehavior(String activeBehavior) {
		this.activeBehavior = activeBehavior;
	}

	public String getActiveBehavior() {
		return activeBehavior;
	}
}
