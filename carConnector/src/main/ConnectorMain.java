package main;

import communication.CommandsReporter;
import communication.SensorReceiver;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class ConnectorMain {

	public static void  main(String[] args) {
		JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost"); 
		try {
			ScheduledExecutorService threadScheduler = Executors.newScheduledThreadPool(2);
			
			SensorReceiver sr = new SensorReceiver(jedisPool, 8080);
			threadScheduler.scheduleAtFixedRate(sr, 0, 5000, TimeUnit.MILLISECONDS);
					
			CommandsReporter cr = new CommandsReporter(jedisPool, 8081);
			threadScheduler.scheduleAtFixedRate(cr, 0, 2000, TimeUnit.MILLISECONDS);
			
			//cr.stop();
			//sr.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
