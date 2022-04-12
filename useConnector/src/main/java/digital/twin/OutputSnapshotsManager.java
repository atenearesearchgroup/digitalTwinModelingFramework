package digital.twin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObjectState;
import org.yaml.snakeyaml.Yaml;
import redis.clients.jedis.Jedis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class OutputSnapshotsManager extends OutputManager {

    final String CONFIGURATION_FILE_PATH = "./DTConfig.yaml";
    final String RETRIEVED_CLASS = "retrievedClass";
    final String ATTRIBUTES = "attributes";

    /**
     * It sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
     * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
     */
    @SuppressWarnings("unchecked")
    public OutputSnapshotsManager() {
        super();
        this.setChannel("DTOutChannel");
        Map<String, Object> configurationParameters = loadConfigurationFile();

        this.retrievedClass = configurationParameters.get(RETRIEVED_CLASS).toString();
        this.identifier = "processedSnapsDT";

        Map<String, Object> attsConfiguration = (Map<String, Object>) configurationParameters.get(ATTRIBUTES);
        for(String attName : attsConfiguration.keySet()){
            attributes.put(attName, attsConfiguration.get(attName).toString());
        }
    }

    /**
     * It saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake.
     * Then, it removes them from the diagram.
     *
     * @param api   USE system API instances to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
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

    private Map<String, Object> loadConfigurationFile() {
        InputStream inputStream = null;
        Yaml yaml = new Yaml();
        try {
            inputStream = new FileInputStream(CONFIGURATION_FILE_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return yaml.load(inputStream);
    }
}
