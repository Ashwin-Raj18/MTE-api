package org.mb.mte.repository;

import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.client.RedisJSONClient;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import org.mb.mte.util.MteProperties;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class RedisRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    MteProperties props;


    private RedisJSONClient redisInit(){
        Config config = new Config();
        String url = "redis://"+props.getRedisUrl();
        logger.info("Redis url: {}", url);
        config.useSingleServer().setAddress(url);
        return new RedisJSONClient(config);
    }

    public void addData(String key, String value){

        RedisJSONClient redisJSONClient = redisInit();
        RedisJSON redisJSON = redisJSONClient.getRedisJSON();
        Map<String,String> map = new HashMap<>();
        map.put(key,value);
        redisJSON.set(key, SetArgs.Builder.create(".", GsonUtils.toJson(map)));
        redisJSONClient.shutdown();
    }

    public String getData(String key){
        RedisJSONClient redisJSONClient = redisInit();
        RedisJSON redisJSON = redisJSONClient.getRedisJSON();
        Map<String, Object> actual = redisJSON.get(key, Map.class, new GetArgs().path(".").indent("\t").newLine("\n").space(" "));
        redisJSONClient.shutdown();
        return (String) actual.get(key);
    }

}
