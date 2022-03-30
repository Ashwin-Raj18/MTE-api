package org.mb.mte.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class BlackDuckService {

    private static final Logger logger = LoggerFactory.getLogger(BlackDuckService.class);

    @Autowired
    RedisRepository redisRepository;


    public String getBdProjects(){
        String projects = getAllProjectsJson();
        JSONObject jObj = new JSONObject(projects);
        JSONArray jArrItems = jObj.getJSONArray("items");
        List<String> bdProjects = new ArrayList();
        for(Object jItem: jArrItems){
            JSONObject item = (JSONObject)jItem;
            bdProjects.add(item.getString("name"));
        }
        return bdProjects.toString();
    }


    public String getBdMetricsByProject(String project) {
        return  redisRepository.getData(RedisKeys.blackDuckMetricsKey+"_"+project);
    }

    public String getBdComponentsByProject(String project){
        String allComp = getBdAllComponentsJson(project);
        JSONArray baseArr = new JSONArray(allComp);
        for(Object compObj: baseArr){
            JSONObject compElem  = (JSONObject) compObj;
            JSONObject compJson = compElem.getJSONObject("component");
            compJson.remove("_meta");
            compJson.remove("appliedFilters");
            JSONArray itemsArr = compJson.getJSONArray("items");
            for(Object itemObj: itemsArr){
                JSONObject itemJson = (JSONObject)itemObj;
                itemJson.remove("_meta");
                itemJson.remove("versionRiskProfile");
                itemJson.remove("componentVersion");
                itemJson.remove("activityRiskProfile");
                itemJson.remove("component");
                itemJson.remove("origins");
                itemJson.remove("operationalRiskProfile");
                itemJson.remove("componentVersionName");
                itemJson.remove("activityData");
                itemJson.remove("operationalRiskProfile");
            }
        }
        return baseArr.toString();
    }

    public String getBdVulByProject(String project){
        String allVuls =  getAllVulJson(project);
        JSONArray baseArr = new JSONArray(allVuls);
        for(Object vulObj: baseArr){
            JSONObject vulElem  = (JSONObject) vulObj;
            JSONObject vulJson = vulElem.getJSONObject("vul");
            vulJson.remove("_meta");
            vulJson.remove("appliedFilters");
            JSONArray itemsArr = vulJson.getJSONArray("items");
            for(Object itemObj: itemsArr){
                JSONObject itemJson = (JSONObject)itemObj;
                itemJson.remove("_meta");
                itemJson.remove("componentVersion");
                itemJson.remove("component");
                itemJson.remove("componentVersionOrigin");
            }
        }
        return baseArr.toString();
    }
    
    public String getAllProjectsJson(){
        return redisRepository.getData(RedisKeys.blackDuckProjectsKey);
    }

    public String getBdAllComponentsJson(String project){
        return redisRepository.getData(RedisKeys.blackduckComponentKey+"_"+project);
    }

    public String getAllVulJson(String project){
        return redisRepository.getData(RedisKeys.blackduckVulKey+"_"+project);
    }

}
