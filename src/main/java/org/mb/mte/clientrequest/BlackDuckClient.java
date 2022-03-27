package org.mb.mte.clientrequest;

import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.MteProperties;
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
    private WebClient webClient;

    @Autowired
    MteProperties props;

    /**
     * authenticate with the token and provides JWT
     * @return JWT token
     * @throws Exception
     */
    public String authenticate() throws Exception{
        String URL = props.getBdUrl()  + "/api/tokens/authenticate";
        ResponseEntity<String> response = webClient.post()
                .uri(URL)
                .header("Authorization", "token "+props.getBdToken())
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
        String URL = props.getBdUrl()  + "/api/projects";
        ResponseEntity<String> response = webClient.get()
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
        ResponseEntity<String> response = webClient.get()
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
