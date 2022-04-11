package pubsub;

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
	private final int sleepTime;
	private boolean running;
	
	/**
	 * Default constructor
	 * 
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 * @param sleepTime		Milliseconds between each check in the database.
	 */
	public InPubService(String channel, JedisPool jedisPool, int sleepTime) {
		super(channel);
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.running = true;
	}
	
	/**
	 * It checks periodically if there are new unprocessed snapshots coming from the Physical Twin in the Data Lake.
	 */
	public void run() {
        while(running){
        	// Wait some milliseconds until it checks again
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Checks for new snapshots
            Jedis jedisTempConn = jedisPool.getResource();
            Jedis jedisTempConnDL = jedisPool.getResource();
            try {
            	if(!InputSnapshotsManager.getUnprocessedSnapshots(jedisTempConnDL).isEmpty()) {
            		jedisTempConn.publish(this.getChannel(), "New Snapshots in database");
            		System.out.println("[" + this.hashCode() + "-DT] " + "New Snapshots in database");
            	}
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               jedisPool.returnResource(jedisTempConn);
               jedisPool.returnResource(jedisTempConnDL);
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
