package digital.twin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OutputManager {
    protected final Map<String, String> attributes;
    protected final String SNAPSHOT_ID = "snapshotId";
    protected final String STRING = "str";
    protected final String NUMBER = "double";
    protected final String BOOLEAN = "boolean";
    protected String retrievedClass;
    protected String identifier;
    private String channel;

    public OutputManager() {
        this.attributes = new HashMap<>();
    }

    /**
     * It retrieves the OutputCarSnapshot objects from the currently displayed object diagram.
     *
     * @param api USE system API instance to interact with the currently displayed object diagram.
     * @return The list of OutputCarSnapshots available in the currently displayed object diagram.
     */
    public List<MObjectState> getObjects(UseSystemApi api) {
        List<MObjectState> snapshots = new ArrayList<MObjectState>();

        MClass snapshotClass = api.getSystem().model().getClass(this.retrievedClass);

        for (MObject o : api.getSystem().state().allObjects()) {
            if (o.cls().allSupertypes().contains(snapshotClass)) {
                MObjectState ostate = o.state(api.getSystem().state());
                snapshots.add(ostate);
            }
        }

        return snapshots;
    }

    public abstract void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException;

    /**
     * This method is equivalent to the redis command <i>ZADD DT_sensorKey_LIST score registryKey</i>
     *
     * @param sensorKey   Sensor identifier.
     * @param score       Value of the sensor readings.
     * @param registryKey Snapshot Id
     * @param jedis       An instance of the Jedis client to access the data lake.
     */
    protected void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis, String executionId) {
        jedis.zadd(executionId + ":" + sensorKey.toUpperCase() + "_LIST", score, registryKey);
    }

    /**
     * It retrieves an attribute with the name <i>attributeName</i> from a Map of attributes and values.
     *
     * @param attributes    Map with the attributes and its values.
     * @param attributeName Name of the attribute whose value is retrieved.
     * @return The corresponding attribute value
     */
    protected String getAttribute(Map<MAttribute, Value> attributes, String attributeName) {
        for (MAttribute snapshotKey : attributes.keySet()) {
            if (snapshotKey.name().equals(attributeName)) {
                return attributes.get(snapshotKey).toString();
            }
        }
        return null;
    }

    /**
     * Auxiliary method to store the attributes in the database, extracted from the diagram.
     *
     * @param api                USE system API instance to interact with the currently displayed object diagram.
     * @param jedis              An instance of the Jedis client to access the data lake.
     * @param snapshot           An instance of the Snapshot Object retrieved from USE
     * @param carValues          List with the attributes retrieved from the Snapshot
     * @param snapshotAttributes List with the name of the attributes in the snapshot class
     * @param snapshotId         Snapshot identifier
     * @throws UseApiException Any error related to the USE API
     */
    protected void saveAttributes(UseSystemApi api, Jedis jedis, MObjectState snapshot, Map<String, String> carValues, Map<MAttribute, Value> snapshotAttributes, String snapshotId) throws UseApiException {
        carValues.put(SNAPSHOT_ID, snapshotId);
        String executionId = snapshotId.substring(0, snapshotId.lastIndexOf(":"));

        for (String att : this.attributes.keySet()) {
            String attributeValue = getAttribute(snapshotAttributes, att);
            System.out.println("[INFO-DT-Output] " + att + ": " + attributeValue);
            carValues.put(att, attributeValue);
            if (attributes.get(att).equals(NUMBER)) {
                addSearchRegister(att, Double.parseDouble(attributeValue.replace("'", "")), snapshotId, jedis, executionId);
            } else if (attributes.get(att).equals(BOOLEAN)) {
                addSearchRegister(att, Boolean.parseBoolean(attributeValue) ? 1 : 0, snapshotId, jedis, executionId);
            }
        }

        jedis.hset(snapshotId, carValues);
        jedis.zadd(identifier, 0, snapshotId);

        api.deleteObjectEx(snapshot.object());
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
