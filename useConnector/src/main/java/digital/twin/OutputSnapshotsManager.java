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
public class OutputSnapshotsManager extends OutputManager {

	private final String PROCESSED_SN_DT = "processedSnapsDT";

	/**
	 * It sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
	 * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
	 */
	public OutputSnapshotsManager() {
		super();
		this.retrievedClass = "OutputCarSnapshot";
		
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
	 * @param api				USE system API instance to interact with the currently displayed object diagram.
	 * @param jedis				An instance of the Jedis client to access the data lake.
	 * @throws UseApiException
	 */
	public void saveSnapshots(UseSystemApi api, Jedis jedis) throws UseApiException {
		List<MObjectState> outputSnapshots = this.getSnapshots(api);
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


}
