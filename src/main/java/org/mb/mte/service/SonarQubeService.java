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

    public JSONObject getSqIssues(String project){
        JSONObject jsonObj = new JSONObject(redisRepository.getData(RedisKeys.sqIssuesKey+"_"+project));
        JSONArray jArr = jsonObj.getJSONArray("issues");

        Map<String, Object> mainMap = new HashMap();
        mainMap.put("project",project);
        List<JSONObject> listIssues = new ArrayList<>();
        for(Object obj: jArr){
            JSONObject issueObj = (JSONObject) obj;
            Map<String, Object> issueMap = new HashMap();
            issueMap.put("severity",issueObj.getString("severity"));
            issueMap.put("component",issueObj.getString("component"));
            issueMap.put("line",issueObj.getInt("line"));
            issueMap.put("message",issueObj.getString("message"));
            issueMap.put("effort",issueObj.getString("effort"));
            issueMap.put("author",issueObj.getString("author"));
            issueMap.put("type",issueObj.getString("type"));
            issueMap.put("creationDate",issueObj.getString("creationDate"));
            issueMap.put("scope",issueObj.getString("scope"));
            listIssues.add(new JSONObject(issueMap));
        }
        mainMap.put("issues",listIssues);
        return new JSONObject(mainMap);

    }

    public String getSqHotSpots(String project){
        String hotSpotStr =  redisRepository.getData(RedisKeys.sqHotspotKey+"_"+project);
        return hotSpotStr;
    }
}
