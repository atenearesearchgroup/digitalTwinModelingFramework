

import bluetooth.BluetoothConnector;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * Robot that stops if it hits something before it completes its travel.
 */

public class TravelTest {
	DifferentialPilot pilot;
	TouchSensor bump = new TouchSensor(SensorPort.S1);

	public void drawSquare(float length) {
		for (int i = 0; i < 4; i++) {
			pilot.travel(length);
			while (pilot.isMoving()) {
				if (bump.isPressed())
					pilot.stop();
			}
			pilot.rotate(90);
			while (pilot.isMoving()) {
				if (bump.isPressed())
					pilot.stop();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		BluetoothConnector conn = new BluetoothConnector();
		conn.openConnection();


		TravelTest sq = new TravelTest();
		sq.pilot = new DifferentialPilot(2.25f, 5.5f, Motor.A, Motor.C);
		sq.drawSquare(20);
		
		conn.closeConnection();
		
		BluetoothConnector conn1 = new BluetoothConnector();
		conn1.openConnection();
		
		System.out.println("Tachometer A: " + Motor.A.getTachoCount());
		System.out.println("Tachometer C: " + Motor.C.getTachoCount());
		Motor.A.rotate(5000);
		Motor.C.rotate(-5000);
		Thread.sleep(10000);
		Sound.playTone(1000, 1000);
		System.out.println("Tachometer A: " + Motor.A.getTachoCount());
		System.out.println("Tachometer C: " + Motor.C.getTachoCount());
		
		conn1.closeConnection();
	}
}