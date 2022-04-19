package pubsub;

import digital.twin.InputManager;
import digital.twin.InputSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class InPubService extends PubService {
	
	private final JedisPool jedisPool;
	private boolean running;
	private InputManager inputManager;
	
	/**
	 * Default constructor
	 * 
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 */
	public InPubService(String channel, JedisPool jedisPool, InputManager im) {
		super(channel);
		this.jedisPool = jedisPool;
		this.running = true;
		this.inputManager = im;
	}
	
	/**
	 * It checks periodically if there are new unprocessed snapshots coming from the Physical Twin in the Data Lake.
	 */
	public void run() {
		// Checks for new snapshots
		Jedis jedisTempConn = jedisPool.getResource();
		try {
			if(!this.inputManager.getUnprocessedObjects(jedisTempConn).isEmpty()) {
				jedisTempConn.publish(this.getChannel(), "New Snapshots in database");
				System.out.println("[" + this.hashCode() + "-DT] " + "New Snapshots in database");
			} else {
				System.out.println( "[Snapshot-" + this.getChannel() + "] No new input commands");
			}
		} catch (Exception e) {
		   e.printStackTrace();
		} finally {
			jedisPool.returnResource(jedisTempConn);
		}
    }
	
	/**
	 * It stops the periodic search for new snapshots.
	 */
	public void stop() {
		this.running = false;
	}
}
