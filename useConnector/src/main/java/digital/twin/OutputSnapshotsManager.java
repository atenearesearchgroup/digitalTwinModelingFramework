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
public class OutputSnapshotsManager extends OutputManager {

    /**
     * It sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
     * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
     * TODO: Support a configuration file that determines the format of the information to be received, specifying which attributes we want to receive and store.
     */
    public OutputSnapshotsManager() {
        super();
        this.setChannel("DTOutChannel");
        this.retrievedClass = "OutputCarSnapshot";
        this.identifier = "processedSnapsDT";

        attributes.put("twinId", STRING);
        attributes.put("timestamp", NUMBER);
        attributes.put("executionId", NUMBER);

        attributes.put("xPos", NUMBER);
        attributes.put("yPos", NUMBER);
        attributes.put("angle", NUMBER);
        attributes.put("speed", NUMBER);

        attributes.put("light", NUMBER);
        attributes.put("distance", NUMBER);
        attributes.put("bump", BOOLEAN);
        attributes.put("isMoving", BOOLEAN);

        attributes.put("action", STRING);
    }

    /**
     * It saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake.
     * Then, it removes them from the diagram.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException
     */
    public void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> outputSnapshots = this.getObjects(api);
        for (MObjectState snapshot : outputSnapshots) {
            Map<String, String> carValues = new HashMap<>();
            Map<MAttribute, Value> snapshotAttributes = snapshot.attributeValueMap();

            String snapshotId = "DT:" + getAttribute(snapshotAttributes, "twinId").replace("'", "") + ":" + getAttribute(snapshotAttributes, "executionId").replace("'", "") + ":" + getAttribute(snapshotAttributes, "timestamp");
            saveAttributes(api, jedis, snapshot, carValues, snapshotAttributes, snapshotId);
        }
    }

}
