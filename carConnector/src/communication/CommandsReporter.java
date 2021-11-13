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

	private JedisPool jedisPool;
	private Socket client;
	private int port;
	private boolean running;
	private int sleepTime;

	public CommandsReporter(JedisPool jedisPool, int port, int sleepTime) throws IOException {
		this.jedisPool = jedisPool;
		this.running = true;
		this.port = port;
		this.sleepTime = sleepTime;
		
		try {
			connectToServer();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void run() {
		while (running) {
			Jedis jedis = jedisPool.getResource();
			Set<String> commands = getUnprocessedCommands(jedis);
			if (!commands.isEmpty()) {
				for (String c : commands) {
					String action = jedis.hgetAll(c).get("action");
					try {
						BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
						bf.write(action + "\n");
						bf.flush();
						System.out.println("[INFO-PT-CommandsReporter] " + action);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
			try {
				Thread.sleep(this.sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Set<String> getUnprocessedCommands(Jedis jedis) {
		Set<String> unprocessedCommands = jedis.zrangeByLex(COMMANDS_LIST, "[0:0", "(1");
		return unprocessedCommands;
	}

	public void stop() {
		this.running = false;
	}

	public void connectToServer() throws UnknownHostException, IOException, InterruptedException {
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
