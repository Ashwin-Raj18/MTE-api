package org.mb.mte.controller;

import org.json.JSONObject;
import org.mb.mte.service.SonarQubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class SonarQubeController {

    @Autowired
    SonarQubeService sonarQubeService;

    @GetMapping("/sqProjects")
    public List<String> getSqProjects() {
        return sonarQubeService.getSqProjects();
    }

    @GetMapping("/sqMetricsByProject")
    public String getSqMetricsByProj(@RequestParam String project) {
        JSONObject res = sonarQubeService.getSqMetricsByProject(project);
        return res.toString();
    }

    @GetMapping("/sqIssuesByProject")
    public String getSqIssues(@RequestParam String project){
        JSONObject res = sonarQubeService.getSqIssues(project);
        return res.toString();
    }

    @GetMapping("/sqHotspotsByProject")
    public String getHotSpots(@RequestParam String project){
        return sonarQubeService.getSqHotSpots(project);
    }
}
