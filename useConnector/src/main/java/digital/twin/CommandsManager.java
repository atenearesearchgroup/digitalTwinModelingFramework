package digital.twin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObjectState;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */

public class CommandsManager extends OutputManager{
	
	public CommandsManager() {
		this.retrievedClass = "Command";
		this.setChannel("CommandOutChannel");
		
		attributes.put("twinId", STRING);
		attributes.put("timestamp", NUMBER);
		attributes.put("executionId", NUMBER);
		
		attributes.put("action", STRING);
	}
	
	/**
	 * It saves all the Commands object in the currently displayed object diagram in the data lake. 
	 * 
	 * @param api				USE system API instance to interact with the currently displayed object diagram.
	 * @param jedis				An instance of the Jedis client to access the data lake.
	 * @throws UseApiException
	 */
	public void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException {
		List<MObjectState> unprocessedCommands = this.getObjects(api);
		for (MObjectState command : unprocessedCommands) {
			Map<String, String> commandsValues = new HashMap<>();
			Map<MAttribute, Value> commandsAttributes = command.attributeValueMap();
			
			String commandId = "0:0:command:" + getAttribute(commandsAttributes, "twinId").replace("\'", "") + ":"
					+ getAttribute(commandsAttributes, "executionId").replace("\'", "") + ":"
					+ getAttribute(commandsAttributes, "timestamp");
			
			commandsValues.put(SNAPSHOT_ID, commandId);

			for (String att : this.attributes.keySet()) {
				String attributeValue = getAttribute(commandsAttributes, att);
				System.out.println("[INFO-DT-Output] " + att + ": " + attributeValue);
				commandsValues.put(att, attributeValue);
				if (attributes.get(att).equals(NUMBER)) {
					addSearchRegister(att, Double.parseDouble(attributeValue.replace("'", "")), commandId, jedis);
				} else if (attributes.get(att).equals(BOOLEAN)) {
					addSearchRegister(att, Boolean.parseBoolean(attributeValue)?1:0, commandId, jedis);
				}
			}

			jedis.hset(commandId, commandsValues);
			
			api.deleteObjectEx(command.object());
		}
	}
}
