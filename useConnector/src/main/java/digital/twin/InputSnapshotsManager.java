package digital.twin;

import java.util.Map;
import java.util.Set;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.type.Type;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class InputSnapshotsManager {
	
	private final static String PROCESSING_QUEUE_LIST = "processingQueueList";

	public static Set<String> getUnprocessedSnapshots(Jedis jedis) {
		Set<String> snapshots = jedis.zrangeByLex(PROCESSING_QUEUE_LIST, "[0", "(1");
		return snapshots;
	}

	public static void saveSnapshots(UseSystemApi api, Jedis jedis, Set<String> snapshots) throws UseApiException {
		for (String snapshot : snapshots) {
			String snapshotId = snapshot.substring(2, snapshot.length());
			Map<String, String> values = jedis.hgetAll(snapshotId);
			
			String snapshotName = "in" + snapshotId.split(":")[0] + snapshotId.split(":")[2];
			api.createObject("InputCarSnapshot", snapshotName);
			System.out.println("[Snapshot] " + snapshotName);

			for(String value : values.keySet()) {
				//System.out.println("[Snapshot] " + value + " : " + values.get(value));
				MAttribute attribute = api.getSystem().model().getClass("InputCarSnapshot").attribute(value, true);
				if(attribute != null) {
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
	
	private static String setOCLExpression(Type type, String value) {
		if(type.isTypeOfReal() && !value.contains(".")) {			
			value += ".0";
		} else if(type.isTypeOfString()) {
			value = "'" + value + "'";
		} else if(type.isTypeOfBoolean()) {
			if(value.equals("0")) {
				value = "false";
			} else {
				value = "true";
			}
		} else if(type.isTypeOfEnum()) {
			value = type + "::" + value;
		}
		return value;
	}
}
