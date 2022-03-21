package org.mb.mte.clientrequest;

import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.MteProperties;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JiraClient {

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    MteProperties props;

    private String jiraProjectsUri = "project/search";

    private String webClientGet(String uri){
        WebClient client = WebClient.create("https://tweakers.atlassian.net/rest/api/3/");
        return client.get()
                .uri(uri)
                .headers(headers -> headers.setBasicAuth("ajaykumarsh022@gmail.com","eFZ54XDoXhxA304vtuqo0FE0"))
                .retrieve()
                .bodyToMono(String.class).block();
    }

    public void jiraProjects() {
        String response = webClientGet(jiraProjectsUri);
        String jiraProjectsJson = JsonFormatUtil.getJson(response);
        redisRepository.addData(RedisKeys.  jiraProjectsKey,jiraProjectsJson);
        System.out.print(jiraProjectsJson);
    }
}
