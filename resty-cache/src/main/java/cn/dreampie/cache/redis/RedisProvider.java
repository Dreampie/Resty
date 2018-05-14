package cn.dreampie.cache.redis;

import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheProvider;
import cn.dreampie.common.Constant;
import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.common.util.serialize.Serializer;
import cn.dreampie.log.Logger;
import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.util.*;

/**
 * Created by Dreampie on 15/4/24.
 */
public class RedisProvider extends CacheProvider {

  private static final Logger logger = Logger.getLogger(RedisProvider.class);
  private static final int PAGE_SIZE = 128;

  private static final Pool pool;
  private static final String host;
  private static final int timeout;
  private static final int expired;

  static {
    Prop config = null;
    try {
      config = Proper.use("redis.properties");
    } catch (Exception ignore) {
      logger.warn(ignore.getMessage());
    }
    if (config != null) {
      String shardHost = config.get("redis.shard.host");
      String[] hp;
      host = config.get("redis.host");
      timeout = config.getInt("redis.timeout", Protocol.DEFAULT_TIMEOUT);
      expired = config.getInt("redis.expired", -1);
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setLifo(config.getBoolean("redis.pool.lifo", BaseObjectPoolConfig.DEFAULT_LIFO));
      poolConfig.setMaxWaitMillis(config.getLong("redis.pool.maxWaitMillis", BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS));
      poolConfig.setMinEvictableIdleTimeMillis(config.getLong("redis.pool.minEvictableIdleTimeMillis", BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
      poolConfig.setSoftMinEvictableIdleTimeMillis(config.getLong("redis.pool.softMinEvictableIdleTimeMillis", BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
      poolConfig.setNumTestsPerEvictionRun(config.getInt("redis.pool.numTestsPerEvictionRun", BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN));
      poolConfig.setTestOnBorrow(config.getBoolean("redis.pool.testOnBorrow", BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW));
      poolConfig.setTestOnReturn(config.getBoolean("redis.pool.testOnReturn", BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN));
      poolConfig.setTestWhileIdle(config.getBoolean("redis.pool.testWhileIdle", BaseObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE));
      poolConfig.setTimeBetweenEvictionRunsMillis(config.getLong("redis.pool.timeBetweenEvictionRunsMillis", BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
      poolConfig.setEvictionPolicyClassName(config.get("redis.pool.evictionPolicyClassName", BaseObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME));
      poolConfig.setBlockWhenExhausted(config.getBoolean("redis.pool.blockWhenExhausted", BaseObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED));
      poolConfig.setJmxEnabled(config.getBoolean("redis.pool.jmxEnabled", BaseObjectPoolConfig.DEFAULT_JMX_ENABLE));
      poolConfig.setJmxNamePrefix(config.get("redis.pool.jmxNamePrefix", BaseObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX));

      //hp[0] is hostname,hp[1] is port,hp[2] is the password of redis
      if (shardHost != null) {
        String[] shards = shardHost.split(",");
        List<JedisShardInfo> shardInfos = new ArrayList<JedisShardInfo>();
        JedisShardInfo shardInfo;
        for (String s : shards) {
          hp = s.split(":");
          shardInfo = new JedisShardInfo(hp[0], Integer.parseInt(hp[1]), timeout);
          if (hp.length >= 3) {
            shardInfo.setPassword(hp[2]);
          }
          shardInfos.add(shardInfo);
        }
        pool = new ShardedJedisPool(poolConfig, shardInfos);
      } else {
        if (host != null) {
          hp = host.split(":");
          if(hp.length >= 3){
        	pool = new JedisPool(poolConfig, hp[0], Integer.parseInt(hp[1]), timeout, hp[2]);
          } else {
            pool = new JedisPool(poolConfig, hp[0], Integer.parseInt(hp[1]), timeout);
          }
        } else {
          pool = null;
          logger.error("Could not found 'redis.host' or 'redis.shard.host'");
        }
      }
    } else {
      host = "127.0.0.1:6379";
      pool = null;
      timeout = Protocol.DEFAULT_TIMEOUT;
      expired = -1;
    }
  }

  private ShardedJedis getShardedJedis() {
    ShardedJedis shardedJedis = null;
    if (pool != null && pool instanceof ShardedJedisPool) {
      shardedJedis = (ShardedJedis) pool.getResource();
    }
    return shardedJedis;
  }

  private Jedis getJedis() {
    Jedis jedis = null;
    if (pool != null && pool instanceof JedisPool) {
      jedis = (Jedis) pool.getResource();
    }
    //hp[0] is hostname,hp[1] is port,hp[2] is the password of redis
    if (jedis == null) {
      String[] hp = host.split(":");
      jedis = new Jedis(hp[0], Integer.parseInt(hp[1]), timeout);
      if (hp.length >= 3) {
        jedis.auth(hp[2]);
      }
    }
    return jedis;
  }

  private void returnResource(ShardedJedis shardedJedis, Jedis jedis) {
    if (pool != null) {
      if (shardedJedis != null) {
        pool.returnResource(shardedJedis);
      }
      if (jedis != null) {
        pool.returnResource(jedis);
      }
    } else {
      if (jedis != null) {
        jedis.disconnect();
      }
    }
  }

  private String getCacheKey(String group, String key) {
    return group + Constant.CONNECTOR + key;
  }

  public <T> T getCache(String group, String key) {
    String cacheKey = getCacheKey(group, key);
    ShardedJedis shardedJedis = null;
    Jedis jedis = null;
    try {
      shardedJedis = getShardedJedis();
      T cahe = null;
      if (shardedJedis != null) {
        cahe = (T) Serializer.deserialize(shardedJedis.get(cacheKey.getBytes()));
      } else {
        jedis = getJedis();
        if (jedis != null) {
          cahe = (T) Serializer.deserialize(jedis.get(cacheKey.getBytes()));
        }
      }
      return cahe;
    } catch (Exception e) {
      logger.warn("%s", e, e);
      return null;
    } finally {
      returnResource(shardedJedis, jedis);
    }
  }

  public void addCache(String group, String key, Object cache, int expired) {
    ShardedJedis shardedJedis = null;
    Jedis jedis = null;
    try {
      byte[] cacheKey = getCacheKey(group, key).getBytes();
      shardedJedis = getShardedJedis();
      if (shardedJedis != null) {
        shardedJedis.set(cacheKey, Serializer.serialize(cache));
        if (expired != -1) {
          shardedJedis.expire(cacheKey, expired);
          //添加group key
          addGroupKey(shardedJedis, group, key, expired);
          return;
        } else {
          if (RedisProvider.expired != -1) {
            shardedJedis.expire(cacheKey, RedisProvider.expired);
            //添加group key
            addGroupKey(shardedJedis, group, key, RedisProvider.expired);
            return;
          }
        }
        addGroupKey(shardedJedis, group, key);
      } else {
        jedis = getJedis();
        if (jedis != null) {
          jedis.set(cacheKey, Serializer.serialize(cache));
          if (expired != -1) {
            jedis.expire(cacheKey, expired);
            //添加到group key
            addGroupKey(jedis, group, key, expired);
            return;
          } else {
            if (RedisProvider.expired != -1) {
              jedis.expire(cacheKey, RedisProvider.expired);

              //添加到group key
              addGroupKey(jedis, group, key, RedisProvider.expired);
              return;
            }
          }
          addGroupKey(jedis, group, key);
        }
      }
    } catch (Exception e) {
      logger.warn("%s", e, e);
    } finally {
      returnResource(shardedJedis, jedis);
    }
  }

  public void removeCache(String group, String key) {
    String cacheKey = getCacheKey(group, key);
    ShardedJedis shardedJedis = null;
    Jedis jedis = null;
    try {
      shardedJedis = getShardedJedis();
      if (shardedJedis != null) {
        shardedJedis.del(cacheKey.getBytes());
        //删除 group key
        delGroupKey(shardedJedis, group, key);
      } else {
        jedis = getJedis();
        if (jedis != null) {
          jedis.del(cacheKey.getBytes());
          //删除 group key
          delGroupKey(jedis, group, key);
        }
      }
    } catch (Exception e) {
      logger.warn("%s", e, e);
    } finally {
      returnResource(shardedJedis, jedis);
    }
  }

  public void doFlush(CacheEvent event) {
    ShardedJedis shardedJedis = null;
    Jedis jedis = null;
    try {
      shardedJedis = getShardedJedis();
      if (shardedJedis != null) {
        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
          Collection<Jedis> shards = shardedJedis.getAllShards();
          for (Jedis j : shards) {
            j.flushDB();
          }
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
          byte[] groupRawKey=event.getGroup().getBytes();
          //从分组里取出所有的key
          String groupKeys = event.getGroup() + Constant.CONNECTOR + "keys";
          byte[] groupRawKeys = groupKeys.getBytes();
          Collection<Jedis> shards = shardedJedis.getAllShards();

          int offset = 0;
          boolean finished = false;

          do {
            // need to paginate the keys
            Set<byte[]> rawKeys = shardedJedis.zrange(groupRawKeys, (offset) * PAGE_SIZE, (offset + 1) * PAGE_SIZE - 1);
            finished = rawKeys.size() < PAGE_SIZE;
            offset++;
            if (!rawKeys.isEmpty()) {
              List<byte[]> groupedKeys = new ArrayList<byte[]>();
              for (byte[] rawKey : rawKeys) {
                groupedKeys.add(getGroupedKey(groupRawKey, rawKey));
              }

              byte[][] groupedRawKeys = groupedKeys.toArray(new byte[groupedKeys.size()][]);

              for (Jedis j : shards) {
                j.del(groupedRawKeys);
              }
            }
          } while (!finished);

          shardedJedis.del(groupKeys);
        }
      } else {
        jedis = getJedis();
        if (jedis != null) {
          if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            jedis.flushDB();
          } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
            byte[] groupRawKey=event.getGroup().getBytes();
            //从分组里取出所有的key
            String groupKeys = event.getGroup() + Constant.CONNECTOR + "keys";
            byte[] groupRawKeys = groupKeys.getBytes();

            int offset = 0;
            boolean finished = false;

            do {
              // need to paginate the keys
              Set<byte[]> rawKeys = jedis.zrange(groupRawKeys, (offset) * PAGE_SIZE, (offset + 1) * PAGE_SIZE - 1);
              finished = rawKeys.size() < PAGE_SIZE;
              offset++;
              if (!rawKeys.isEmpty()) {
                List<byte[]> groupedKeys = new ArrayList<byte[]>();
                for (byte[] rawKey : rawKeys) {
                  groupedKeys.add(getGroupedKey(groupRawKey, rawKey));
                }

                byte[][] groupedRawKeys = groupedKeys.toArray(new byte[groupedKeys.size()][]);
                jedis.del(groupedRawKeys);
              }
            } while (!finished);
            jedis.del(groupKeys);
          }
        }
      }
    } catch (Exception e) {
      logger.warn("%s", e, e);
    } finally {
      returnResource(shardedJedis, jedis);
    }
  }

  private void addGroupKey(Object jedis, String group, String key){
    addGroupKey(jedis,group,key,-1);
  }

  private void addGroupKey(Object jedis, String group, String key, int expired) {
    String groupKeys = group + Constant.CONNECTOR + "keys";
    byte[] groupRawKeys = groupKeys.getBytes();

    if (jedis instanceof ShardedJedis) {
      ((ShardedJedis) jedis).zadd(groupRawKeys, expired > 0 ? System.currentTimeMillis() + expired * 1000L : expired, Serializer.serialize(key));
    } else if (jedis instanceof Jedis) {
      ((Jedis) jedis).zadd(groupRawKeys, expired > 0 ? System.currentTimeMillis() + expired * 1000L : expired, Serializer.serialize(key));
    }
  }

  private void delGroupKey(Object jedis, String group, String key) {
    String groupKeys = group + Constant.CONNECTOR + "keys";
    byte[] groupRawKeys = groupKeys.getBytes();

    if (jedis instanceof ShardedJedis) {
      ((ShardedJedis) jedis).zrem(groupRawKeys, Serializer.serialize(key));
      ((ShardedJedis) jedis).zremrangeByScore(groupRawKeys, 0, System.currentTimeMillis());
    } else if (jedis instanceof Jedis) {
      ((Jedis) jedis).zrem(groupRawKeys, Serializer.serialize(key));
      ((Jedis) jedis).zremrangeByScore(groupRawKeys, 0, System.currentTimeMillis());
    }

  }

  /**
   * 获取添加了region的key
   *
   * @param groupRawKey
   * @param rawKey
   * @return
   */
  private byte[] getGroupedKey(byte[] groupRawKey, byte[] rawKey) {
    String cacheKey = new String(groupRawKey) + Constant.CONNECTOR + Serializer.deserialize(rawKey);
    byte[] regionedKey = cacheKey.getBytes();
    return regionedKey;
  }
}
