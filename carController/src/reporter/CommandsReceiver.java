package reporter;

import car.Car;

import java.io.BufferedReader;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 *
 * Receive commands sent from the computer
 */
public class CommandsReceiver implements Runnable {

    private final BufferedReader inFromClient;
    private final Car c;

    public CommandsReceiver(BufferedReader inFromClient, Car c) {
        this.inFromClient = inFromClient;
        this.c = c;
    }

    public void run() {
        try {
            if (inFromClient.ready()) {
                String message = inFromClient.readLine();
                System.out.println("[INFO-PT-CommandsReceiver] " + message);
                c.addToQueue(message);
            } else {
                System.out.println("[INFO-PT-CommandsReceiver] No new commands.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
