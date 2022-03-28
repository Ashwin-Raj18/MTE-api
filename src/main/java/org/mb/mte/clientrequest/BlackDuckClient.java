package org.mb.mte.clientrequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.service.BlackDuckService;
import org.mb.mte.util.MteProperties;
import org.mb.mte.util.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mb.mte.util.JsonFormatUtil.getJsonObject;
import static org.mb.mte.util.JsonFormatUtil.getKeyValueJsonString;

@Component
public class BlackDuckClient {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    MteProperties props;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    BlackDuckService blackDuckService;

    private String bdAuthUri="/api/tokens/authenticate";
    private String bdProjectsUri = "/api/projects";


    private String webClientGet(String url, String authHeader){
        ResponseEntity<String> response = webClient.get()
                .uri(url)
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(
                        status -> status.value() != 200,
                        clientResponse -> Mono.empty()
                )
                .toEntity(String.class)
                .block();

        return response.getBody();
    }


    public void blackDuckProjects() throws Exception {
        //authenticate and get bearer token
        String authUrl = props.getBdUrl()  + bdAuthUri;
        String tokenHeader = "token "+props.getBdToken();
        String response = webClientGet(authUrl,tokenHeader);
        String auth = getKeyValueJsonString(response, "bearerToken");
        //call api to get list of project
        String url = props.getBdUrl()  + bdProjectsUri;
        String authHeader = "Bearer "+auth;
        String projects = webClientGet(url,authHeader);
        JSONObject jObj = new JSONObject(projects);
        JSONArray jArrItems = jObj.getJSONArray("items");
        List<String> bdProjects = new ArrayList();
        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            bdProjects.add(item.getString("name"));
        }
        redisRepository.addData(RedisKeys.blackDuckProjectsKey,bdProjects.toString());
    }

    public void bdMetrics() throws Exception {
        String bdData = getBlackDuckData();
        String filteredBdData= getRequiredBlackDuckData(bdData);
        List<String> projects = getListOfprojects(bdData);
        JSONArray projectsJson = new JSONArray(projects);

        redisRepository.addData(RedisKeys.blackDuckMetricsKey,filteredBdData);
        redisRepository.addData(RedisKeys.blackDuckProjectsKey,projectsJson.toString());
        //add individual project data

        JSONArray jArrItems = new JSONArray(filteredBdData);
        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            String projName = item.getString("name");
            redisRepository.addData(RedisKeys.blackDuckMetricsKey+"_"+projName, item.toString());
        }
        logger.info(">>>>>>>>>PUSHED BLACKDUCK DATA TO REDIS>>>>>>>");
    }

    public String getBlackDuckData() throws Exception {

        String projects = blackDuckService.getBdProjects();
        //get version details of each project
        String bdData = getVersionDetails( projects);
        //String bdData =getRequiredBlackDuckData(bdData);
        return bdData;
    }

    private String getVersionDetails( String projects )  throws Exception{
        String authUrl = props.getBdUrl()  + bdAuthUri;
        String tokenHeader = "token "+props.getBdToken();
        String auth = getKeyValueJsonString(webClientGet(authUrl,tokenHeader), "bearerToken");
        String authHeader = "Bearer "+auth;
        JsonObject project = getJsonObject(projects);
        JsonArray items = (JsonArray) project.get("items");
        JsonArray updateitems = new JsonArray();
        items.forEach(item->{
            JsonObject itemJson = getJsonObject(item.toString());
            JsonObject metaData = itemJson.getAsJsonObject("_meta");
            String URL = getKeyValueJsonString(metaData.toString() , "href") + "/versions";
            String response = webClientGet(URL, authHeader);
            itemJson.add("versions" ,getJsonObject(response));
            updateitems.add(itemJson);
        });
        project.remove("items"); // remove old item list
        project.add("items" ,updateitems ); // replace with new one
        logger.info("retrieved all BD version data");
        return project.toString();
    }

    private String getRequiredBlackDuckData(String bdData) {
        JSONObject jObj = new JSONObject(bdData);
        JSONArray jArrItems = jObj.getJSONArray("items");
        JSONArray jNewArrItems = new JSONArray();

        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            JSONObject newItem = new JSONObject();

            JSONObject version = item.getJSONObject("versions");
            JSONArray jVersionItems = version.getJSONArray("items");
            JSONArray jNewVersionItems = new JSONArray();

            for (Object jVersionItem : jVersionItems) {
                JSONObject newVersionItem = new JSONObject();
                JSONObject versionItem = (JSONObject) jVersionItem;
                if(versionItem.has("versionName"))
                    newVersionItem.put("versionName", versionItem.get("versionName"));
                if(versionItem.has("createdAt"))
                    newVersionItem.put("createdAt", versionItem.get("createdAt"));
                if(versionItem.has("securityRiskProfile"))
                    newVersionItem.put("securityRiskProfile", versionItem.get("securityRiskProfile"));
                if(versionItem.has("licenseRiskProfile"))
                    newVersionItem.put("licenseRiskProfile", versionItem.get("licenseRiskProfile"));
                if(versionItem.has("operationalRiskProfile"))
                    newVersionItem.put("operationalRiskProfile", versionItem.get("operationalRiskProfile"));
                jNewVersionItems.put(newVersionItem);
            }
            version.remove("appliedFilters");
            version.remove("_meta");
            version.remove("items");
            version.put("items", jNewVersionItems);

            newItem.put("name",item.getString("name"));
            newItem.put("versions",version);
            jNewArrItems.put(newItem);
        }

        return jNewArrItems.toString();
    }

    private List<String> getListOfprojects(String projects){
        JSONObject jObj = new JSONObject(projects);
        JSONArray jArrItems = jObj.getJSONArray("items");
        List<String> bdProjects = new ArrayList();
        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            bdProjects.add(item.getString("name"));
        }
        return bdProjects;
    }

}
