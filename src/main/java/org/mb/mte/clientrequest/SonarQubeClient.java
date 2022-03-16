package org.mb.mte.clientrequest;


import org.mb.mte.repository.RedisRepository;
import org.mb.mte.service.SonarQubeService;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.MteProperties;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class SonarQubeClient {

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    MteProperties props;

    @Autowired
    SonarQubeService sqService;

    private String sqMetricsUri = "api/measures/component?metricKeys=new_code_smells,new_bugs,new_vulnerabilities,ncloc,new_coverage";
    private String sqProjectsUri = "api/components/search?qualifiers=TRK";

    private String webClientGet(String uri){
        WebClient client = WebClient.create(props.getSqUrl());
        return client.get()
                .uri(uri)
                .headers(headers -> headers.setBasicAuth(props.getSqToken(), ""))
                .retrieve()
                .bodyToMono(String.class).block();
    }

    public void sqMetricsByProject() {
        List<String> sqProjects = sqService.getSqProjects();
        for(String proj:sqProjects){
            String respose = webClientGet(sqMetricsUri+"&component="+proj);
            String sqMettricJson = JsonFormatUtil.getJson(respose);
            redisRepository.addData(RedisKeys.sqMetricsKey+"_"+proj,sqMettricJson);
        }
    }

    public void sqProjects() {
        String respose = webClientGet(sqProjectsUri);
        String sqProjectsJson = JsonFormatUtil.getJson(respose);
        redisRepository.addData(RedisKeys.sqProjectsKey,sqProjectsJson);
    }
}
