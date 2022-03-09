package communication;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * Receives the sensor information sent by the car and stores it in the database.
 * <p>
 * TODO: Support a configuration file that determines the format of the information to be received, specifying which attributes we want to receive and store.
 */
public class SensorReceiver implements Runnable {

    private final String PROCESSING_QUEUE = "PROCESSING_QUEUE_LIST";
    private final String PHYSICAL_TWIN = "physical_twin";
    private final String SNAPSHOT_ID = "snapshotId";
    private final String EXECUTION_ID = "executionId";
    private final String STRING = "str";
    private final String NUMBER = "double";

    private final Map<String, String> attributes;

    private final JedisPool jedisPool;
    private final BufferedReader inFromClient;

    /**
     * Default constructor
     *
     * @param jedisPool    Jedis client pool, connected to the Data Lake
     * @param inFromClient connection to serverSocket
     */
    public SensorReceiver(JedisPool jedisPool, BufferedReader inFromClient) throws IOException {
        this.jedisPool = jedisPool;
        this.inFromClient = inFromClient;

        this.attributes = new HashMap<>();

        attributes.put("twinId", STRING);
        attributes.put("timestamp", NUMBER);
        attributes.put(EXECUTION_ID, NUMBER);
        attributes.put(SNAPSHOT_ID, STRING);

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
    public void run() {
        try {
            if (inFromClient.ready()) {
                Jedis jedis = jedisPool.getResource();
                String message = inFromClient.readLine();
                System.out.println("[INFO-PT-SensorReceiver]" + message);
                Map<String, String> sensorValues = new HashMap<>();

                JSONParser parser = new JSONParser();
                JSONObject snapshot = (JSONObject) parser.parse(message);
                String snapshotId = snapshot.get(SNAPSHOT_ID).toString();
                String executionID = snapshot.get(EXECUTION_ID).toString();

                for (String attribute : attributes.keySet()) {
                    String value = snapshot.get(attribute).toString().replace(',', '.');
                    System.out.println("[INFO-PT-SensorReceiver] " + attribute + ": " + value);
                    sensorValues.put(attribute, value);
                    if (attributes.get(attribute).equals(NUMBER)) {
                        addSearchRegister(attribute, Double.parseDouble(value), snapshotId, jedis, executionID);
                    }
                }

                int processingQueue = 0;
                sensorValues.put(PROCESSING_QUEUE, Integer.toString(processingQueue));
                jedis.zadd(PROCESSING_QUEUE + "List", processingQueue, 0 + ":" + snapshotId);

                int physicalTwin = 1;
                sensorValues.put(PHYSICAL_TWIN, Integer.toString(physicalTwin));
                addSearchRegister(PHYSICAL_TWIN, physicalTwin, snapshotId, jedis, executionID);

                jedis.hset(snapshotId, sensorValues);
                jedisPool.returnResource(jedis);
            } else {
                System.out.println("[INFO-PT-SensorReceiver] No new snapshots");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis, String executionId) {
        jedis.zadd(executionId + ":" + sensorKey.toUpperCase() + "_LIST", score, registryKey);
    }

}
