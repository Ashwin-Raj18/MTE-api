package org.mb.mte.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.List;

@Service
public class JiraService {

    @Autowired
    RedisRepository redisRepository;

    public String getJiraProjects() {

       String Projects=redisRepository.getData(RedisKeys.jiraProjectsKey);
       return Projects;
    }

    public List<Object> getJiraIssues(String project) {
       String str=  redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        JSONArray jsonArray= jsonObject.getJSONArray("Issues");
        return jsonArray.toList();

    }
}
