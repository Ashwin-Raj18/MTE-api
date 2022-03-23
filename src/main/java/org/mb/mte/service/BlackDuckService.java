package org.mb.mte.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.clientrequest.BlackDuckClient;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mb.mte.util.JsonFormatUtil.getJsonObject;
import static org.mb.mte.util.JsonFormatUtil.getKeyValueJsonString;

@Service
public class BlackDuckService {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    private WebClient.Builder webClient;

    @Autowired
    private BlackDuckClient blackDuckClient;

    @Autowired
    RedisRepository redisRepository;


    public List<String> getBlackDuckProjectNames() throws Exception {
        //authenticate and get bearer token
        String auth = getKeyValueJsonString(blackDuckClient.authenticate(), "bearerToken");
        //call api to get list of project
        String projects = blackDuckClient.getListOfProjects(auth);
        List<String> projectList = getListOfprojects(projects);
        return projectList;
    }

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
        String bdData = getVersionDetails(auth, projects);
        //String bdData =getRequiredBlackDuckData(bdData);
        return bdData;
    }

    public String getBlackDuckMetricsByProject(String projectName) throws Exception {
        //authenticate and get bearer token
        String auth = getKeyValueJsonString(blackDuckClient.authenticate(), "bearerToken");
        //call api to get list of project and its project id
        String projects = blackDuckClient.getListOfProjects(auth);
        //get particular project version data
        String projectData = getVersionDetailsOfParticularProject(auth, projects , projectName);

        String jsonData =getRequiredBlackDuckProjectData(projectData);
        return jsonData;
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
        logger.info("retrieved all BD version data");
        return project.toString();
    }

    /**
     * get version details of each project used and combines
     * with project data
     * @param auth
     * @param projects
     * @return balckduck data
     * @throws Exception
     */
    private String getVersionDetailsOfParticularProject(String auth , String projects , String projectName )  throws Exception{
        JsonObject project = getJsonObject(projects);
        JsonArray items = (JsonArray) project.get("items");
        JsonArray updateitems = new JsonArray();
        String url = "";
        for(int i=0;i< items.size() ; i++)
        {
            JsonObject itemJson = getJsonObject(items.get(i).toString());
            if(getKeyValueJsonString(itemJson.toString() , "name").equalsIgnoreCase(projectName)) {
                JsonObject metaData = itemJson.getAsJsonObject("_meta");
                String URL = getKeyValueJsonString(metaData.toString() , "href") + "/versions";
                ResponseEntity<String> response = blackDuckClient.getVersionDetails(auth , URL);
                logger.info("retrieved BD project data");
                return response.getBody();
            }
        }
        return "";
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

    private String getRequiredBlackDuckProjectData(String bdData) {
        JSONObject version = new JSONObject(bdData);
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
        return version.toString();
    }


    public String getBdProjects() {
        return redisRepository.getData(RedisKeys.blackDuckProjectsKey);
    }

    public String getBdMetricsByProject(String project) {
        return  redisRepository.getData(RedisKeys.blackDuckMetricsKey+"_"+project);
    }

    public void setBdProjectsData() throws Exception {
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


}
