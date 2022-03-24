package org.mb.mte.controller;

import org.mb.mte.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JiraController {

    @Autowired
    JiraService jiraService;

    @GetMapping("/jiraProjects")
    public String fetchJiraProjects(){
        return jiraService.getJiraProjects();
    }

    @GetMapping("/project/jiraIssues")
    public List<Object> fetchJiraProjectIssues(@RequestParam String project){
       return jiraService.getJiraIssues(project);

    }
}
