package main;

import communication.CommandsReporter;
import communication.SensorReceiver;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class ConnectorMain {

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        try {
            ScheduledExecutorService threadScheduler = Executors.newScheduledThreadPool(2);

            ServerSocket sensorServer = new ServerSocket(8081);
            Socket connection = sensorServer.accept();
            System.out.println("[INFO-PT-SensorReceiver] THE CLIENT " + connection.getInetAddress() + ":" + connection.getPort() + " IS CONNECTED ");
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            SensorReceiver sr = new SensorReceiver(jedisPool, inFromClient);
            threadScheduler.scheduleAtFixedRate(sr, 0, 5000, TimeUnit.MILLISECONDS);

			Socket client = connectToServer(8080);
            CommandsReporter cr = new CommandsReporter(jedisPool, client);
            threadScheduler.scheduleAtFixedRate(cr, 0, 2000, TimeUnit.MILLISECONDS);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
	}

    private static Socket connectToServer(int port) throws IOException, InterruptedException {
        int retryCounter = 0;
        Socket client = null;

        while (retryCounter < 200) {
            try {
                retryCounter++;
                client = new Socket("localhost", port);
                if (client == null) {
                    System.out.println("[INFO-ERROR] Car server is not available.");
                } else {
                    System.out.println("[INFO-PT] Connected to Car server successfully.");
                    break;
                }
            } catch (ConnectException e) {
                Thread.sleep(500);
            }
        }
        return client;
    }

}
