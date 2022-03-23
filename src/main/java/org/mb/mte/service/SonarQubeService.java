package org.mb.mte.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SonarQubeService {

    @Autowired
    RedisRepository redisRepository;

    public JSONObject getSqMetricsByProject(String project){
       JSONObject jsonObj = new JSONObject( redisRepository.getData(RedisKeys.sqMetricsKey+"_"+project));
       JSONArray jArr = jsonObj.getJSONObject("component").getJSONArray("measures");
       //create new json
       Map<String, Object> mainMap = new HashMap();
       mainMap.put("key", jsonObj.getJSONObject("component").getString("key"));
       List<JSONObject> listMeasures = new ArrayList<>();
       for(Object obj: jArr){
           JSONObject measureObj = (JSONObject) obj;
           Map<String, String> measureMap = new HashMap();
           measureMap.put("metric",measureObj.getString("metric").replace("_"," "));
           if(measureObj.has("period")){
               measureMap.put("value",measureObj.getJSONObject("period").getString("value"));
           }
           if(measureObj.has("value")){
               measureMap.put("value",measureObj.getString("value"));
           }
           listMeasures.add(new JSONObject(measureMap));
       }

       mainMap.put("measures",listMeasures);
       return new JSONObject(mainMap);
    }

    public List<String> getSqProjects(){
        String projectStr =  redisRepository.getData(RedisKeys.sqProjectsKey);
        JSONObject jObj = new JSONObject(projectStr);
        JSONArray jArr = jObj.getJSONArray("components");
        List<String> sqProjects = new ArrayList();
        for(Object jProj: jArr){
            JSONObject project = (JSONObject)jProj;
            sqProjects.add(project.getString("key"));
        }
        return sqProjects;
    }
}
