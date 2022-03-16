package org.mb.mte.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.mb.mte.clientrequest.BlackDuckClient;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import static org.mb.mte.util.JsonFormatUtil.getJsonObject;
import static org.mb.mte.util.JsonFormatUtil.getKeyValueJsonString;

@Service
public class BlackDuckService {

    @Autowired
    private WebClient.Builder webClient;

    @Autowired
    private BlackDuckClient blackDuckClient;

    @Autowired
    RedisRepository redisRepository;

    private final String baseUrl = "https://bdscan.daimler.com";

    /**
     * @return json blackduck project data along with version details
     * @throws Exception
     */
    public String getBlackDuckData() throws Exception {
        //authenticate and get bearer token
        String auth = getKeyValueJsonString(blackDuckClient.authenticate(), "bearerToken");
        //call api to get list of project
        String projects = blackDuckClient.getListOfProjects(auth);
        //get version details of each project
        String projectData = getVersionDetails(auth, projects);
        return projectData;
    }


    /**
     * get version details of each project used and combines
     * with project data
     * @param auth
     * @param projects
     * @return balckduck data
     * @throws Exception
     */
    private String getVersionDetails(String auth , String projects )  throws Exception{
        JsonObject project = getJsonObject(projects);
        JsonArray items = (JsonArray) project.get("items");
        JsonArray updateitems = new JsonArray();
        items.forEach(item->{
            JsonObject itemJson = getJsonObject(item.toString());
            JsonObject metaData = itemJson.getAsJsonObject("_meta");
            String URL = getKeyValueJsonString(metaData.toString() , "href") + "/versions";
            ResponseEntity<String> response = blackDuckClient.getVersionDetails(auth , URL);
            itemJson.add("versions" ,getJsonObject(response.getBody()));
            updateitems.add(itemJson);
        });
        project.remove("items"); // remove old item list
        project.add("items" ,updateitems ); // replace with new one
        return project.toString();
    }


    /**
     * get the scanned blackduck data and push to redis
     * @throws Exception
     */
    public void bdScanDetails() throws Exception {
        String response = getBlackDuckData();
        String bcProjectsJson = JsonFormatUtil.getJson(response);
        redisRepository.addData(RedisKeys.blackDuckMetricsKey,bcProjectsJson);
    }
}
