package digital.twin;

import config.ConfigurationManager;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */

public class OutputCommandsManager extends OutputManager {

    public OutputCommandsManager(ConfigurationManager cm) {
        this.retrievedClass = cm.getOutCommandClass();
        this.setChannel("CommandOutChannel");
        this.identifier = "commands";
        this.attributes = cm.getCommandsAttributes();
    }

    /**
     * It saves all the Commands object in the currently displayed object diagram in the data lake.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException
     */
    public void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> unprocessedCommands = this.getObjects(api);
        for (MObjectState command : unprocessedCommands) {
            Map<String, String> commandsValues = new HashMap<>();
            Map<MAttribute, Value> commandsAttributes = command.attributeValueMap();
            String commandId = "command:" + getAttribute(commandsAttributes, "twinId").replace("'", "") + ":" + getAttribute(commandsAttributes, "timestamp");
            saveAttributes(api, jedis, command, commandsValues, commandsAttributes, commandId);
        }
    }

}