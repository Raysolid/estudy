package com.estudy.utils;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 普通缓存放入并设置时间
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("续时redisKey:{},time:{}失败", key, time);
            return false;
        }
    }

    public Long count(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    /**
     * List集合操作
     */
    public List<V> getQueueList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public boolean lpush(String key, V value, Long time) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time != null && time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("入队redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public long remove(String key, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, 1, value);
        } catch (Exception e) {
            logger.error("移除redisKey:{},value:{}失败", key, value);
            return 0;
        }
    }

    public boolean lpushAll(String key, List<V> values, long time) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("批量入队redisKey:{},values:{}失败", key, values);
            return false;
        }
    }

    /**
     * BitMap操作
     */
    public boolean setBit(String key, int offset, boolean value) {
        try {
            return redisTemplate.opsForValue().setBit(key, offset, value);
        } catch (Exception e) {
            logger.error("setBit失败,key:{},value:{}", key, value);
            return false;
        }
    }

    public boolean getBit(String key, long offset) {
        try {
            return redisTemplate.opsForValue().getBit(key, offset);
        } catch (Exception e) {
            logger.error("getBit失败,key:{},offset:{}", key, offset);
            return false;
        }
    }

    public List<Long> bitField(String key, int bits) {
        try {
            return redisTemplate.opsForValue().bitField(
                    key, BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(bits)).valueAt(0)
            );
        } catch (Exception e) {
            logger.error("获取bitField失败,key:{}", key);
            return null;
        }
    }

    /**
     * Set集合操作
     */
    // Set集合操作
    public void addToSet(String key, V... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public boolean isSetMember(String key, V value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long removeFromSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

}