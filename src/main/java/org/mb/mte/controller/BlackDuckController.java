package org.mb.mte.controller;

import org.mb.mte.service.BlackDuckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "*")
@RestController
public class BlackDuckController {

    @Autowired
    private BlackDuckService blackDuckService;

    @GetMapping("/bdProjects")
    public List<String> getBdprojects(){
        return blackDuckService.getBdProjects();
    }

    @GetMapping("/bdMetricsByProject")
    public String getBdMetricsByProj(@RequestParam String project) {
        return blackDuckService.getBdMetricsByProject(project);
    }

    @GetMapping("/bdComponentByProject")
    public String getBdComponent(@RequestParam String project){
        return blackDuckService.getBdComponentsByProject(project);
    }

    @GetMapping("/bdVulsByProject")
    public String getBdVul(@RequestParam String project){
        return blackDuckService.getBdVulByProject(project);
    }

}

