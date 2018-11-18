package com.kunda.engine.tools.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Description: 带有效期map
 * @author: yawen
 * @date: 2016-11-24 下午4:08:46
 * @param <K>
 * @param <V>
 */
public class ExMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(ExMap.class);

    private static final long serialVersionUID = 1L;

    /**
     * 默认过期时间5分钟
     */
    private long EXPIRY = 1000 * 60 * 5;

    private ConcurrentHashMap<K, Long> expiryMap = new ConcurrentHashMap<>();

    public ExMap() {
        super();
    }

    public ExMap(long defaultExpiryTime) {
        this(1 << 4, defaultExpiryTime);
    }

    public ExMap(int initialCapacity, long defaultExpiryTime) {
        super(initialCapacity);
        this.EXPIRY = defaultExpiryTime;
    }

    public V put(K key, V value) {
        expiryMap.put(key, System.currentTimeMillis() + EXPIRY);
        return super.put(key, value);
    }

    public boolean containsKey(Object key) {
        return !checkExpiry(key, true) && super.containsKey(key);
    }

    /**
     * @param key
     * @param value
     * @param expiryTime 键值对有效期 毫秒
     * @return
     */
    public V put(K key, V value, long expiryTime) {
        expiryMap.put(key, System.currentTimeMillis() + expiryTime);
        return super.put(key, value);
    }

    public int size() {
        return entrySet().size();
    }

    public boolean isEmpty() {
        return entrySet().size() == 0;
    }

    public boolean containsValue(Object value) {
        if (value == null) return false;
        Set<Map.Entry<K, V>> set = super.entrySet();
        Iterator<Map.Entry<K, V>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            if (value.equals(entry.getValue())) {
                if (checkExpiry(entry.getKey(), false)) {
                    iterator.remove();
                    return false;
                } else return true;
            }
        }
        return false;
    }

    public Collection<V> values() {

        Collection<V> values = super.values();

        if (values == null || values.size() < 1) return values;

        Iterator<V> iterator = values.iterator();

        while (iterator.hasNext()) {
            V next = iterator.next();
            if (!containsValue(next)) iterator.remove();
        }
        return values;
    }

    public V get(Object key) {
        if (key == null)
            return null;
        if (checkExpiry(key, true))
            return null;
        return super.get(key);
    }

    /**
     * @param key
     * @return null:不存在或key为null -1:过期  存在且没过期返回value 因为过期的不是实时删除，所以稍微有点作用
     * @Description: 是否过期
     */
    public Object isInvalid(Object key) {
        if (key == null)
            return null;
        if (!expiryMap.containsKey(key)) {
            return null;
        }
        long expiryTime = expiryMap.get(key);

        boolean flag = System.currentTimeMillis() > expiryTime;

        if (flag) {
            super.remove(key);
            expiryMap.remove(key);
            return -1;
        }
        return super.get(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            expiryMap.put(e.getKey(), System.currentTimeMillis() + EXPIRY);
        super.putAll(m);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = super.entrySet();
        Iterator<Map.Entry<K, V>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            if (checkExpiry(entry.getKey(), false)) iterator.remove();
        }

        return set;
    }

    /**
     * @param isRemoveSuper true super删除
     * @return
     * @Description: 是否过期
     * @author: qd-ankang
     * @date: 2016-11-24 下午4:05:02
     */
    private boolean checkExpiry(Object key, boolean isRemoveSuper) {

        if (!expiryMap.containsKey(key)) {
            return false;
        }
        long expiryTime = expiryMap.get(key);

        boolean flag = System.currentTimeMillis() > expiryTime;

        if (flag) {
            if (isRemoveSuper)
                super.remove(key);
            expiryMap.remove(key);
        }
        return flag;
    }


}