package org.mb.mte.clientrequest;


import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
@Component
public class SonarQubeClient {

    @Autowired
    RedisRepository redisRepository;

    private String scBaseUrl = "https://rimini-sonar.dot.daimler.com";
    private String scToken = "7b262d48b75b9d3686f89c5810c4f038e63ee9bd";
    private String scMetricsUri = "api/measures/component?metricKeys=new_code_smells,new_bugs,new_vulnerabilities,ncloc,new_coverage";
    private String scProjectsUri = "api/components/search?qualifiers=TRK";

    private String webClientGet(String uri){
        WebClient client = WebClient.create(scBaseUrl);
        return client.get()
                .uri(uri)
                .headers(headers -> headers.setBasicAuth(scToken, ""))
                .retrieve()
                .bodyToMono(String.class).block();
    }

    public void sqMetrics() {
        String respose = webClientGet(scMetricsUri);
        String scMettricJson = JsonFormatUtil.getJson(respose);
        redisRepository.addData(RedisKeys.scMetricsKey,scMettricJson);
    }

    public void sqProjects() {
        String respose = webClientGet(scProjectsUri);
        String scProjectsJson = JsonFormatUtil.getJson(respose);
        redisRepository.addData(RedisKeys.scProjectsKey,scProjectsJson);
    }
}
