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
	private final int sleepTime;
	private boolean running;
	private InputManager inputManager;
	
	/**
	 * Default constructor
	 * 
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 * @param sleepTime		Milliseconds between each check in the database.
	 */
	public InPubService(String channel, JedisPool jedisPool, int sleepTime, InputManager im) {
		super(channel);
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.running = true;
		this.inputManager = im;
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
			System.out.println("holiwi" + this.getChannel());
            // Checks for new snapshots
            Jedis jedisTempConn = null;
            Jedis jedisTempConnDL = jedisPool.getResource();
			System.out.println("holiwiwi" + this.getChannel());
            try {
            	if(!this.inputManager.getUnprocessedObjects(jedisTempConnDL).isEmpty()) {
					jedisTempConn = jedisPool.getResource();
            		jedisTempConn.publish(this.getChannel(), "New Snapshots in database");
            		System.out.println("[" + this.hashCode() + "-DT] " + "New Snapshots in database");
            	} else {
					System.out.println("esta vacio");
				}
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
				if(jedisTempConn!= null)
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
