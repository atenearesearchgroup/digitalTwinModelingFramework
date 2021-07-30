package pubsub;

import org.tzi.use.api.UseSystemApi;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class SubService implements Runnable {
	
	private UseSystemApi api;
	private JedisPool jedisPool;
	private String subscribedChannel;
	
	public SubService(UseSystemApi api, JedisPool jedisPool, String channel) {
		this.api = api;
		this.jedisPool = jedisPool;
		this.subscribedChannel = channel;
	}

	public void run() {
        try {
        	System.out.println("[INFO] Subscribing to " + this.subscribedChannel);
        	Jedis jedisSubscriber = jedisPool.getResource();
        	Jedis jedisCrud = jedisPool.getResource();
        	jedisSubscriber.subscribe(new DTPubSub(api, jedisCrud), this.subscribedChannel);
		    jedisPool.returnResource(jedisSubscriber);
		    jedisPool.returnResource(jedisCrud);
        	System.out.println("[INFO] Subscription to " + this.subscribedChannel + " ended");
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

}
