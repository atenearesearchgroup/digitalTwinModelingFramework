package digital.twin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class MismatchSnapshotsManager {
	
	private final static String PROCESSING_QUEUE_LIST = "processingQueueList";
	private final static String PROCESSED_SNAPS = "processedSnapsDT";
	
	private final static String INPUT_CAR_SNAPSHOT = "InputCarSnapshot";
	private final static String OUTPUT_CAR_SNAPSHOT = "OutputCarSnapshot";
	
	private String executionId;
	private String twinId;
	private UseSystemApi api;
	private Jedis jedis;
	
	public MismatchSnapshotsManager(UseSystemApi api, Jedis jedis) {
		this.api = api;
		this.jedis = jedis;
		this.executionId = getConfigurationAttributeValue("executionId");
		this.twinId = getConfigurationAttributeValue("twinId");
	}

	public Set<String> getPTSnapshots() {
		Set<String> pTSnapshots = jedis.zrangeByLex(PROCESSING_QUEUE_LIST, "[1:" + this.twinId + ":" + this.executionId, "(2");
		return pTSnapshots;
	}
	
	public Set<String> getDTSnapshots(Set<String> pTSnapshots){
		Set<String> dTSnapshots = new HashSet<>();
		Set<String> allDTSnapshots = jedis.zrangeByLex(PROCESSED_SNAPS, "[A", "(Z");
		for(String snapshot : pTSnapshots) {
			String key = "DT:" + snapshot.substring(2);
			if(allDTSnapshots.contains(key)) {
				dTSnapshots.add(key);
			}
		}
		return dTSnapshots;
	}

	public void saveSnapshots() throws UseApiException {
		Set<String> pTSnapshots = getPTSnapshots();
		storeObjects(pTSnapshots, INPUT_CAR_SNAPSHOT, "in", 2);
		
		Set<String> dTSnapshots = getDTSnapshots(pTSnapshots);
		storeObjects(dTSnapshots, OUTPUT_CAR_SNAPSHOT, "out", 3);
	}

	private void storeObjects(Set<String> snapshots, String objectName, String prefixId, int prefixLength) throws UseApiException {
		for (String snapshot : snapshots) {
			String snapshotId = snapshot.substring(prefixLength, snapshot.length());
			Map<String, String> values = jedis.hgetAll(snapshotId);
			
			String snapshotName = prefixId + snapshotId.split(":")[0] + snapshotId.split(":")[2];	
			api.createObject(objectName, snapshotName);
			System.out.println("[Snapshot] " + snapshotName);

			for(String value : values.keySet()) {
				//System.out.println("[Snapshot] " + value + " : " + values.get(value));
				MAttribute attribute = api.getSystem().model().getClass(objectName).attribute(value, true);
				if(attribute != null) {
					Type type = attribute.type();
					//System.out.println("[Snapshot] " + type);
					api.setAttributeValue(snapshotName, value, setOCLExpression(type, values.get(value)));					
				}
			}
		}
	}
	
	private String getConfigurationAttributeValue(String attribute) {
		String attributeValue = "";
		MClass analysisClass = api.getSystem().model().getClass("AnalysisConfiguration");

		for (MObject o : api.getSystem().state().allObjects()) {
			if (o.cls().allSupertypes().contains(analysisClass)) {
				MObjectState ostate = o.state(api.getSystem().state());
				attributeValue = ostate.attributeValue(attribute).toString();
				break;
			}
		}
		
		return attributeValue;
	}
	
	private String setOCLExpression(Type type, String value) {
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
