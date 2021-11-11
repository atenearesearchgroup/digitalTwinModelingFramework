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

public abstract class OutputManager {
	protected String retrievedClass;
	protected final Map<String, String> attributes;
	protected final String SNAPSHOT_ID = "snapshotId";
	protected final String STRING = "str";
	protected final String NUMBER = "double";
	protected final String BOOLEAN = "boolean";
	
	private String channel;
	
	public OutputManager() {
		this.attributes = new HashMap<>();
	}
	
	/**
	 * It retrieves the OutputCarSnapshot objects from the currently displayed object diagram.
	 * 
	 * @param api		USE system API instance to interact with the currently displayed object diagram.
	 * @return			The list of OutputCarSnapshots available in the currently displayed object diagram.
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
	 * @param sensorKey			Sensor identifier.
	 * @param score				Value of the sensor readings.
	 * @param registryKey		Snapshot Id
	 * @param jedis				An instance of the Jedis client to access the data lake.
	 */
	protected void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis) {
		jedis.zadd("DT_" + sensorKey + "_LIST", score, registryKey);
	}

	/**
	 * It retrieves an attribute with the name <i>attributeName</i> from a Map of attributes and values.
	 * 
	 * @param attributes 		Map with the attributes and its values.
	 * @param attributeName		Name of the attribute whose value is retrieved.
	 * @return
	 */
	protected String getAttribute(Map<MAttribute, Value> attributes, String attributeName) {
		for (MAttribute snapshotKey : attributes.keySet()) {
			if (snapshotKey.name().equals(attributeName)) {
				return attributes.get(snapshotKey).toString();
			}
		}
		return null;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
