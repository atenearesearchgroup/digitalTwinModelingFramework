package plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;

import pubsub.OutPubService;
import pubsub.DTPubSub;
import pubsub.InPubService;
import pubsub.SubService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 * Plugin's main class
 *  
 */
public class DigitalTwinConnectorPlugin implements IPluginActionDelegate {
	
	private UseSystemApi api;
	private JedisPool jedisPool;
	private ExecutorService snapshotsProducer;
	private ExecutorService snapshotsProcessor;
	private boolean shutDown;
	private OutPubService outPublisher;
	private InPubService inPublisher;
	
	/**
	 * Default constructor
	 */
	public DigitalTwinConnectorPlugin() {
		this.snapshotsProducer = Executors.newSingleThreadExecutor();
		this.snapshotsProcessor = Executors.newSingleThreadExecutor();
		this.shutDown = false;
	}

	/**
	 * This is the Action Method called from the Action Proxy
	 * 
	 * @param pluginAction		This is the reference to the current USE running instance.
	 */
	public void performAction(IPluginAction pluginAction) {
		if(!shutDown) {
			if(snapshotsProducer.isShutdown()) {
				snapshotsProducer = Executors.newSingleThreadExecutor();
			}
			if(snapshotsProcessor.isShutdown()) {
				snapshotsProcessor = Executors.newSingleThreadExecutor();
			}
			api = UseSystemApi.create(pluginAction.getSession());
			jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

			checkConnectionWithDatabase();
			
			this.outPublisher = new OutPubService(api, jedisPool, 5000);
			this.inPublisher = new InPubService(api, jedisPool, 5000);
			snapshotsProducer.submit(outPublisher);
			snapshotsProcessor.submit(inPublisher);
			shutDown = true;
			new Thread(new SubService(api, jedisPool, DTPubSub.DT_OUT_CHANNEL), "subscriber OUTPUT thread").start();
			new Thread(new SubService(api, jedisPool, DTPubSub.DT_IN_CHANNEL), "subscriber INPUT thread").start();
		} else {
			snapshotsProducer.shutdown();
			snapshotsProcessor.shutdown();
			outPublisher.stop();
			inPublisher.stop();
			shutDown = false;
			System.out.println("[INFO-DT] Connection ended successfully");
		}
	   	    
	}
	
	/**
	 * It checks that the connection with the Data Lake works properly.
	 */
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