package digital.twin;

import config.ConfigurationManager;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.type.Type;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class InputSnapshotsManager {

    private final static String PROCESSING_QUEUE_LIST = "PROCESSING_QUEUE_LIST";

    /**
     * Method to retrieve the list of unprocessed input snapshots from the database.
     * This method is equivalent to the query <i>ZRANGEBYLEX processingQueueList [0 (1</i>
     *
     * @param jedis An instance of the Jedis client to access the data lake.
     * @return Returns the list of unprocessed snapshots stored in the Data Lake
     */
    public static Set<String> getUnprocessedSnapshots(Jedis jedis) {
        return jedis.zrangeByLex(PROCESSING_QUEUE_LIST, "[0", "(1");
    }

    /**
     * Method to process a set of input snapshots. It creates the corresponding snapshot objects on USE
     * so that they are processed.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     */
    public static void saveSnapshots(UseSystemApi api, Jedis jedis, ConfigurationManager cm) throws UseApiException {
        Set<String> unprocessedSnapshots = InputSnapshotsManager.getUnprocessedSnapshots(jedis);
        for (String snapshot : unprocessedSnapshots) {
            String snapshotId = snapshot.substring(2);
            Map<String, String> values = jedis.hgetAll(snapshotId);

            String snapshotName = "in" + snapshotId.split(":")[0] + snapshotId.split(":")[2];
            api.createObject(cm.getInputClass(), snapshotName);
            System.out.println("[Snapshot] " + snapshotName);

            for (String value : values.keySet()) {
                //System.out.println("[Snapshot] " + value + " : " + values.get(value));
                MAttribute attribute = api.getSystem().model().getClass(cm.getInputClass()).attribute(value, true);
                if (attribute != null) {
                    Type type = attribute.type();
                    //System.out.println("[Snapshot] " + type);
                    api.setAttributeValue(snapshotName, value, setOCLExpression(type, values.get(value)));
                }
            }

            jedis.zrem(PROCESSING_QUEUE_LIST, snapshot);
            jedis.zadd(PROCESSING_QUEUE_LIST, 0, "1:" + snapshotId);
            jedis.hset(snapshotId, "processingQueue", "1");
        }
    }

    /**
     * Method to transform Strings stored in the database into valid OCL expressions for USE.
     *
     * @param type  Type of the parameter retrieved from the database.
     * @param value Value of the parameter retrieved.
     * @return A String valid for USE.
     */
    private static String setOCLExpression(Type type, String value) {
        if (type.isTypeOfReal() && !value.contains(".")) {
            value += ".0";
        } else if (type.isTypeOfString()) {
            value = "'" + value + "'";
        } else if (type.isTypeOfBoolean()) {
            if (value.equals("0")) {
                value = "false";
            } else {
                value = "true";
            }
        } else if (type.isTypeOfEnum()) {
            value = type + "::" + value;
        }
        return value;
    }
}
