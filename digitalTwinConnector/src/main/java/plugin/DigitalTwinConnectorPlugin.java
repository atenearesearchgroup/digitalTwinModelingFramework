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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * <p>
 * Plugin's main class
 */
public class DigitalTwinConnectorPlugin implements IPluginActionDelegate {

    private UseSystemApi api;
    private JedisPool jedisPool;
    private ScheduledExecutorService executor;
    private boolean shutDown;
    private OutPubService outPublisher;
    private OutPubService commandOutPublisher;
    private InPubService inPublisher;
    private InPubService commandInPublisher;

    /**
     * Default constructor
     */
    public DigitalTwinConnectorPlugin() {
        this.executor = Executors.newScheduledThreadPool(4);
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
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(12);
            jedisPool = new JedisPool(poolConfig, "localhost");

            checkConnectionWithDatabase();
            this.outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, api, jedisPool, new OutputSnapshotsManager(ConfigurationManager.getConfig()));
            this.commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, api, jedisPool, new OutputCommandsManager(ConfigurationManager.getConfig()));
            this.inPublisher = new InPubService(DTPubSub.DT_IN_CHANNEL, jedisPool, new InputSnapshotsManager());
            this.commandInPublisher = new InPubService(DTPubSub.COMMAND_IN_CHANNEL, jedisPool, new InputCommandsManager());

            if (executor.isShutdown()) {
                executor = Executors.newScheduledThreadPool(4);
            }

            executor.scheduleAtFixedRate(outPublisher, 0, 10000, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(commandOutPublisher, 0, 10000, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(inPublisher, 0, 5000, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(commandInPublisher, 0, 5000, TimeUnit.MILLISECONDS);

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
            executor.shutdown();
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