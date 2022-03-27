package org.mb.mte.clientrequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.JsonFormatUtil;
import org.mb.mte.util.MteProperties;
import org.mb.mte.util.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class JiraClient {


    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);
    @Autowired
    RedisRepository redisRepository;

    @Autowired
    MteProperties props;

    private String jiraProjectsUrl = "/rest/api/3/project/search?maxResults=1000&startAt=0";
    private String jiraIssuesUrl = "/rest/api/3/search?jql=";

    private String webClientGet(String uri) {
        WebClient client = WebClient.create(props.getJiraUrl());
        return client.get()
                .uri(uri)
                .headers(headers -> headers.setBasicAuth(props.getJiraUsername(),props.getJiraToken()))
                .retrieve()
                .bodyToMono(String.class).block();
    }

    public void jiraProjects() {
        String response = webClientGet(jiraProjectsUrl);
        logger.info("api response from JIRA:{}",response);
        System.out.println(response);
        JSONArray filteredArr=filterJiraProjects(response);
        String jiraProjectsJson = JsonFormatUtil.getJson(filteredArr.toString());
        logger.info("Filtered Projects-----------------------"+jiraProjectsJson);
        redisRepository.addData(RedisKeys.jiraProjectsKey,jiraProjectsJson);
    }

    private JSONArray filterJiraProjects(String response) {
        logger.info("filtering of Jira Projects Started for {}",response);
        JSONObject json = new JSONObject(response);
        JSONArray projArr = json.getJSONArray("values");
        JSONArray FinalProjArray= new JSONArray();
        for(Object obj:projArr){
            JSONObject jObj = (JSONObject) obj;
            FinalProjArray.put(jObj.getString("name"));

        }
       return  FinalProjArray;
    }

    public void jiraIssuesProject() {
        String response = webClientGet(jiraIssuesUrl);
        logger.info("response from jira Issues: {}",response);
        JSONObject jsonObject=filterJiraIssues(response);
        String jiraIssues = jsonObject.toString();
        System.out.print(jiraIssues);
        redisRepository.addData(RedisKeys.jiraIssuesKey,jiraIssues);
    }

    private JSONObject filterJiraIssues(String response) {

        Map<String, String> map = new HashMap<String, String>();


        JSONArray FJsonArr = new JSONArray();

        JSONObject json = new JSONObject(response);
        JSONArray jArr = json.getJSONArray("issues");
        for (Object obj : jArr) {
            JSONObject FJsonObj = new JSONObject();
//            logger.info(++i + "------------------>>>>>Array val ="+obj);
            JSONObject issueObj = (JSONObject) obj;

            map.put("key", issueObj.getString("key"));
            JSONObject childrens = issueObj.getJSONObject("fields");
            JSONObject jsonFields = new JSONObject();

            //Filtering IssueType
            JSONObject jsonIssueType = new JSONObject();
            jsonIssueType.put("description", childrens.getJSONObject("issuetype").getString("description"));
            jsonIssueType.put("name", childrens.getJSONObject("issuetype").getString("name"));
            jsonIssueType.put("subtask", childrens.getJSONObject("issuetype").getBoolean("subtask"));
            jsonFields.put("issuetype", jsonIssueType);

            //filtering the project related
            String projectName = childrens.getJSONObject("project").getString("name");

            String createdDate = childrens.getString("created");

            //Filtering the Sprint data
            JSONObject jsonSprint = new JSONObject();

            JSONArray jSprintArray = childrens.optJSONArray("customfield_10020");
            if (jSprintArray != null) {
                JSONObject jsprintObj = (JSONObject) jSprintArray.get(0);
                jsonSprint.put("id", jsprintObj.get("id").toString());
                jsonSprint.put("name", jsprintObj.get("name"));
                jsonSprint.put("state", jsprintObj.get("state"));
                jsonSprint.put("startDate", jsprintObj.get("startDate"));
                jsonSprint.put("endDate", jsprintObj.get("endDate"));
            }


            String priority = childrens.getJSONObject("priority").getString("name");

            //Filtering the assignee info
            JSONObject jsonAssignee = new JSONObject();

            if(!childrens.get("assignee").equals(null)){
                Object jsonAs=  childrens.get("assignee");
                JSONObject jsonDummyAssign = (JSONObject) jsonAs;
                jsonAssignee.put("displayName",jsonDummyAssign.get("displayName"));
            }


            String updatedDate = childrens.getString("updated");

            //Filtering the status
            String status = childrens.getJSONObject("status").getString("name");

            //Filtering the Headline of the issue
            String summary = childrens.getString("summary");

            //filtering the creator
            String creator = childrens.getJSONObject("creator").getString("displayName");

            String reporter = childrens.getJSONObject("reporter").getString("displayName");

            //Filling Field Object
            jsonFields.put("projectName", projectName);
            jsonFields.put("sprint", jsonSprint);
            jsonFields.put("priority", priority);
            jsonFields.put("createdDate", createdDate);
            jsonFields.put("Assignee", jsonAssignee);
            jsonFields.put("updatedDate", updatedDate);
            jsonFields.put("status", status);
            jsonFields.put("summary", summary);
            jsonFields.put("creator", creator);
            jsonFields.put("reporter", reporter);
            map.put("fields", String.valueOf(jsonFields));
            FJsonObj.put("key",issueObj.getString("key"));
            FJsonObj.put("fields",jsonFields);
            FJsonArr.put(FJsonObj);

        }
        JSONObject IssuesObject = new JSONObject();
        IssuesObject.put("Count",json.get("total"));
        IssuesObject.put("Issues",FJsonArr);
        return IssuesObject;

    }

}
