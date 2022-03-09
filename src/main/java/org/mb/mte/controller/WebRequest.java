package org.mb.mte.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.client.RedisJSONClient;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import org.redisson.config.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebRequest {

    public static void main(String[] args) throws IOException {
        String command =
                "curl -u 30e3d84ef28b817b48cfbcaf3153cb8b9aa011ba: https://rimini-sonar.dot.daimler.com/api/issues/search?componentKeys=arta&facetMode=effort&facets=types&types=CODE_SMELL";
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(new File("C:\\server_code_x\\performance-hack\\BlackDuckReporting"));
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        String text = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(text);
        String prettyJsonString = gson.toJson(je);
        System.out.println(prettyJsonString);
        System.out.println("exit value: "+process.exitValue());
        process.destroy();

        //redis
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedisJSONClient redisJSONClient = new RedisJSONClient(config);

        RedisJSON redisJSON = redisJSONClient.getRedisJSON();

        redisJSON.set("sonarQube", SetArgs.Builder.create(".", GsonUtils.toJson(prettyJsonString)));
        //Map<String, Object> actual = redisJSON.get(key, Map.class, new GetArgs().path(".").indent("\t").newLine("\n").space(" "));
        redisJSONClient.shutdown();
    }
}
