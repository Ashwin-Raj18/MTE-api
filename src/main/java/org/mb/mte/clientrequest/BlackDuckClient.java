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

    private String bearerToken;
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

    private String webClientPost(String url, String authHeader){
        ResponseEntity<String> response = webClient.post()
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

    public void bdAuthBearerToken(){
        //authenticate and get bearer token
        String authUrl = props.getBdUrl()  + bdAuthUri;
        String tokenHeader = "token "+props.getBdToken();
        String response = webClientPost(authUrl,tokenHeader);
        this.bearerToken = "Bearer "+getKeyValueJsonString(response, "bearerToken");
    }


    public void blackDuckProjects() throws Exception {
        String url = props.getBdUrl()  + bdProjectsUri;
        String projects = webClientGet(url,this.bearerToken);
        redisRepository.addData(RedisKeys.blackDuckProjectsKey,projects.toString());
        logger.info(">>>>>>>>>PUSHED BLACKDUCK PROJECTS TO REDIS>>>>>>>");
    }

    public void bdMetrics() throws Exception {
        String projects = blackDuckService.getAllProjectsJson();
        String bdVersionData = getVersionData( projects);;
        String bdProfileData = getBdProfileData(bdVersionData);
        redisRepository.addData(RedisKeys.blackDuckMetricsKey,bdProfileData);

        //add individual project data
        JSONArray jArrItems = new JSONArray(bdProfileData);
        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            String projName = item.getString("name");
            redisRepository.addData(RedisKeys.blackDuckMetricsKey+"_"+projName, item.toString());
        }
        logger.info(">>>>>>>>>PUSHED BLACKDUCK METRICS TO REDIS>>>>>>>");
    }


    public  void bdComponents(){

        JSONArray projectsArry = new JSONArray(blackDuckService.getBdProjects());
        for(Object project: projectsArry){
            String profileStr = blackDuckService.getBdMetricsByProject((String) project);
            JSONArray profileItemsArr = new JSONObject(profileStr).getJSONObject("versions").getJSONArray("items");
            JSONArray finalCompArr = new JSONArray();
            for(Object profile:profileItemsArr){
                JSONObject jo = (JSONObject)profile;
                String compHref = jo.getString("VersionHref")+"/components?offset=0&limit=100";
                String bdComp = webClientGet(compHref,this.bearerToken);
                JSONObject compObj  = new JSONObject();
                compObj.put("component",new JSONObject(bdComp));
                compObj.put("createdAt",jo.getString("createdAt"));
                compObj.put("versionName",jo.getString("versionName"));
                finalCompArr.put(compObj);
            }
            redisRepository.addData(RedisKeys.blackduckComponentKey+"_"+(String)project,finalCompArr.toString());
        }
    }

    public  void bdVulnerabilities(){
        JSONArray projectsArry = new JSONArray(blackDuckService.getBdProjects());
        for(Object project: projectsArry){
            String profileStr = blackDuckService.getBdMetricsByProject((String) project);
            JSONArray profileItemsArr = new JSONObject(profileStr).getJSONObject("versions").getJSONArray("items");
            JSONArray finalCompArr = new JSONArray();
            for(Object profile:profileItemsArr){
                JSONObject jo = (JSONObject)profile;
                String vulHref = jo.getString("VersionHref")+"/vulnerability-bom?offset=0&limit=100";
                String bdVul = webClientGet(vulHref,this.bearerToken);
                JSONObject vulObj  = new JSONObject();
                vulObj.put("vul",new JSONObject(bdVul));
                vulObj.put("createdAt",jo.getString("createdAt"));
                vulObj.put("versionName",jo.getString("versionName"));
                finalCompArr.put(vulObj);
            }
            redisRepository.addData(RedisKeys.blackduckVulKey+"_"+(String)project,finalCompArr.toString());
        }
    }


    private String getVersionData( String projects )  throws Exception{

        JsonObject project = getJsonObject(projects);
        JsonArray items = (JsonArray) project.get("items");
        JsonArray updateitems = new JsonArray();
        items.forEach(item->{
            JsonObject itemJson = getJsonObject(item.toString());
            JsonObject metaData = itemJson.getAsJsonObject("_meta");
            String URL = getKeyValueJsonString(metaData.toString() , "href") + "/versions";
            String response = webClientGet(URL, this.bearerToken);
            itemJson.add("versions" ,getJsonObject(response));
            updateitems.add(itemJson);
        });
        project.remove("items"); // remove old item list
        project.add("items" ,updateitems ); // replace with new one
        logger.info("retrieved all BD version data");
        return project.toString();
    }

    private String getBdProfileData(String bdData) {
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
                if(versionItem.has("_meta"))
                    newVersionItem.put("VersionHref",versionItem.getJSONObject("_meta").getString("href"));
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

}
