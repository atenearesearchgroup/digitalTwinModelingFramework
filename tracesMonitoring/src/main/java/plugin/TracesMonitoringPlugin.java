package plugin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;

import digital.twin.MismatchSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class TracesMonitoringPlugin implements IPluginActionDelegate {
	
	private UseSystemApi api;
	private JedisPool jedisPool;
	
	/**
	 * Default constructor
	 */
	public TracesMonitoringPlugin() {
	}

	/**
	 * This is the Action Method called from the Action Proxy
	 */
	public void performAction(IPluginAction pluginAction) {
		api = UseSystemApi.create(pluginAction.getSession());
		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

		checkConnectionWithDatabase();   
		
		Jedis temporalJedis = jedisPool.getResource();
		MismatchSnapshotsManager mmSnap = new MismatchSnapshotsManager(api, temporalJedis);
		if(!mmSnap.getPTSnapshots().isEmpty()) {
			try {
				mmSnap.saveSnapshots();
			} catch (UseApiException e) {
				e.printStackTrace();
			}
		}
		
		jedisPool.returnResource(temporalJedis);
		
	}
	            
	private void checkConnectionWithDatabase() {
		try {
			Jedis jedis = jedisPool.getResource();
			// prints out "Connection Successful" if Java successfully connects to Redis server.
			System.out.println("[INFO-DT] Connection Successful");
			System.out.println("[INFO-DT] The server is running " + jedis.ping());
			jedisPool.returnResource(jedis);
		}catch(Exception e) {
			System.out.println(e);
		}
	}

}