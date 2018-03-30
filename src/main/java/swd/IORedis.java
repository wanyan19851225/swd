package swd;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IORedis {
	
	private static JedisPool jedisPool;
	
	public void init(String adr,String auth,int port){

		if(jedisPool==null){
		
			int MAX_ACTIVE = 1024;

			int MAX_IDLE = 200;

			int MAX_WAIT = 10000;
   
			int TIMEOUT = 10000;

			boolean TEST_ON_BORROW = true;

			try {
		
				JedisPoolConfig config = new JedisPoolConfig();
		
				config.setMaxTotal(MAX_ACTIVE);
			
				config.setMaxIdle(MAX_IDLE);
		
				config.setMaxWaitMillis(MAX_WAIT);
		
				config.setTestOnBorrow(TEST_ON_BORROW);
		
				jedisPool = new JedisPool(config,adr,port, TIMEOUT,auth);
			} catch (Exception e) {
		
				e.printStackTrace();
			}
		
			}
		
		}
	
	public synchronized static Jedis getJedis() {

		try {
			if (jedisPool!=null) {
				Jedis resource = jedisPool.getResource();
				return resource;
			}else{
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	public static void returnResource(Jedis jedis) {

		if (jedis != null) 
			jedis.close();
	}
	
	
}
