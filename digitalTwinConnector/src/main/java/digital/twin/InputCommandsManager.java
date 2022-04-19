package digital.twin;

import config.ConfigurationManager;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.type.Type;
import redis.clients.jedis.Jedis;
import util.OCLUtil;
import util.StringUtil;

import java.util.Map;
import java.util.Set;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class InputCommandsManager extends InputManager{

    public InputCommandsManager(){
        this.CLASS_KEY = "NiryoNedArm:PROCESSEDDT_LIST";
    }

    /**
     * Method to process a set of input commands. It creates the corresponding commands objects on USE
     * so that they are processed.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     */
    public void saveObjects(UseSystemApi api, Jedis jedis, ConfigurationManager cm) throws UseApiException {
        Set<String> unprocessedCommands = getUnprocessedObjects(jedis);
        for (String command : unprocessedCommands) {
            Map<String, String> values = jedis.hgetAll(command);

            String snapshotName = "in" + StringUtil.toCamel(command.split(":")[0])
                    + StringUtil.toCamel(command.split(":")[1])
                    + StringUtil.toCamel(command.split(":")[2]);
            System.out.println("[Snapshot] " + snapshotName);
            api.createObject(cm.getInCommandClass(), snapshotName);
            System.out.println("[Snapshot-created] " + snapshotName);

            for (String value : values.keySet()) {
                //System.out.println("[Snapshot] " + value + " : " + values.get(value));
                MAttribute attribute = api.getSystem().model().getClass(cm.getInCommandClass()).attribute(value, true);
                if (attribute != null) {
                    Type type = attribute.type();
                    //System.out.println("[Snapshot] " + type);
                    api.setAttributeValue(snapshotName, value, OCLUtil.setOCLExpression(type, values.get(value)));
                }
            }
            jedis.zincrby(CLASS_KEY, 1, command);
            jedis.hset(command, "processedDT", "1");
        }
    }
}
