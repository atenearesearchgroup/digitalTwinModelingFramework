package plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;

import digital.twin.CommandsManager;
import digital.twin.OutputSnapshotsManager;
import pubsub.OutPubService;
import pubsub.PubService;
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
	private ExecutorService commandsProducer;
	private boolean shutDown;
	private OutPubService outPublisher;
	private OutPubService commandOutPublisher;
	private InPubService inPublisher;
	
	/**
	 * Default constructor
	 */
	public DigitalTwinConnectorPlugin() {
		this.snapshotsProducer = Executors.newSingleThreadExecutor();
		this.commandsProducer = Executors.newSingleThreadExecutor();
		this.snapshotsProcessor = Executors.newSingleThreadExecutor();
		this.shutDown = true;
	}

	/**
	 * This is the Action Method called from the Action Proxy
	 * 
	 * @param pluginAction		This is the reference to the current USE running instance.
	 */
	public void performAction(IPluginAction pluginAction) {
		if(shutDown) {
			api = UseSystemApi.create(pluginAction.getSession());
			jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

			checkConnectionWithDatabase();
			this.outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, api, jedisPool, 5000, new OutputSnapshotsManager());
			this.commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, api, jedisPool, 5000, new CommandsManager());
			this.inPublisher = new InPubService(DTPubSub.DT_IN_CHANNEL, api, jedisPool, 5000);
			
			startInformationExchange(outPublisher, snapshotsProducer, DTPubSub.DT_OUT_CHANNEL);
			startInformationExchange(commandOutPublisher, commandsProducer, DTPubSub.COMMAND_OUT_CHANNEL);
			startInformationExchange(inPublisher, snapshotsProcessor, DTPubSub.DT_IN_CHANNEL);
			shutDown = false;
		} else {
			stopInformationExchange(outPublisher, snapshotsProducer);
			stopInformationExchange(commandOutPublisher, commandsProducer);
			stopInformationExchange(inPublisher, snapshotsProcessor);
			shutDown = true;
			System.out.println("[INFO-DT] Connection ended successfully");
		}
	   	    
	}
	
	private void startInformationExchange(PubService pubService, ExecutorService executor, String channel) {
		if(executor.isShutdown()) {
			executor = Executors.newSingleThreadExecutor();
		}
		executor.submit(pubService);
		new Thread(new SubService(api, jedisPool, channel), "subscriber " + channel + " thread").start();
	}
	
	private void stopInformationExchange(PubService pubService, ExecutorService executor) {
		executor.shutdown();
		pubService.stop();
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