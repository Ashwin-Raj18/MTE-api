package org.mb.mte.clientrequest;

import org.mb.mte.repository.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BlackDuckClient {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    private WebClient.Builder webClient;

    private String bdBaseUrl = "https://bdscan.daimler.com";
    private String bdToken = "ODc3NThiYWUtY2Y2Ni00ZTE5LWE0ZGUtOGVlYWVjM2I0MGEwOmIxMDE3M2U5LTY0ZjYtNDM3Mi1hZmJjLTg3NWQyZDU3YWNjZg==";

    /**
     * authenticate with the token and provides JWT
     * @return JWT token
     * @throws Exception
     */
    public String authenticate() throws Exception{
        String URL = bdBaseUrl  + "/api/tokens/authenticate";
        ResponseEntity<String> response = this.webClient.build().post()
                .uri(URL)
                .header("Authorization", "token "+bdToken)
                .retrieve()
                .onStatus(
                        status -> status.value() != 200,
                        clientResponse -> Mono.empty()
                )
                .toEntity(String.class)
                .block();
        if(response.getStatusCodeValue() != 200){
            throw new Exception("error while authenticating");
        }
        logger.info("Authentication successful");
        return response.getBody();
    }

    public String getListOfProjects(String auth) throws Exception {
        String URL = bdBaseUrl  + "/api/projects";
        ResponseEntity<String> response = this.webClient.build().get()
                .uri(URL)
                .header("Authorization", "Bearer "+auth)
                .retrieve()
                .onStatus(
                        status -> status.value() != 200,
                        clientResponse -> Mono.empty()
                )
                .toEntity(String.class)
                .block();
        if(response.getStatusCodeValue() != 200){
            throw new Exception("error while getting list of projects");
        }
        logger.info("retrieved all projects");
        return response.getBody();
    }

    public ResponseEntity<String> getVersionDetails(String auth, String VersionUrl){
        ResponseEntity<String> response = this.webClient.build().get()
                .uri(VersionUrl)
                .header("Authorization", "Bearer " + auth)
                .retrieve()
                .onStatus(
                        status -> status.value() != 200,
                        clientResponse -> Mono.empty()
                )
                .toEntity(String.class)
                .block();
        return response;
    }

}
