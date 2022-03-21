package org.mb.mte.clientrequest;

import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.MteProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JiraClient {


    @Autowired
    MteProperties props;

//    private String jiraProjectsUri = "project/search";

    private String webClientGet(String uri){
        WebClient client = WebClient.create("https://tweakers.atlassian.net");
        return client.get()
                .uri(uri)
                .headers(headers -> headers.setBasicAuth("ashwinrajrao@gmail.com","Zqx5XPHy6zfEF5fHqK717D83"))
                .retrieve()
                .bodyToMono(String.class).block();
    }

    public void jiraProjects() {
        String response = webClientGet("/rest/api/3/project/search");
        String sqProjectsJson = JsonFormatUtil.getJson(response);
        System.out.print(sqProjectsJson);
    }
}
