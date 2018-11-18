package com.kunda.engine.manager.redis;

import com.kunda.engine.core.ServerBootStrap;
import redis.clients.jedis.*;

import java.util.*;

import static com.kunda.engine.common.fun.Avatas.StringList2StringArray;


/**
 * Created by 亚文 on 2016/9/7.
 */


public class Rdm {

    private static JedisPool pool;//redis线程池
    private static Rdm instance = new Rdm();
    private static JedisCluster cluster;//集群
    private boolean mode = false;//是否开启集群模式

    /**
     * 构造
     * */
    private Rdm() {

    }

    public static Rdm instance(){
        return instance;
    }

    public void init() {
        Properties props = ServerBootStrap.getInstance().loadDefaultConfigFile();

        int timeout = Integer.parseInt(props.getProperty("redis.timeout","10000"));
        int maxTotal = Integer.parseInt(props.getProperty("redis.maxtotal","1000"));
        int maxIdel = Integer.parseInt(props.getProperty("redis.maxidel","400"));
        String host = props.getProperty("redis.host");
        int port = Integer.parseInt(props.getProperty("redis.port"));
        String password = props.getProperty("redis.password");

        mode = Boolean.parseBoolean(props.getProperty("redis.cluster"));//集群模式
        String rnodes = props.getProperty("redis.nodes");//节点
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdel);
        config.setMaxWaitMillis(-1);

