package communication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CommandsReporter implements Runnable {

	private final String COMMANDS_LIST = "commands";

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
		this.client = new Socket("localhost", this.port);
	}

	@Override
	public void run() {
		while(running) {
			Jedis jedis = jedisPool.getResource();
			Set<String> commands = getUnprocessedCommands(jedis);
			if(commands.isEmpty()) {
				for(String c : commands) {					
					String action = jedis.hgetAll(c).get("action");		
					
					try {
						BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
						System.out.println("[INFO-PT-CommandsReporter] " + action);
						bf.write(action);
						bf.flush();
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

}
