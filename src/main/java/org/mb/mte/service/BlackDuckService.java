package org.mb.mte.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BlackDuckService {

    @Autowired
    private WebClient.Builder webClient;

    private final String baseUrl = "https://bdscan.daimler.com";

    /**
     *
     * @param token
     * @return json blackduck project data along with version details
     * @throws Exception
     */
    public Object getBlackDuckData(String token) throws Exception{
        //authenticate and get bearer token
        String auth = authenticate(token).getString("bearerToken");
        //call api to get list of project
        Object projects = getListOfProjects(auth);
        //get version details of each project
        Object projectData = getVersionDetails(auth , projects);

        return projectData;
}

    /**
     * authenticate with the token and provides JWT
     * @param token
     * @return JWT token
     * @throws Exception
     */
    public JSONObject authenticate(String token) throws Exception{
        String URL = baseUrl  + "/api/tokens/authenticate";
        ResponseEntity<String> response = this.webClient.build().post()
                .uri(URL)
                .header("Authorization", "token "+token)
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
        System.out.println("Authentication successful");
        return new JSONObject(response.getBody());
    }

    /**
     * get list of project used by user
     * @param auth
     * @return project details objects
     * @throws Exception
     */
    private Object getListOfProjects(String auth) throws Exception {
        String URL = baseUrl  + "/api/projects";
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
        System.out.println("retrieved all projects");

        return response.getBody();
    }

    /**
     * get version details of each project used and combines
     * with project data
     * @param auth
     * @param projects
     * @return balckduck data
     * @throws Exception
     */
    private Object getVersionDetails(String auth , Object projects )  throws Exception{
        JSONObject project = new JSONObject(projects.toString());
        JSONArray items = (JSONArray) project.get("items");
        JSONArray updateitems = new JSONArray();
        items.forEach(item->{
                JSONObject itemjson = new JSONObject(item.toString());
                String URL = itemjson.getJSONObject("_meta").get("href").toString() + "/versions";
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
                itemjson.put("versions" ,new JSONObject(response.getBody()));
                updateitems.put(itemjson);
        });
        project.remove("items"); // remove old item list
        project.put("items" ,updateitems ); // replace with new one
        return project;
    }

}