        if(mode){ //集群模式
            Set<HostAndPort> nodes = new LinkedHashSet<>();
            String[] hostposts =   rnodes.split(",");
            for(String hp : hostposts){
                String[] hport =  hp.split(":");
                nodes.add(new HostAndPort(hport[0],Integer.parseInt(hport[1])) );
            }
            cluster = new JedisCluster(nodes,timeout,maxIdel,maxTotal,password, config);
        } else if (pool == null) {
            pool = new JedisPool(config, host, port, timeout ,password , 0 );
        }

    }


    /**
     * 非集群模式初始化
     * */
    public void init( String host,String password,boolean mode ,String rnodes) {
        int port = 6379;
        int db =  5;
        int timeout = 10000;
        int maxTotal = 1000;
        int maxIdel = 400;

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdel);
        config.setMaxWaitMillis(-1);

        if(mode){ //集群模式
            Set<HostAndPort> nodes = new LinkedHashSet<>();
            String[] hostposts =   rnodes.split(",");
            for(String hp : hostposts){
                String[] hport =  hp.split(":");
                nodes.add(new HostAndPort(hport[0],Integer.parseInt(hport[1])) );
            }
            cluster = new JedisCluster(nodes, config);
        } else if (pool == null) {
            pool = new JedisPool(config, host, port, timeout ,password , db );
        }
    }


    public  String get(String key){
        if(mode){
            return  cluster.get(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.get(key);
            }
        }

    }


    public  String hGet (String key,String field){

        if(mode){
            return  cluster.hget(key,field);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hget(key,field);
            }
        }
    }

    //批量删除
    public void batchdel(String keys){
        if(mode){
            Map<String, JedisPool> nodes =  cluster.getClusterNodes();
            for( Map.Entry<String, JedisPool> entry :  nodes.entrySet()){
                try(Jedis jedis =  entry.getValue().getResource()){
                    Set<String> allkey =  jedis.keys(keys);
                    for( String key :  allkey){
                        if(cluster.exists(key)){
                            cluster.del(key);
                        }
                    }
                }
            }
        }else {
            try(Jedis jedis = pool.getResource()) {
                Set<String>  allkey =  jedis.keys(keys);
                for( String key :  allkey){
                    jedis.del(key);
                }
            }
        }
    }

    //模糊查询
    public Set<String> keys(String keys){
        if(mode){
            Map<String, JedisPool> nodes =  cluster.getClusterNodes();
            TreeSet<String>  tree = new TreeSet<>();
            for( Map.Entry<String, JedisPool> entry :  nodes.entrySet()){
                try(Jedis jedis =  entry.getValue().getResource()){
                    tree.addAll(jedis.keys(keys));
                }
            }
            return tree;
        }else {
            try(Jedis jedis = pool.getResource()) {
               return jedis.keys(keys);

            }
        }
    }



    public  List<String> hmget(String key, String ... field) {

        if(mode){
            return  cluster.hmget(key,field);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hmget(key,field);
            }
        }
    }





    public  List<String> hmget(String key, List<String>  fields){

        if(mode){
            return  cluster.hmget(key, StringList2StringArray(fields));
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hmget(key,StringList2StringArray(fields));
            }
        }
    }





    public  void set(String key, String value){
        if(mode){
              cluster.set(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                 jedis.set(key,value);
            }
        }
    }

    public  void set(byte[] key, byte[]  value){
        if(mode){
            cluster.set(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.set(key,value);
            }
        }
    }

    public  void setExpireTime(String key, int seconds){
        if(mode){
            cluster.expire(key, seconds);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.expire(key, seconds);
            }
        }
    }


    public  void setPExpireTime(String key, long time){

        if(mode){
            cluster.pexpireAt(key, time);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.pexpireAt(key, time);
            }
        }
    }



    public  long hsetnx(String key, String field, String value){
        if(mode){
            return cluster.hsetnx(key, field, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return   jedis.hsetnx(key, field, value);
            }
        }
    }


    public  long setnx(String key, String value){
        if(mode){
            return cluster.setnx(key,value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return   jedis.setnx(key,value);
            }
        }
    }

    public  void incre(String key){
        if(mode){
            cluster.incr(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
               jedis.incr(key);
            }
        }
    }

    public  void hincrby(String key,String field, int value){
        if(mode){
            cluster.hincrBy(key,field,value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.hincrBy(key,field,value);
            }
        }

    }

    public  String hmset(String key, String nestedKey, String value){
        Map<String, String> map = new HashMap<>();
        map.put(nestedKey, value);
        if(mode){
            return  cluster.hmset(key, map);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.hmset(key, map);
            }
        }

    }




    public  String hmset(String key, Map<String, String> map) {

        if(mode){
            return  cluster.hmset(key, map);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.hmset(key, map);
            }
        }
    }

    public Map<String, String> hgetAll(String key) {

        if(mode){
            return  cluster.hgetAll(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.hgetAll(key);
            }
        }
    }



    public  long lPush(String key, String value ){
        if(mode){
            return  cluster.lpush(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.lpush(key, value);
            }
        }
    }




    //移除并获取列表的第一个元素
    public  String lPop(String key ) {
        if(mode){
            return  cluster.lpop(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.lpop(key);
            }
        }
    }


    //列表修剪，只保留指定区间的元素
    public  String lTrim(String key, int  start ,int stop ){
        if(mode){
            return  cluster.ltrim(key, start,stop);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.ltrim(key, start,stop);
            }
        }
    }

    public  long lPushx(String key, String value ){
        if(mode){
            return  cluster.lpushx(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.lpushx(key, value);
            }
        }

    }

    public  List<String> lRang(String key, long start , long end ){
        if(mode){
            return  cluster.lrange( key, start,end );
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.lrange( key, start,end );
            }
        }
    }


    public  long rPush(String key, String value ){
        if(mode){
            return  cluster.rpush(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.rpush(key, value);
            }
        }
    }

    public  long rPushx(String key, String value ){
        if(mode){
            return  cluster.rpushx(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.rpushx(key, value);
            }
        }
    }


    public  void setex(String key,int ttl, String value){
        if(mode){
              cluster.setex(key, ttl,value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                  jedis.setex(key, ttl,value);
            }
        }
    }


    public  void del(String key){
        if(mode){
            cluster.del(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.del(key);
            }
        }
    }

    public  void del(byte[] key){
        if(mode){
            cluster.del(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.del(key);
            }
        }
    }

    public  void hdel(String key,String field){
        if(mode){
            cluster.hdel(key,field);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.hdel(key,field);
            }
        }
    }

    public  void happend(String key,String value){
        if(mode){
            cluster.append(key,value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.append(key,value);
            }
        }
    }


    public  boolean exists (String key){
        if(mode){
           return cluster.exists(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
               return jedis.exists(key);
            }
        }
    }

    public boolean hexist(String key,String field){

        if(mode){
            return  cluster.hexists(key,field);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hexists(key,field);
            }
        }

    }

    public  Long hlen(String key){
        if(mode){
            return  cluster.hlen(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return  jedis.hlen(key);
            }
        }
    }



    public  void sadd(String key, String value){
        if(mode){
             cluster.sadd(key, value);
        }else {
            try(Jedis jedis = pool.getResource()) {
                 jedis.sadd(key, value);
            }
        }
    }

    public  void srem(String key, String member){
        if(mode){
            cluster.srem(key, member);
        }else {
            try(Jedis jedis = pool.getResource()) {
                jedis.srem(key, member);
            }
        }
    }

    public Set<String> smembers(String s) {
        if(mode){
            return cluster.smembers(s);
        }else {
            try(Jedis jedis = pool.getResource()) {
               return jedis.smembers(s);
            }
        }
    }

    public boolean sismember(String key ,String member) {
        if(mode){
            return cluster.sismember( key , member);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.sismember( key , member);
            }
        }
    }


    public long scard(String key){
        if(mode){
            return cluster.scard(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.scard(key);
            }
        }
    }


    public Long llen(String s) {
        if(mode){
            return cluster.llen(s);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.llen(s);
            }
        }
    }

    public long zcard(String key) {
        if(mode){
            return cluster.zcard(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zcard(key);
            }
        }
    }


    public long zadd(String key, double score , String member) {
        if(mode){
            return cluster.zadd(key,score,member);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zadd(key,score,member);
            }
        }
    }

    public long zrem(String key, String member) {
        if(mode){
            return cluster.zrem(key,member);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zrem(key,member);
            }
        }
    }




    public Set<String> zrange(String key, long start , long end) {
        if(mode){
            return cluster.zrange(key,start,end);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zrange(key,start,end);
            }
        }
    }


    public Set<String> zrangeByScore(String key, double start , double end) {
        if(mode){
            return cluster.zrangeByScore(key,start,end);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zrangeByScore(key,start,end);
            }
        }
    }

    public Set<String> zrevrange(String key, int start , int end) {
        if(mode){
            return cluster.zrevrange(key,start,end);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zrevrange(key,start,end);
            }
        }
    }


    public ScanResult<String> scan(String pattern,ScanParams param){
        if(mode){
            return cluster.scan(pattern, param);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.scan(pattern, param);
            }
        }
    }

    public ScanResult<String> sscan(String key,String cursor,ScanParams param){
        if(mode){
            return cluster.sscan(key,cursor, param);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.sscan(key,cursor, param);
            }
        }
    }

    public ScanResult<Tuple> zscan(String key,String cursor,ScanParams param){
        if(mode){
            return cluster.zscan(key,cursor, param);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.zscan(key,cursor, param);
            }
        }
    }


    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams param){
        if(mode){
            return cluster.hscan(key,cursor, param);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hscan(key,cursor, param);
            }
        }
    }


    public List<String> hvals(String key){
        if(mode){
            return cluster.hvals(key);
        }else {
            try(Jedis jedis = pool.getResource()) {
                return jedis.hvals(key);
            }
        }
    }



}
