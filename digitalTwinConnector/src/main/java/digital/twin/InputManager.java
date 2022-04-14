package digital.twin;

import config.ConfigurationManager;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import redis.clients.jedis.Jedis;

import java.util.Set;

public abstract class InputManager {

    String CLASS_KEY = "";

    /**
     * Method to retrieve the list of unprocessed input commands from the database.
     * This method is equivalent to the query <i>ZRANGEBYSCORE <key> [0 (1</i>
     *
     * @param jedis An instance of the Jedis client to access the data lake.
     * @return Returns the list of unprocessed snapshots stored in the Data Lake
     */
    public Set<String> getUnprocessedObjects(Jedis jedis) {
        return jedis.zrangeByScore(this.CLASS_KEY, "[0", "(1");
    }

    public abstract void saveObjects(UseSystemApi api, Jedis jedis, ConfigurationManager cm) throws UseApiException;
}
