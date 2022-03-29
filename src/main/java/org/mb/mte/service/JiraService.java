package org.mb.mte.service;

//import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class JiraService {

    @Autowired
    RedisRepository redisRepository;

    public String getJiraProjects() {

        String Projects = redisRepository.getData(RedisKeys.jiraProjectsKey);
        return Projects;
    }

    public List<Object> getJiraIssues(String project) {
        String str = redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("Issues");
            JSONArray FinalIssuesArray = filterIssueBasedonProject(project, jsonArray);
            return FinalIssuesArray.toList();
        } else {
            return null;
        }

    }

    private JSONArray filterIssueBasedonProject(String project, JSONArray jsonArray) {
        JSONArray outputArr = new JSONArray();

        for (Object obj : jsonArray) {
            JSONObject object = (JSONObject) obj;
            JSONObject fieldsObj = (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
//           System.out.println(projName);
            if (projName.equalsIgnoreCase(project)) {
                outputArr.put(obj);
            }
        }
        return outputArr;
    }

    public HashSet<String> getJiraSprints(String project) {
        String str = redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("Issues");
            HashSet<String> FinalIssuesArray = filterSprintsofProject(project, jsonArray);
            return FinalIssuesArray;
        } else {
            return null;
        }
    }

    public List<Object> getJiraSprintsIssues(String project, String sprint) {
        String str = redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("Issues");
            List<Object> FinalIssuesArray = filterIssuesofSprintsofProject(project, sprint, jsonArray);
            return FinalIssuesArray;
        } else {
            return null;
        }
    }

    private List<Object> filterIssuesofSprintsofProject(String project, String sprint, JSONArray jsonArray) {
        JSONArray outputArr = new JSONArray();
        HashSet<String> hashSet = new HashSet<>();
        for (Object obj : jsonArray) {
            JSONObject object = (JSONObject) obj;
            JSONObject fieldsObj = (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
            JSONObject SprintObj = (JSONObject) fieldsObj.get("sprint");
            if (!SprintObj.isEmpty()) {
                String sprintName = SprintObj.get("name").toString();
                if (projName.equalsIgnoreCase(project) && sprintName.equalsIgnoreCase(sprint)) {
                    outputArr.put(obj);
                }
            }


        }
        return outputArr.toList();
    }

    private HashSet<String> filterSprintsofProject(String project, JSONArray jsonArray) {
        HashSet<String> hashSet = new HashSet<>();
        for (Object obj : jsonArray) {
            JSONObject object = (JSONObject) obj;
            JSONObject fieldsObj = (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
            JSONObject SprintObj = (JSONObject) fieldsObj.get("sprint");
            if (!SprintObj.isEmpty()) {
                String SprintName = SprintObj.get("name").toString();
                if (projName.equalsIgnoreCase(project)) {
                    hashSet.add(SprintName);
                }
            }


        }
        return hashSet;
    }

    public Map<String, Object> getJiraSprintMetrics(String project) {
        String str = redisRepository.getData(RedisKeys.jiraIssuesKey);
        JSONObject jsonObject = new JSONObject(str);
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("Issues");
            Map<String, Object> Finalmetric = FilterMetrics(project, jsonArray);
            return Finalmetric;
        } else {
            return null;
        }
    }

    private Map<String, Object> FilterMetrics(String project, JSONArray jsonArray) {
//        JSONObject outPutObject = new JSONObject();
        Map<String, Object> outputmap = new HashMap<>();
        JSONObject Status = new JSONObject();
        JSONObject IssueType = new JSONObject();
        HashMap<String, Integer> statusmetricsMap = new HashMap<>();
        HashMap<String, Integer> issuesmetricsMap = new HashMap<>();
        String sprintName = null;
        String startDate = null;
        String endDate = null;
        for (Object obj : jsonArray) {
            JSONObject object = (JSONObject) obj;
            JSONObject fieldsObj = (JSONObject) object.get("fields");
            String projName = fieldsObj.get("projectName").toString();
            JSONObject SprintObj = (JSONObject) fieldsObj.get("sprint");
            if (!SprintObj.isEmpty()) {
                String state = SprintObj.get("state").toString();
                if (projName.equalsIgnoreCase(project) && state.equals("active")) {
                    sprintName = SprintObj.getString("name");
                    startDate = SprintObj.getString("startDate");
                    endDate = SprintObj.getString("endDate");
                    JSONObject issueType = (JSONObject) fieldsObj.get("issuetype");
                    String issueTypeName = issueType.getString("name");
                    if (issuesmetricsMap.get(issueTypeName) != null) {
                        int issuenum = issuesmetricsMap.get(issueTypeName);

                        issuenum += 1;
                        issuesmetricsMap.put(issueTypeName, issuenum);
                    } else {
                        issuesmetricsMap.put(issueTypeName, 1);
                    }
                    String StatusName = fieldsObj.getString("status");
                    if (statusmetricsMap.get(StatusName) != null) {
                        int j = statusmetricsMap.get(StatusName);
                        j += 1;
                        statusmetricsMap.put(StatusName, j);
                    } else {
                        statusmetricsMap.put(StatusName, 1);
                    }
                }
            }

        }
//        System.out.print(issuesmetricsMap);
//        System.out.print(statusmetricsMap);
        outputmap.put("status", statusmetricsMap);
        outputmap.put("issueType", issuesmetricsMap);
        outputmap.put("SprintName", sprintName);
        outputmap.put("StartDate",startDate);
        outputmap.put("EndDate",endDate);
        System.out.println(outputmap);

        return outputmap;
    }
}
