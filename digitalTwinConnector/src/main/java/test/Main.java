package test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        Jedis jedis = jedisPool.getResource();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String commandId = "command:NiryoNedArm:"+ timestamp;

        Map<String, String> commandValues = new HashMap<>();
        commandValues.put("twinId", "NiryoNedArm");
        commandValues.put("timestamp", timestamp);
        commandValues.put("action", "RotateAllServos");
        commandValues.put("processedDT", "0");
        commandValues.put("processedPT", "0");
        commandValues.put("args", "90,90,90,90,90,90");

        jedis.hset(commandId, commandValues);
        for(String value : commandValues.keySet()){
            if(value.equals("timestamp") || value.contains("processed")){
                jedis.zadd(("NiryoNedArm:" + value.toUpperCase() + "_LIST"), Double.parseDouble(commandValues.get(value)),commandId);
            }
        }
        jedisPool.returnResource(jedis);
    }
}
