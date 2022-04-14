package plugin;

import config.ConfigurationManager;
import digital.twin.InputCommandsManager;
import digital.twin.InputSnapshotsManager;
import digital.twin.OutputCommandsManager;
import digital.twin.OutputSnapshotsManager;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import pubsub.DTPubSub;
import pubsub.InPubService;
import pubsub.OutPubService;
import pubsub.SubService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * <p>
 * Plugin's main class
 */
public class DigitalTwinConnectorPlugin implements IPluginActionDelegate {

    private UseSystemApi api;
    private JedisPool jedisPool;
    private ExecutorService executor;
    private boolean shutDown;
    private OutPubService outPublisher;
    private OutPubService commandOutPublisher;
    private InPubService inPublisher;
    private InPubService commandInPublisher;

    /**
     * Default constructor
     */
    public DigitalTwinConnectorPlugin() {
        this.executor = Executors.newFixedThreadPool(3);
        this.shutDown = true;
    }

    /**
     * This is the Action Method called from the Action Proxy
     *
     * @param pluginAction This is the reference to the current USE running
     *                     instance.
     */
    public void performAction(IPluginAction pluginAction) {
        if (shutDown) {
            api = UseSystemApi.create(pluginAction.getSession());
            jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

            checkConnectionWithDatabase();
            this.outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, api, jedisPool, 5000, new OutputSnapshotsManager(ConfigurationManager.getConfig()));
            this.commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, api, jedisPool, 5000, new OutputCommandsManager(ConfigurationManager.getConfig()));
            this.inPublisher = new InPubService(DTPubSub.DT_IN_CHANNEL, jedisPool, 5000, new InputSnapshotsManager());
            this.commandInPublisher = new InPubService(DTPubSub.COMMAND_IN_CHANNEL, jedisPool, 5000, new InputCommandsManager());

            if (executor.isShutdown()) {
                executor = Executors.newFixedThreadPool(4);
            }

            executor.submit(outPublisher);
            executor.submit(commandOutPublisher);
            executor.submit(inPublisher);
            executor.submit(commandInPublisher);

            new Thread(new SubService(api, jedisPool, DTPubSub.DT_OUT_CHANNEL), "subscriber " + DTPubSub.DT_OUT_CHANNEL + " thread").start();
            new Thread(new SubService(api, jedisPool, DTPubSub.COMMAND_OUT_CHANNEL), "subscriber " + DTPubSub.COMMAND_OUT_CHANNEL + " thread").start();
            new Thread(new SubService(api, jedisPool, DTPubSub.DT_IN_CHANNEL), "subscriber " + DTPubSub.DT_IN_CHANNEL + " thread").start();
            new Thread(new SubService(api, jedisPool, DTPubSub.COMMAND_IN_CHANNEL), "subscriber " + DTPubSub.COMMAND_IN_CHANNEL + " thread").start();
            shutDown = false;
        } else {
            outPublisher.stop();
            commandOutPublisher.stop();
            inPublisher.stop();
            commandInPublisher.stop();
            shutDown = true;
            System.out.println("[INFO-DT] Connection ended successfully");
        }

    }

    /**
     * It checks that the connection with the Data Lake works properly.
     */
    private void checkConnectionWithDatabase() {
        try {
            Jedis jedis = jedisPool.getResource();
            System.out.println("[INFO-DT] Connection Successful");
            System.out.println("[INFO-DT] The server is running " + jedis.ping());
            jedisPool.returnResource(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}