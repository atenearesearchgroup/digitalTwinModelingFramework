package communication;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 * Sends the commands detected in the database to the car server.
 */
public class CommandsReporter implements Runnable {

    private final String COMMANDS_LIST = "commands";

    private final JedisPool jedisPool;
    private final Socket client;

    public CommandsReporter(JedisPool jedisPool, Socket client) throws IOException {
        this.jedisPool = jedisPool;
        this.client = client;
    }

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
                    bf.close();
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


}
