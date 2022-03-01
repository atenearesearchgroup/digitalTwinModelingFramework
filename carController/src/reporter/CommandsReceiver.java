package reporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import car.Car;

public class CommandsReceiver implements Runnable {

	private ServerSocket server;
	private int port;
	private boolean running;	
	private Car c;

	public CommandsReceiver(int port, Car c) throws IOException {
		this.port = port;
		this.server = new ServerSocket(this.port);
		this.running = true;
		this.c = c;
	}
	
	// TODO: revisar el papel que tienen los buffers en la ejecución, porque causan excepciones
	public void run() {
			try {
				Socket connection = server.accept();
				System.out.println("[INFO-PT] THE CAR IS RECEIVING COMMANDS");
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					if(inFromClient.ready()) {
						String message = inFromClient.readLine();
						System.out.println("[INFO-PT] " + message);
						c.addToQueue(message);
					} else {
						System.out.println("[INFO-PT] No new commands.");
					}
				connection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}

	}
	
	public void stop() throws IOException {
		this.running = false;
		this.server.close();
	}
}
