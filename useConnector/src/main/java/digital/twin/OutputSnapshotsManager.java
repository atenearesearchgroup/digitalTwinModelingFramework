package digital.twin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * 
 */
public class OutputSnapshotsManager {

	private final Map<String, String> attributes;
	private final String SNAPSHOT_ID = "snapshotId";
	private final String STRING = "str";
	private final String NUMBER = "double";
	private final String BOOLEAN = "boolean";
	private final String PROCESSED_SN_DT = "processedSnapsDT";

	/**
	 * It sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
	 * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
	 */
	public OutputSnapshotsManager() {
		this.attributes = new HashMap<>();

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
	 * It retrieves the OutputCarSnapshot objects from the currently displayed object diagram.
	 * 
	 * @param api		USE system API instance to interact with the currently displayed object diagram.
	 * @return			The list of OutputCarSnapshots available in the currently displayed object diagram.
	 */
	public static List<MObjectState> getSnapshots(UseSystemApi api) {
		List<MObjectState> snapshots = new ArrayList<MObjectState>();

		MClass snapshotClass = api.getSystem().model().getClass("OutputCarSnapshot");

		for (MObject o : api.getSystem().state().allObjects()) {
			if (o.cls().allSupertypes().contains(snapshotClass)) {
				MObjectState ostate = o.state(api.getSystem().state());
				snapshots.add(ostate);
			}
		}

		return snapshots;
	}

	/**
	 * It saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake. 
	 * Then, it removes them from the diagram.
	 * 
	 * @param api				USE system API instance to interact with the currently displayed object diagram.
	 * @param jedis				An instance of the Jedis client to access the data lake.
	 * @throws UseApiException
	 */
	public void saveSnapshots(UseSystemApi api, Jedis jedis) throws UseApiException {
		List<MObjectState> outputSnapshots = OutputSnapshotsManager.getSnapshots(api);
		for (MObjectState snapshot : outputSnapshots) {
			Map<String, String> carValues = new HashMap<>();
			Map<MAttribute, Value> snapshotAttributes = snapshot.attributeValueMap();

			String snapshotId = "DT:" + getAttribute(snapshotAttributes, "twinId").replace("\'", "") + ":"
					+ getAttribute(snapshotAttributes, "executionId").replace("\'", "") + ":"
					+ getAttribute(snapshotAttributes, "timestamp");
			carValues.put(SNAPSHOT_ID, snapshotId);

			for (String att : this.attributes.keySet()) {
				String attributeValue = getAttribute(snapshotAttributes, att);
				System.out.println("[INFO-DT-Output] " + att + ": " + attributeValue);
				carValues.put(att, attributeValue);
				if (attributes.get(att).equals(NUMBER)) {
					addSearchRegister(att, Double.parseDouble(attributeValue.replace("'", "")), snapshotId, jedis);
				} else if (attributes.get(att).equals(BOOLEAN)) {
					addSearchRegister(att, Boolean.parseBoolean(attributeValue)?1:0, snapshotId, jedis);
				}
			}

			jedis.hset(snapshotId, carValues);
			jedis.zadd(PROCESSED_SN_DT, 0, snapshotId);
			
			api.deleteObjectEx(snapshot.object());
		}
	}

	/**
	 * This method is equivalent to the redis command <i>ZADD DT_sensorKey_LIST score registryKey</i>
	 * 
	 * @param sensorKey			Sensor identifier.
	 * @param score				Value of the sensor readings.
	 * @param registryKey		Snapshot Id
	 * @param jedis				An instance of the Jedis client to access the data lake.
	 */
	private void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis) {
		jedis.zadd("DT_" + sensorKey + "_LIST", score, registryKey);
	}

	/**
	 * It retrieves an attribute with the name <i>attributeName</i> from a Map of attributes and values.
	 * 
	 * @param attributes 		Map with the attributes and its values.
	 * @param attributeName		Name of the attribute whose value is retrieved.
	 * @return
	 */
	private String getAttribute(Map<MAttribute, Value> attributes, String attributeName) {
		for (MAttribute snapshotKey : attributes.keySet()) {
			if (snapshotKey.name().equals(attributeName)) {
				return attributes.get(snapshotKey).toString();
			}
		}
		return null;
	}
}
