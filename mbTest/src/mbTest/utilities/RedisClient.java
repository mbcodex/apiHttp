package mbTest.utilities;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
	private Jedis jedis;
	private JedisPool jedisPool;
	private static RedisClient instance;

	private RedisClient() {
		initialPool();
		jedis = jedisPool.getResource();
	}

	public static RedisClient getInstance() {
		if (instance == null) {
			instance = new RedisClient();
		}
		return instance;
	}

	private void initialPool() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(20);
		config.setMaxWaitMillis(1000l);
		config.setTestOnBorrow(false);

		jedisPool = new JedisPool(config, PowinProperty.REDIS_HOST.toString(),
				Integer.parseInt(PowinProperty.REDIS_PORT.toString()));
	}

	public void flushAll() {
		jedis.flushAll();
	}
}
