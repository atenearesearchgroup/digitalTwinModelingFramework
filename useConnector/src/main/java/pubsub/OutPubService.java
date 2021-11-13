package pubsub;

import org.tzi.use.api.UseSystemApi;

import digital.twin.OutputSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class OutPubService implements Runnable {
	
	private UseSystemApi api;
	private JedisPool jedisPool;
	private int sleepTime;
	private boolean running;
	
	/**
	 * Default constructor
	 * 
	 * @param api			USE system API instance to interact with the currently displayed object diagram.
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 * @param sleepTime		Milliseconds between each check in the database.
	 */
	public OutPubService(UseSystemApi api, JedisPool jedisPool, int sleepTime) {
		this.api = api;
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.running = true;
	}
	
	/**
	 * It checks periodically if there are new output snapshots in the currently displayed object diagram on USE.
	 */
	public void run() {
        while(running){
        	// Wait some seconds until it checks again
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Checks for new snapshots
            Jedis jedisTemporalConnection = jedisPool.getResource();
            try {
            	if(!OutputSnapshotsManager.getSnapshots(api).isEmpty()) {
            		jedisTemporalConnection.publish(DTPubSub.DT_OUT_CHANNEL, "New Snapshots");
            		System.out.println("[" + this.hashCode() + "-DT] " + "New Snapshots");
            	}
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
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
