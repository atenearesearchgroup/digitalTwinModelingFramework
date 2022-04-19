package pubsub;

import digital.twin.OutputManager;
import org.tzi.use.api.UseSystemApi;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class OutPubService extends PubService {

    private final UseSystemApi api;
    private final JedisPool jedisPool;
    private final OutputManager output;
    private boolean running;

    /**
     * Default constructor
     *
     * @param api       USE system API instance to interact with the currently displayed object diagram.
     * @param jedisPool Jedis client pool, connected to the Data Lake
     */
    public OutPubService(String channel, UseSystemApi api, JedisPool jedisPool, OutputManager output) {
        super(channel);
        this.api = api;
        this.jedisPool = jedisPool;
        this.running = true;
        this.output = output;
    }

    /**
     * It checks periodically if there are new output snapshots in the currently displayed object diagram on USE.
     */
    public void run() {
        // Checks for new snapshots
        Jedis jedisTemporalConnection = null;
        try {
            if (!output.getObjects(api).isEmpty()) {
                jedisTemporalConnection = jedisPool.getResource();
                jedisTemporalConnection.publish(this.getChannel(), "New Information");
                System.out.println("[" + this.hashCode() + "-" + this.getChannel() + "]" + " New Information");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedisTemporalConnection != null) {
                jedisPool.returnResource(jedisTemporalConnection);
            }
        }
    }

    /**
     * It stops the periodic search for new snapshots.
     */
    public void stop() {
        this.running = false;
    }
}
