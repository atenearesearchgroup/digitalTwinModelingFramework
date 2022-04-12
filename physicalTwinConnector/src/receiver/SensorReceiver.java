package receiver;

import config.ConfigurationManager;
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
 */
public class SensorReceiver implements Runnable {

    private final String PROCESSING_QUEUE = "PROCESSING_QUEUE_LIST";
    private final String PHYSICAL_TWIN = "physical_twin";
    private final String SNAPSHOT_ID = "snapshotId";
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
        Map<String, String> attr = ConfigurationManager.getConfig().getOutputAttributes();
        for(String attrName : attr.keySet()){
            attributes.put(attrName, attr.get(attrName));
        }
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
                String snapshotId = "PT:" + snapshot.get(SNAPSHOT_ID).toString();
                String executionId = snapshotId.substring(0, snapshotId.lastIndexOf(":"));

                for (String attribute : attributes.keySet()) {
                    String value = snapshot.get(attribute).toString().replace(',', '.');
                    System.out.println("[INFO-PT-SensorReceiver] " + attribute + ": " + value);
                    sensorValues.put(attribute, value);
                    if (attributes.get(attribute).equals(NUMBER)) {
                        addSearchRegister(attribute, Double.parseDouble(value), snapshotId, jedis, executionId);
                    }
                }

                int processingQueue = 0;
                sensorValues.put(PROCESSING_QUEUE, Integer.toString(processingQueue));
                jedis.zadd(executionId + ":" + PROCESSING_QUEUE + "_LIST", processingQueue, 0 + ":" + snapshotId);

                int physicalTwin = 1;
                sensorValues.put(PHYSICAL_TWIN, Integer.toString(physicalTwin));
                addSearchRegister(PHYSICAL_TWIN, physicalTwin, snapshotId, jedis, executionId);

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
