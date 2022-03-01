package communication;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class SensorReceiver implements Runnable {

	private final String PROCESSING_QUEUE = "processingQueue";
	private final String PHYSICAL_TWIN = "physical_twin";
	private final String SNAPSHOT_ID = "snapshotId";
	private final String STRING = "str";
	private final String NUMBER = "double";

	private final Map<String, String> attributes;

	private final JedisPool jedisPool;
	private final ServerSocket server;
	private final int port;
	private boolean running;

	/**
	 * Default constructor
	 * 
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 * @param port			Port to deploy the server socket
	 */
	public SensorReceiver(JedisPool jedisPool, int port) throws IOException {
		this.jedisPool = jedisPool;
		this.running = true;
		this.port = port;
		this.server = new ServerSocket(this.port);
		
		this.attributes = new HashMap<>();
		
		attributes.put("twinId", STRING);
		attributes.put("timestamp", NUMBER);
		attributes.put("executionId", NUMBER);
		attributes.put("snapshotId", STRING);
		
		attributes.put("xPos", NUMBER);
		attributes.put("yPos", NUMBER);
		attributes.put("angle", NUMBER);
		attributes.put("speed", NUMBER);
		
		attributes.put("light", NUMBER);
		attributes.put("distance", NUMBER);
		attributes.put("bump", NUMBER);
		attributes.put("isMoving", NUMBER);
		
		attributes.put("action", STRING);
	}

	/**
	 * This method runs in the background, receiving information from the Physical Twin and storing it into the Data Lake.
	 */
	// TODO: revisar el papel que tienen los buffers en la ejecución, porque causan excepciones
	public void run() {
			try {
				Socket connection = server.accept();
				System.out.println("[INFO-PT] THE CLIENT " + connection.getInetAddress() + ":" + connection.getPort()
						+ " IS CONNECTED ");

				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					if(inFromClient.ready()) {
						Jedis jedis = jedisPool.getResource();
						String message = inFromClient.readLine();
						System.out.println("[INFO-PT]" + message);
						Map<String, String> sensorValues = new HashMap<>();
						
						JSONParser parser = new JSONParser();
						JSONObject snapshot = (JSONObject) parser.parse(message);
						String snapshotId = snapshot.get(SNAPSHOT_ID).toString();
						
						for(String attribute : attributes.keySet()) {
							String value = snapshot.get(attribute).toString().replace(',', '.');
							System.out.println("[INFO-PT-Reception] " + attribute + ": " + value);
							sensorValues.put(attribute, value);
							if(attributes.get(attribute).equals(NUMBER)) {							
								addSearchRegister(attribute, Double.parseDouble(value), snapshotId, jedis);
							}
						}
						
						int processingQueue = 0;
						sensorValues.put(PROCESSING_QUEUE, Integer.toString(processingQueue));
						jedis.zadd(PROCESSING_QUEUE + "List", processingQueue, 0 + ":" + snapshotId);
						
						int physicalTwin = 1;
						sensorValues.put(PHYSICAL_TWIN, Integer.toString(physicalTwin));
						addSearchRegister(PHYSICAL_TWIN, physicalTwin, snapshotId, jedis);

						jedis.hset(snapshotId, sensorValues);
						jedisPool.returnResource(jedis);
					} else {
						System.out.println("[INFO-PT] No new snapshots");
					}
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	private void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis) {
		jedis.zadd(sensorKey.toUpperCase() + "_LIST", score, registryKey);
	}
	
	/**
	 * This method stops the reception of information from the Physical Twin.
	 */
	public void stop() throws IOException {
		this.running = false;
		this.server.close();
	}

}
