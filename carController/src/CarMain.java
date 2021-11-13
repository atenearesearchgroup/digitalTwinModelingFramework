import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bluetooth.BluetoothConnector;
import car.Car;
import car.LineFollowerCar;
import lejos.util.PilotProps;
import reporter.CommandsReceiver;
import reporter.SensorReporter;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class CarMain {

	public static void main(String[] args) throws IOException {

		BluetoothConnector conn = new BluetoothConnector();
		conn.openConnection();

		PilotProps pp = new PilotProps();
		pp.loadPersistentValues();

		Car car = new LineFollowerCar();
		ExecutorService snapshotsProducer = Executors.newSingleThreadExecutor();		
		SensorReporter sr = new SensorReporter(car, 8080, 5000);
		snapshotsProducer.submit(sr);
		
		ExecutorService commandsExecutor = Executors.newSingleThreadExecutor();		
		CommandsReceiver cr = new CommandsReceiver(8081, car);
		commandsExecutor.submit(cr);		
		
		car.startBehaving();
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Type \"end\" to close the connection and finish the program...");
		while (true) {
			if (scan.nextLine().equals("end")) {
				break;
			}
		}
		
		cr.stop();
		sr.stop();
		scan.close();
		conn.closeConnection();
	}

}
