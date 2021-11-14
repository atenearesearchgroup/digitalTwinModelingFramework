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
	private int sleepTime;
	
	public CommandsReceiver(int port, Car c, int sleepTime) throws IOException {
		this.port = port;
		this.server = new ServerSocket(this.port);
		this.running = true;
		this.c = c;
		this.sleepTime = sleepTime;
	}
	
	
	public void run() {
		while(running) {
			try {
				Socket connection = server.accept();
				System.out.println("[INFO-PT] THE CAR IS RECEIVING COMMANDS");
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while(running) {
					if(inFromClient.ready()) {
						String message = inFromClient.readLine();
						System.out.println("[INFO-PT] " + message);
						c.addToQueue(message);
					} else {
						System.out.println("[INFO-PT] No new commands.");
					}
					Thread.sleep(this.sleepTime);
				}
				connection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void stop() throws IOException {
		this.running = false;
		this.server.close();
	}
}
