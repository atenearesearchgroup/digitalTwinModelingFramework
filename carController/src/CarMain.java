import bluetooth.BluetoothConnector;
import car.Car;
import car.LineFollowerCar;
import lejos.util.PilotProps;
import reporter.CommandsReceiver;
import reporter.SensorReporter;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class CarMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        BluetoothConnector conn = new BluetoothConnector();
        conn.openConnection();

        PilotProps pp = new PilotProps();
        pp.loadPersistentValues();

        Car car = new LineFollowerCar();
        //Car car = new RemoteControlledCar();

        ScheduledExecutorService threadScheduler = Executors.newScheduledThreadPool(2);

		Socket client = connectToServer(8081);
        BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        SensorReporter sr = new SensorReporter(car, outToClient);
        threadScheduler.scheduleAtFixedRate(sr, 0, 5000, TimeUnit.MILLISECONDS);

        ServerSocket commandsServer = new ServerSocket(8080);
        Socket connection = commandsServer.accept();
        System.out.println("[INFO-PT-CommandsReceiver] THE CLIENT " + connection.getInetAddress() + ":" + connection.getPort() + " IS CONNECTED ");
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        CommandsReceiver cr = new CommandsReceiver(inFromClient, car);
        threadScheduler.scheduleAtFixedRate(cr, 0, 2000, TimeUnit.MILLISECONDS);

        car.startBehaving();
    }


	private static Socket connectToServer(int port) throws IOException, InterruptedException {
		int retryCounter = 0;
		Socket client = null;

		while (retryCounter < 200) {
			try {
				retryCounter++;
				client = new Socket("localhost", port);
				if (client == null) {
					System.out.println("[INFO-ERROR] Server is not available.");
				} else {
					System.out.println("[INFO-DT] Connected to Car server successfully.");
					break;
				}
			} catch (ConnectException e) {
				Thread.sleep(500);
			}
		}
		return client;
	}
}
