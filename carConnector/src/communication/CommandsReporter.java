package communication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CommandsReporter implements Runnable {

	private final String COMMANDS_LIST = "commands";
	private final int TRIAL_THRESHOLD = 200;

	private final JedisPool jedisPool;
	private Socket client;
	private final int port;
	private boolean running;

	public CommandsReporter(JedisPool jedisPool, int port) throws IOException {
		this.jedisPool = jedisPool;
		this.running = true;
		this.port = port;

		try {
			connectToServer();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	// TODO: revisar el papel que tienen los buffers en la ejecución, porque causan excepciones
	@Override
	public void run() {
			Jedis jedis = jedisPool.getResource();
			Set<String> commands = getUnprocessedCommands(jedis);
			if (!commands.isEmpty()) {
				for (String c : commands) {
					String action = jedis.hgetAll(c).get("action");
					try {
						BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
						bf.write(action + "\n");
						bf.flush();
						String newEntry = "1" + c.substring(1);
						jedis.rename(c, newEntry);
						jedis.zrem("commands", c);
						jedis.zadd(c, 0, newEntry);
						System.out.println("[INFO-PT-CommandsReporter] " + action);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} else {
				System.out.println("[INFO-PT-CommandsReporter] No new commands.");
			}
			jedisPool.returnResource(jedis);

	}

	public Set<String> getUnprocessedCommands(Jedis jedis) {
		return jedis.zrangeByLex(COMMANDS_LIST, "[0:0", "(1");
	}

	public void stop() {
		this.running = false;
	}

	public void connectToServer() throws IOException, InterruptedException {
		int retryCounter = 0;
		this.client = null;

		while (retryCounter < TRIAL_THRESHOLD) {
			try {
				retryCounter++;
				this.client = new Socket("localhost", this.port);
				if (this.client == null) {
					System.out.println("[INFO-ERROR] Car server is not available.");
				} else {
					System.out.println("[INFO-PT] Connected to Car server successfully.");
					break;
				}
			} catch (ConnectException e) {
				Thread.sleep(500);
			}
		}
		
	}

}
