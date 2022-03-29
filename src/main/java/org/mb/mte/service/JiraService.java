package org.mb.mte.service;

//import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
        if(jsonObject!=null){
            JSONArray jsonArray= jsonObject.getJSONArray("Issues");
            JSONArray FinalIssuesArray = filterIssueBasedonProject(project,jsonArray);
            return FinalIssuesArray.toList();
        }else{
            return null;
        }
        
    }

    private JSONArray filterIssueBasedonProject(String project, JSONArray jsonArray) {
        JSONArray outputArr= new JSONArray();

        for(Object obj:jsonArray){
            JSONObject object= (JSONObject) obj;
           JSONObject fieldsObj= (JSONObject) object.get("fields");
           String projName=  fieldsObj.get("projectName").toString();
//           System.out.println(projName);
           if(projName.equalsIgnoreCase(project)){
               outputArr.put(obj);
           }
        }
        return outputArr;
    }

    public HashSet<String> getJiraSprints(String project) {
        String str=  redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if(jsonObject!=null){
            JSONArray jsonArray= jsonObject.getJSONArray("Issues");
            HashSet<String> FinalIssuesArray = filterSprintsofProject(project,jsonArray);
            return FinalIssuesArray;
        }else{
            return null;
        }
    }
    public List<Object> getJiraSprintsIssues(String project, String sprint) {
        String str=  redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if(jsonObject!=null){
            JSONArray jsonArray= jsonObject.getJSONArray("Issues");
            List<Object> FinalIssuesArray = filterIssuesofSprintsofProject(project,sprint,jsonArray);
            return FinalIssuesArray;
        }else{
            return null;
        }
    }

    private List<Object> filterIssuesofSprintsofProject(String project,String sprint, JSONArray jsonArray) {
        JSONArray outputArr= new JSONArray();
        HashSet<String> hashSet= new HashSet<>();
        for(Object obj:jsonArray){
            JSONObject object= (JSONObject) obj;
            JSONObject fieldsObj= (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
            JSONObject SprintObj=(JSONObject) fieldsObj.get("sprint");
            if(!SprintObj.isEmpty()){
                String sprintName =SprintObj.get("name").toString();
                if(projName.equalsIgnoreCase(project)&&sprintName.equalsIgnoreCase(sprint)){
                    outputArr.put(obj);
                }
            }



        }
        return outputArr.toList();
    }

    private HashSet<String> filterSprintsofProject(String project, JSONArray jsonArray) {
        HashSet<String> hashSet= new HashSet<>();
        for(Object obj:jsonArray){
            JSONObject object= (JSONObject) obj;
            JSONObject fieldsObj= (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
            JSONObject SprintObj=(JSONObject) fieldsObj.get("sprint");
            if(!SprintObj.isEmpty()){
                String SprintName=  SprintObj.get("name").toString();
                if(projName.equalsIgnoreCase(project)){
                    hashSet.add(SprintName);
            }
            }


        }
        return hashSet;
    }

//    public List<Object> getJiraSprintMetrics(String project) {
//
//    }
}
