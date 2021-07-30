package pubsub;

import org.tzi.use.api.UseSystemApi;

import digital.twin.InputSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class InPubService implements Runnable {
	
	private JedisPool jedisPool;
	private int sleepTime;
	private boolean running;
	
	public InPubService(UseSystemApi api, JedisPool jedisPool, int sleepTime) {
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.running = true;
	}
	
	public void run() {
        while(running){
        	// Wait some seconds until it checks again
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
            		jedisTempConn.publish(DTPubSub.DT_IN_CHANNEL, "New Snapshots in database");
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
	
	public void stop() {
		this.running = false;
	}
}
