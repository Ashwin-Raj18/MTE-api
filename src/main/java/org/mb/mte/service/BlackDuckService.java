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

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    RedisRepository redisRepository;


    public String getBdProjects(){
        String projects = getProjectsJson();
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
    
    public String getProjectsJson(){
        return redisRepository.getData(RedisKeys.blackDuckProjectsKey);
    }

}
