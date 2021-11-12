package reporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.Instant;
import car.Car;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class SensorReporter implements Runnable {

	private final String TWIN_ID = "twinId";
	private final String PHYSICAL_TWIN_NAME = "NXJCar";
	private final String TIMESTAMP = "timestamp";
	private final String EXECUTION_ID = "executionId";
	private final String SNAPSHOT_ID = "snapshotId";

	private final String POSITION_X = "xPos";
	private final String POSITION_Y = "yPos";
	private final String ANGLE = "angle";
	private final String SPEED = "speed";

	private final String LIGHT = "light";
	private final String DISTANCE = "distance";
	private final String BUMPED = "bump";
	private final String IS_MOVING = "isMoving";

	private final String ACTION = "action";

	private Car car;
	private Socket client;
	private int port;
	private boolean running;
	private int sleepTime;

	public SensorReporter(Car car, int port, int sleepTime) throws UnknownHostException, IOException {
		this.car = car;
		this.running = true;
		this.sleepTime = sleepTime;
		this.port = port;
		this.client = new Socket("localhost", this.port);
	}

	public void run() {
		try {
			BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
			String firstTimestamp = Long.toString(Instant.now().getEpochSecond());
			boolean first = true;

			while (running) {
				Thread.sleep(sleepTime);

				StringBuilder snapshot = new StringBuilder();
				snapshot.append("{");

				// IDENTIFIERS //

				processAttribute(snapshot, TWIN_ID, PHYSICAL_TWIN_NAME);

				String timestamp;
				if (first) {
					timestamp = firstTimestamp;
					first = false;
				} else {
					timestamp = Long.toString(Instant.now().getEpochSecond());
				}
				processAttribute(snapshot, TIMESTAMP, timestamp);

				String executionId = firstTimestamp;
				processAttribute(snapshot, EXECUTION_ID, executionId);

				String snapshotId = PHYSICAL_TWIN_NAME + ":" + executionId + ":" + timestamp;
				processAttribute(snapshot, SNAPSHOT_ID, snapshotId);

				// MOVEMENT ATTRIBUTES //

				String xPos = twoDecimalsPrecision(this.car.getPoseProvider().getPose().getX());
				processAttribute(snapshot, POSITION_X, xPos);

				String yPos = twoDecimalsPrecision(this.car.getPoseProvider().getPose().getY());
				processAttribute(snapshot, POSITION_Y, yPos);

				String angle = twoDecimalsPrecision(this.car.getPoseProvider().getPose().getHeading());
				processAttribute(snapshot, ANGLE, angle);

				String speed = twoDecimalsPrecision(this.car.getPilot().getTravelSpeed());
				processAttribute(snapshot, SPEED, speed);

				// SENSORS //

				String light = Integer.toString(this.car.getLight().getLightValue());
				processAttribute(snapshot, LIGHT, light);

				String distance = Integer.toString(this.car.getUltrasonic().getDistance());
				processAttribute(snapshot, DISTANCE, distance);

				String bumped = Integer.toString(
						(this.car.getTouchLeft().isPressed() || this.car.getTouchRight().isPressed()) ? 1 : 0);
				processAttribute(snapshot, BUMPED, bumped);

				String isMoving = Integer.toString(this.car.getPilot().isMoving() ? 1 : 0);
				processAttribute(snapshot, IS_MOVING, isMoving);

				// BEHAVIOR //

				String action = this.car.getActiveBehavior();
				processLastAttribute(snapshot, ACTION, action);

				snapshot.append("}\n");

				System.out.println("[INFO-PT-Reporter] " + snapshot.toString());
				bf.write(snapshot.toString());
				bf.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processAttribute(StringBuilder snapshot, String attributeKey, String attributeValue) {
		System.out.println("[INFO-PT-Reporter] " + attributeKey + ": " + attributeValue);
		snapshot.append("\"" + attributeKey + "\":\"" + attributeValue + "\",");
	}

	private void processLastAttribute(StringBuilder snapshot, String attributeKey, String attributeValue) {
		System.out.println("[INFO-PT-Reporter] " + attributeKey + ": " + attributeValue);
		snapshot.append("\"" + attributeKey + "\":\"" + attributeValue + "\"");
	}

	private String twoDecimalsPrecision(double value) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		return df.format(value);
	}

	public void stop() throws IOException {
		this.running = false;
		this.client.close();
	}

}
