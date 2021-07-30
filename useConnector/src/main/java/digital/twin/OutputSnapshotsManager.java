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
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class OutputSnapshotsManager {

	private final Map<String, String> attributes;
	private final String SNAPSHOT_ID = "snapshotId";
	private final String STRING = "str";
	private final String NUMBER = "double";
	private final String BOOLEAN = "boolean";
	private final String PROCESSED_SN_DT = "processedSnapsDT";

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

	public void saveSnapshots(UseSystemApi api, Jedis jedis, List<MObjectState> snapshots) throws UseApiException {
		for (MObjectState snapshot : snapshots) {
			Map<String, String> carValues = new HashMap<>();
			Map<MAttribute, Value> snapshotAttributes = snapshot.attributeValueMap();

			String snapshotId = "DT:" + getAttribute(snapshotAttributes, "twinId") + ":"
					+ getAttribute(snapshotAttributes, "executionId") + ":"
					+ getAttribute(snapshotAttributes, "timestamp");
			carValues.put(SNAPSHOT_ID, snapshotId);

			for (String att : this.attributes.keySet()) {
				processAttribute(this.attributes, snapshotAttributes, carValues, att, snapshotId, jedis);
			}

			jedis.hset(snapshotId, carValues);
			jedis.zadd(PROCESSED_SN_DT, 0, snapshotId);
			
			api.deleteObjectEx(snapshot.object());
		}
	}

	private void processAttribute(Map<String, String> attributes, Map<MAttribute, Value> snapshotAttributes,
			Map<String, String> carValues, String attributeKey, String snapshotId, Jedis jedis) {
		String attributeValue = getAttribute(snapshotAttributes, attributeKey);
		System.out.println("[INFO-DT-Output] " + attributeKey + ": " + attributeValue);
		carValues.put(attributeKey, attributeValue);
		if (attributes.get(attributeKey).equals(NUMBER)) {
			addSearchRegister(attributeKey, Double.parseDouble(attributeValue.replace("'", "")), snapshotId, jedis);
		} else if (attributes.get(attributeKey).equals(BOOLEAN)) {
			addSearchRegister(attributeKey, Boolean.parseBoolean(attributeValue)?1:0, snapshotId, jedis);
		}
		
	}

	private void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis) {
		jedis.zadd("DT_" + sensorKey + "_LIST", score, registryKey);
	}

	private String getAttribute(Map<MAttribute, Value> attributes, String attributeName) {
		for (MAttribute snapshotKey : attributes.keySet()) {
			if (snapshotKey.name().equals(attributeName)) {
				return attributes.get(snapshotKey).toString();
			}
		}
		return null;
	}
}
