import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bluetooth.BluetoothConnector;
import car.Car;
import car.RemoteControlledCar;
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

		//Car car = new LineFollowerCar();
		Car car = new RemoteControlledCar();

		ScheduledExecutorService threadScheduler = Executors.newScheduledThreadPool(2);

		SensorReporter sr = new SensorReporter(car, 8080);
		threadScheduler.scheduleAtFixedRate(sr, 0, 5000, TimeUnit.MILLISECONDS);
			
		CommandsReceiver cr = new CommandsReceiver(8081, car);
		threadScheduler.scheduleAtFixedRate(cr, 0, 2000, TimeUnit.MILLISECONDS);
		
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
