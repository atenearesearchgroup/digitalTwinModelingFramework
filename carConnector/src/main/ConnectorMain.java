package main;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import receiver.SensorReceiver;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class ConnectorMain {

	public static void  main(String[] args) {
		JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost"); 
		try {
			ExecutorService snapshotsProducer = Executors.newSingleThreadExecutor();			
			SensorReceiver sr = new SensorReceiver(jedisPool, 8080);
			snapshotsProducer.submit(sr);
			
			Scanner scan = new Scanner(System.in);
			System.out.println("Type \"end\" to close the connection and finish the program...");
			while (true) {
				if (scan.nextLine().equals("end")) {
					break;
				}
			}
			
			sr.stop();
			scan.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
