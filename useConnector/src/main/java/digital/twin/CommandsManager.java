package digital.twin;

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

public class CommandsManager extends OutputManager {

    public CommandsManager() {
        this.retrievedClass = "Command";
        this.setChannel("CommandOutChannel");
        this.identifier = "commands";

        attributes.put("twinId", STRING);
        attributes.put("timestamp", NUMBER);
        attributes.put("executionId", NUMBER);

        attributes.put("action", STRING);
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

            String commandId = "0:0:command:" + getAttribute(commandsAttributes, "twinId").replace("'", "") + ":"
                    + getAttribute(commandsAttributes, "executionId").replace("'", "") + ":"
                    + getAttribute(commandsAttributes, "timestamp");

            saveAttributes(api, jedis, command, commandsValues, commandsAttributes, commandId);
        }
    }

}
