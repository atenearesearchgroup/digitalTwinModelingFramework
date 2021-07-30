package pubsub;

import org.tzi.use.api.UseSystemApi;

import digital.twin.OutputSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class OutPubService implements Runnable {
	
	private UseSystemApi api;
	private JedisPool jedisPool;
	private int sleepTime;
	private boolean running;
	
	public OutPubService(UseSystemApi api, JedisPool jedisPool, int sleepTime) {
		this.api = api;
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
	
	public void stop() {
		this.running = false;
	}
}
