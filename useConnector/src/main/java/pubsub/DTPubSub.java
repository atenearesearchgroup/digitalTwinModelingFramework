package pubsub;

import java.util.List;
import java.util.Set;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.sys.MObjectState;

import digital.twin.InputSnapshotsManager;
import digital.twin.OutputSnapshotsManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class DTPubSub extends JedisPubSub {

	private UseSystemApi api;
	private Jedis jedis;
	private OutputSnapshotsManager snapshotsManager;

	public static final String DT_OUT_CHANNEL = "DTOutChannel";
	public static final String DT_IN_CHANNEL = "DTInChannel";

	public DTPubSub(UseSystemApi api, Jedis jedis) {
		this.api = api;
		this.jedis = jedis;
		this.snapshotsManager = new OutputSnapshotsManager();
	}

	@Override
	public void onMessage(String channel, String message) {
		switch (channel) {
		case DT_IN_CHANNEL: // Info entering USE
			try {
				Set<String> unprocessedSnapshots = InputSnapshotsManager.getUnprocessedSnapshots(jedis);
				InputSnapshotsManager.saveSnapshots(api, jedis, unprocessedSnapshots);
				System.out.println("[INFO-DT] New Input Snapshots saved");
			} catch (UseApiException e1) {
				e1.printStackTrace();
			}
			break;
		case DT_OUT_CHANNEL: // Info leaving USE
			try {
				List<MObjectState> snapshots = OutputSnapshotsManager.getSnapshots(api);
				this.snapshotsManager.saveSnapshots(api, jedis, snapshots);
				System.out.println("[INFO-DT] New Output Snapshots saved");
			} catch (UseApiException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("[WARNING-DT] Received message in unknown channel: " + channel);
			break;
		}
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		System.out.println("[INFO-DT] Client is Subscribed to channel : " + channel);
	}
}
