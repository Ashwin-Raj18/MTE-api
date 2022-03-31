package org.mb.mte.controller;

import org.json.JSONObject;
import org.mb.mte.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
public class JiraController {

    @Autowired
    JiraService jiraService;


    //fetches the jira Projects
    @GetMapping("/jiraProjects")
    public String fetchJiraProjects(){
        return jiraService.getJiraProjects();
    }

    //fetches the all Jira Issues of a Project
    @GetMapping("/project/jiraIssues")
    public List<Object> fetchJiraProjectIssues(@RequestParam String project){
       return jiraService.getJiraIssues(project);
    }

    //Fetches the list of Sprints of a project
    @GetMapping("/project/Sprints")
    public HashSet<String> fetchJiraProjectSprints(@RequestParam String project){
        return jiraService.getJiraSprints(project);
    }

    //fetches the List of Issues of the sprint of the project
    @GetMapping("/project/Sprint/Issues")
    public List<Object> fetchJiraProjectSprintIssues(@RequestParam String project,@RequestParam String sprint){
        return jiraService.getJiraSprintsIssues(project,sprint);
    }

    @GetMapping("project/metrics")
    public ResponseEntity<Map<String,Object>> fetchMetricsCurrentSprint(@RequestParam String project){
        return ResponseEntity.ok().body(jiraService.getJiraSprintMetrics(project));
    }


}
